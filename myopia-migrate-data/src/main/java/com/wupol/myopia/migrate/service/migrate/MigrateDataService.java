package com.wupol.myopia.migrate.service.migrate;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdcardUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;
import com.wupol.myopia.business.aggregation.export.excel.imports.PlanStudentExcelImportService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.migrate.domain.dos.SchoolAndGradeClassDO;
import com.wupol.myopia.migrate.domain.dos.ScreeningOrgAndStaffDO;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.domain.model.SysStudentEyeSimple;
import com.wupol.myopia.migrate.service.SysStudentEyeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 迁移数据服务
 * TODO: 1. 给表sys_student_eye表的dept_id字段加索引  2. 上线后需要重置筛查机构、学校、筛查人员账号密码，修改筛查机构类型
 *
 * @Author HaoHao
 * @Date 2022/1/6
 **/
@Slf4j
@Service
public class MigrateDataService {

    private static final int SYS_STUDENT_ID_KEY = -1;

    @Autowired
    private SysStudentEyeService sysStudentEyeService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private PlanStudentExcelImportService planStudentExcelImportService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private VirtualStudentService virtualStudentService;
    @Autowired
    private MigrateScreeningDataService migrateScreeningDataService;
    @Autowired
    private MigrateScreeningOrganizationService migrateScreeningOrganizationService;
    @Autowired
    private MigrateSchoolAndGradeClassService migrateSchoolAndGradeClassService;


    /**
     * 迁移数据
     *
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class)
    public void migrateData() {
        log.info("====================  开始-迁移数据.....  ====================");
        SchoolAndGradeClassDO schoolAndGradeClassDO = migrateSchoolAndGradeClassService.migrateSchoolAndGradeClass();
        List<ScreeningOrgAndStaffDO> screeningOrgAndStaffList = migrateScreeningOrganizationService.migrateScreeningOrgAndScreeningStaff();
        migrateStudentAndScreeningResult(schoolAndGradeClassDO, screeningOrgAndStaffList);
        log.info("====================  完成-迁移数据.....  ====================");
    }


    private void migrateStudentAndScreeningResult(SchoolAndGradeClassDO schoolAndGradeClassDO, List<ScreeningOrgAndStaffDO> screeningOrgAndStaffList) {
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
                createPlanAndBindSchool(schoolAndGradeClassDO, screeningOrgAndStaffDO, startDate, getDateByDateStr(year + "06-30"), year + "年上半年", halfYearStudentEyeMap.get(true));
                createPlanAndBindSchool(schoolAndGradeClassDO, screeningOrgAndStaffDO, endDate, getDateByDateStr(year + "12-31"), year + "年下半年", halfYearStudentEyeMap.get(false));
            });
        });
    }

    /**
     * 创建筛查计划，同时为计划绑定学校
     *
     * @param schoolAndGradeClassDO
     * @param screeningOrgAndStaffDO
     * @param startDate
     * @param endDate
     * @param titlePrefix
     * @param halfYearStudentEyeMap
     * @return void
     **/
    private void createPlanAndBindSchool(SchoolAndGradeClassDO schoolAndGradeClassDO, ScreeningOrgAndStaffDO screeningOrgAndStaffDO, Date startDate, Date endDate,
                                         String titlePrefix, List<SysStudentEyeSimple> halfYearStudentEyeMap) {
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
        Map<String, List<SysStudentEyeSimple>> halfYearStudentEyeGroupBySchoolIdMap = halfYearStudentEyeMap.stream()
                .collect(Collectors.groupingBy(SysStudentEyeSimple::getSchoolId));
        // 封装当前计划下的所有学校数据list
        List<ScreeningPlanSchool> planSchoolList = halfYearStudentEyeGroupBySchoolIdMap.entrySet().stream()
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
        // 迁移学生
        migrateStudent(schoolAndGradeClassDO, screeningOrgAndStaffDO, screeningPlanDTO, halfYearStudentEyeGroupBySchoolIdMap);
    }

