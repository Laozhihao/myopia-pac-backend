package com.wupol.myopia.migrate.service.migrate;

import cn.hutool.core.date.DateUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.migrate.domain.dos.SchoolAndGradeClassDO;
import com.wupol.myopia.migrate.domain.dos.ScreeningOrgAndStaffDO;
import com.wupol.myopia.migrate.domain.dos.PlanAndStudentDO;
import com.wupol.myopia.migrate.domain.model.SysStudentEyeSimple;
import com.wupol.myopia.migrate.service.SysStudentEyeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2022/4/10
 **/
@Log4j2
@Service
public class MigratePlanService {

    @Autowired
    private SysStudentEyeService sysStudentEyeService;
    @Autowired
    private ScreeningPlanService screeningPlanService;

    /**
     * 创建筛查计划，同时为计划绑定学校（逐个筛查机构创建）
     *
     * @param schoolAndGradeClassDO     学校、年级、班级信息
     * @param screeningOrgAndStaffList  筛查机构信息 list
     * @return java.util.List<com.wupol.myopia.migrate.domain.dos.ScreeningPlanAndPendingMigrateStudentEyeDO>
     **/
    @Transactional(rollbackFor = Exception.class)
    public List<PlanAndStudentDO> createPlanAndBindSchool(SchoolAndGradeClassDO schoolAndGradeClassDO, List<ScreeningOrgAndStaffDO> screeningOrgAndStaffList) {
        log.info("====================  ......【3】开始-迁移-计划.....  ====================");
        List<PlanAndStudentDO> planAndStudentList = new ArrayList<>();
        screeningOrgAndStaffList.forEach(screeningOrgAndStaffDO -> {
            // 获取该筛查机构下所有筛查数据记录
            List<SysStudentEyeSimple> simpleStudentEyeDataList = sysStudentEyeService.getSimpleDataList(screeningOrgAndStaffDO.getOldScreeningOrgId());
            // 按年分组
            Map<String, List<SysStudentEyeSimple>> oneYearStudentEyeMap = simpleStudentEyeDataList.stream().collect(Collectors.groupingBy(x -> DateUtil.format(x.getCreateTime(), DateFormatUtil.FORMAT_ONLY_YEAR)));
            // 同年的按上下半年分为两组
            oneYearStudentEyeMap.forEach((year, studentEyeSimpleList) -> {
                Date startDate = DateUtil.parse(year + "-01-01", DateFormatUtil.FORMAT_ONLY_DATE);
                Date endDate = DateUtil.parse(year + "-07-01", DateFormatUtil.FORMAT_ONLY_DATE);
                Map<Boolean, List<SysStudentEyeSimple>> halfYearStudentEyeMap = studentEyeSimpleList.stream().collect(Collectors.partitioningBy(x -> x.getCreateTime().after(startDate) && x.getCreateTime().before(endDate)));
                planAndStudentList.add(createPlanAndBindSchool(schoolAndGradeClassDO, screeningOrgAndStaffDO, startDate, getDateByDateStr(year + "06-30"), year + "年上半年", halfYearStudentEyeMap.get(true)));
                planAndStudentList.add(createPlanAndBindSchool(schoolAndGradeClassDO, screeningOrgAndStaffDO, endDate, getDateByDateStr(year + "12-31"), year + "年下半年", halfYearStudentEyeMap.get(false)));
            });
        });
        log.info("====================  ......【3】完成-迁移-计划.....  ====================");
        return planAndStudentList.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 创建筛查计划，同时为计划绑定学校
     *
     * @param schoolAndGradeClassDO     学校、年级、班级信息
     * @param screeningOrgAndStaffDO    筛查机构信息
     * @param startDate                 开始日期
     * @param endDate                   结束日期
     * @param titlePrefix               筛查计划标题前缀
     * @param currentPlanStudentList    当前计划的筛查学生
     * @return com.wupol.myopia.migrate.domain.dos.ScreeningPlanAndPendingMigrateStudentEyeDO
     **/
    private PlanAndStudentDO createPlanAndBindSchool(SchoolAndGradeClassDO schoolAndGradeClassDO,
                                                     ScreeningOrgAndStaffDO screeningOrgAndStaffDO,
                                                     Date startDate, Date endDate,
                                                     String titlePrefix, List<SysStudentEyeSimple> currentPlanStudentList) {
        if (CollectionUtils.isEmpty(currentPlanStudentList)) {
            return null;
        }
        // 封装计划实体
        ScreeningPlanDTO screeningPlanDTO = new ScreeningPlanDTO();
        screeningPlanDTO.setTitle(titlePrefix + screeningOrgAndStaffDO.getScreeningOrgName() + "筛查计划")
                .setScreeningOrgId(screeningOrgAndStaffDO.getScreeningOrgId())
                .setCreateUserId(1)
                .setDistrictId(screeningOrgAndStaffDO.getDistrictId())
                .setCreateTime(startDate)
                .setStartTime(startDate)
                .setEndTime(endDate)
                .setReleaseStatus(CommonConst.STATUS_RELEASE)
                .setReleaseTime(startDate)
                .setOperatorId(1)
                .setOperateTime(startDate);
        // 根据学校ID分组
        Map<String, List<SysStudentEyeSimple>> currentPlanStudentGroupBySchoolIdMap = currentPlanStudentList.stream()
                .collect(Collectors.groupingBy(SysStudentEyeSimple::getSchoolId));
        // 封装当前计划下的所有学校数据list
        List<ScreeningPlanSchool> planSchoolList = currentPlanStudentGroupBySchoolIdMap.entrySet().stream()
                .map(sysStudentEyeSimpleMap -> new ScreeningPlanSchool()
                        .setSchoolId(schoolAndGradeClassDO.getSchoolMap().get(sysStudentEyeSimpleMap.getKey()))
                        .setScreeningOrgId(screeningOrgAndStaffDO.getScreeningOrgId())
                        .setQualityControllerCommander(screeningOrgAndStaffDO.getScreeningStaffName())
                        .setQualityControllerName(screeningOrgAndStaffDO.getScreeningStaffName())
                        .setSchoolName(sysStudentEyeSimpleMap.getValue().get(0).getSchoolName()))
                .collect(Collectors.toList());
        screeningPlanDTO.setSchools(planSchoolList);
        // 创建筛查计划，同时为计划绑定学校
        screeningPlanService.saveOrUpdateWithSchools(1, screeningPlanDTO, false);
        return new PlanAndStudentDO(screeningPlanDTO, currentPlanStudentGroupBySchoolIdMap, screeningOrgAndStaffDO.getScreeningStaffUserId());
    }


    /**
     * 根据日期字符串获取日期
     *
     * @param dateStr 日期字符串
     * @return java.util.Date
     **/
    private Date getDateByDateStr(String dateStr) {
        return DateUtil.parse(dateStr, DateFormatUtil.FORMAT_ONLY_DATE);
    }

}
