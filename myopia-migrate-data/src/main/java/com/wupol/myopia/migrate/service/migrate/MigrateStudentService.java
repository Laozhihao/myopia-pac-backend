package com.wupol.myopia.migrate.service.migrate;

import cn.hutool.core.util.IdcardUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;
import com.wupol.myopia.business.aggregation.export.excel.imports.PlanStudentExcelImportService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.migrate.domain.dos.PlanAndStudentDO;
import com.wupol.myopia.migrate.domain.dos.SchoolAndGradeClassDO;
import com.wupol.myopia.migrate.domain.dos.ScreeningDataDO;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.domain.model.SysStudentEyeSimple;
import com.wupol.myopia.migrate.service.SysStudentEyeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 迁移学生数据
 * 注意：
 *      1.没有身份证号码的学生走虚拟
 *
 * @Author HaoHao
 * @Date 2022/4/10
 **/
@Service
public class MigrateStudentService {

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

    /**
     * 逐个计划迁移学生数据（上传到对应筛查计划下）
     *
     * @param schoolAndGradeClassDO     学校、年级和班级信息
     * @param planAndStudentList        计划和学生信息list
     * @return java.util.List<com.wupol.myopia.migrate.domain.dos.ScreeningDataDO>
     **/
    @Transactional(rollbackFor = Exception.class)
    public List<ScreeningDataDO> migrateStudentByPlan(SchoolAndGradeClassDO schoolAndGradeClassDO, List<PlanAndStudentDO> planAndStudentList) {
        List<ScreeningDataDO> screeningDataList = new ArrayList<>();
        // 逐个计划迁移
        planAndStudentList.forEach(planAndStudentDO -> {
            // 逐个学校迁移
            screeningDataList.addAll(migrateStudentBySchool(schoolAndGradeClassDO, planAndStudentDO));
        });
        return screeningDataList;
    }

    /**
     * 逐个学校迁移学生数据
     *
     * @param schoolAndGradeClassDO 学校、年级和班级信息
     * @param planAndStudentDO      计划和学生信息
     * @return java.util.List<com.wupol.myopia.migrate.domain.dos.ScreeningDataDO>
     **/
    private List<ScreeningDataDO> migrateStudentBySchool(SchoolAndGradeClassDO schoolAndGradeClassDO, PlanAndStudentDO planAndStudentDO) {
        List<ScreeningDataDO> screeningDataList = new ArrayList<>();
        ScreeningPlan screeningPlan = planAndStudentDO.getScreeningPlan();
        Map<String, List<SysStudentEyeSimple>> pendingMigrateStudentEyeMap = planAndStudentDO.getCurrentPlanStudentGroupBySchoolIdMap();
        Integer screeningStaffUserId = planAndStudentDO.getScreeningStaffUserId();
        // 逐个学校迁移
        pendingMigrateStudentEyeMap.forEach((oldSchoolId, oneSchoolHalfYearStudentEyeList) -> {
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
            screeningDataList.add(new ScreeningDataDO(sysStudentEyeList, newSchoolId, screeningPlan.getScreeningOrgId(), screeningStaffUserId, screeningPlan.getId(), sysStudentIdAndScreeningCodeMap));
        });
        return screeningDataList;
    }

    /**
     * 获取没有身份证号码的学生
     *
     * @param noIdCardStudentEyeList    没有身份证号码的学生筛查数据
     * @param schoolId                  学校ID
     * @param gradeMap                  年级信息
     * @param classMap                  班级信息
     * @param screeningPlan             筛查计划
     * @return java.util.List<java.util.Map<java.lang.Integer,java.lang.String>>
     **/
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
}