    private void migrateStudent(SchoolAndGradeClassDO schoolAndGradeClassDO, ScreeningOrgAndStaffDO screeningOrgAndStaffDO,
                                ScreeningPlanDTO screeningPlan, Map<String, List<SysStudentEyeSimple>> halfYearStudentEyeGroupBySchoolIdMap) {
        // 逐个学校迁移
        halfYearStudentEyeGroupBySchoolIdMap.forEach((oldSchoolId, oneSchoolHalfYearStudentEyeList) -> {
            Integer newSchoolId = schoolAndGradeClassDO.getSchoolMap().get(oldSchoolId);
            // TODO: 验证取最新一条的代码是否可行
            // 获取每个学生半年内最新一条筛查记录
            Comparator<? super SysStudentEyeSimple> comparator = Comparator.comparing(SysStudentEyeSimple::getCreateTime);
            Map<String, SysStudentEyeSimple> latestSysStudentEyeMap = oneSchoolHalfYearStudentEyeList.stream().collect(Collectors.toMap(SysStudentEyeSimple::getStudentId, Function.identity(), BinaryOperator.maxBy(comparator)));
            List<String> eyeIdList = latestSysStudentEyeMap.values().stream().map(SysStudentEyeSimple::getEyeId).collect(Collectors.toList());
            // 获取所有学生完整筛查数据
            List<SysStudentEye> sysStudentEyeList = sysStudentEyeService.listByIds(eyeIdList);
            // 转为Map，对于没有IdCard的走虚拟学生
            Map<Boolean, List<SysStudentEye>> isHasIdCardSysStudentEyeMap = sysStudentEyeList.stream()
                    .collect(Collectors.partitioningBy(x -> StringUtils.isNotBlank(x.getStudentIdcard()) && IdcardUtil.isValidCard(x.getStudentIdcard())));
            List<Map<Integer, String>> noIdCardStudentInfoList = getNoIdCardStudentInfoList(isHasIdCardSysStudentEyeMap.get(false), newSchoolId, schoolAndGradeClassDO.getGradeMap(), schoolAndGradeClassDO.getClassMap(), screeningPlan);
            List<Map<Integer, String>> hasIdCardStudentInfoList = isHasIdCardSysStudentEyeMap.get(true).stream().map(SysStudentEye::convertToMap).collect(Collectors.toList());
            hasIdCardStudentInfoList.addAll(noIdCardStudentInfoList);
            // 批量插入（模拟通过Excel上传）
            planStudentExcelImportService.insertByUpload(1, hasIdCardStudentInfoList, screeningPlan, newSchoolId);
            screeningPlanService.updateStudentNumbers(1, screeningPlan.getId(), screeningPlanSchoolStudentService.getCountByScreeningPlanId(screeningPlan.getId()));
            // 迁移筛查数据
            Map<String, String> sysStudentIdAndScreeningCodeMap = noIdCardStudentInfoList.stream().collect(Collectors.toMap(x -> x.get(SYS_STUDENT_ID_KEY), x -> x.get(ImportExcelEnum.SCREENING_CODE.getIndex())));
            migrateScreeningDataService.migrateScreeningResult(sysStudentEyeList, newSchoolId, screeningOrgAndStaffDO.getScreeningOrgId(), screeningOrgAndStaffDO.getScreeningStaffId(), screeningPlan.getId(), sysStudentIdAndScreeningCodeMap);
        });
    }

    private List<Map<Integer, String>> getNoIdCardStudentInfoList(List<SysStudentEye> noIdCardStudentEyeList, Integer schoolId,
                                                                  Map<String, Integer> gradeMap, Map<String, Integer> classMap, ScreeningPlan screeningPlan) {
        return noIdCardStudentEyeList.stream().map(sysStudentEye -> {
            Map<Integer, String> noIdCardStudent = sysStudentEye.convertToMap();
            Integer gradeId = gradeMap.get(sysStudentEye.getSchoolId() + sysStudentEye.getSchoolGrade());
            Integer classId = classMap.get(sysStudentEye.getSchoolId() + sysStudentEye.getSchoolGrade() + sysStudentEye.getSchoolClazz());
            ScreeningPlanSchoolStudent screeningPlanSchoolStudent = virtualStudentService.createVirtualStudent(screeningPlan, schoolId, gradeId, sysStudentEye.getSchoolGrade(), classId, sysStudentEye.getSchoolClazz());
            noIdCardStudent.put(ImportExcelEnum.SCREENING_CODE.getIndex(), String.valueOf(screeningPlanSchoolStudent.getScreeningCode()));
            noIdCardStudent.put(SYS_STUDENT_ID_KEY, sysStudentEye.getStudentId());
            return noIdCardStudent;
        }).collect(Collectors.toList());
    }


    private Date getDateByDateStr(String dateStr) {
        return DateUtil.parse(dateStr, DateFormatUtil.FORMAT_ONLY_DATE);
    }
}
