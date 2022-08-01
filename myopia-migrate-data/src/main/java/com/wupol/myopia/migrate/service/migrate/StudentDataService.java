package com.wupol.myopia.migrate.service.migrate;

import cn.hutool.core.date.DateUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;
import com.wupol.myopia.business.aggregation.export.excel.imports.PlanStudentExcelImportService;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.SourceClientEnum;
import com.wupol.myopia.business.core.common.constant.ArtificialStatusConstant;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningCodeGenerator;
import com.wupol.myopia.migrate.domain.dos.SchoolAndGradeClassDO;
import com.wupol.myopia.migrate.domain.dos.ScreeningDataDO;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.domain.model.SysStudentEyeSimple;
import com.wupol.myopia.migrate.service.SysStudentEyeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 虚拟学生
 *
 * @Author HaoHao
 * @Date 2022/3/27
 **/
@Log4j2
@Service
public class StudentDataService {

    private static final int SYS_EYE_ID_KEY = -1;

    @Autowired
    private StudentService studentService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SysStudentEyeService sysStudentEyeService;
    @Autowired
    private PlanStudentExcelImportService planStudentExcelImportService;


    /**
     * 迁移学生
     *
     * @param screeningDataList
     * @param oneSchoolHalfYearStudentEyeList
     * @param screeningPlan
     * @param schoolAndGradeClassDO
     * @param newSchoolId
     * @param screeningStaffUserId
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void migrateStudent(List<ScreeningDataDO> screeningDataList, List<SysStudentEyeSimple> oneSchoolHalfYearStudentEyeList, ScreeningPlan screeningPlan, SchoolAndGradeClassDO schoolAndGradeClassDO, Integer newSchoolId, Integer screeningStaffUserId) {
        Integer planId = screeningPlan.getId();

        // TODO: 验证取最新一条的代码是否可行
        // 获取每个学生半年内最新一条筛查记录
        Comparator<? super SysStudentEyeSimple> comparator = Comparator.comparing(SysStudentEyeSimple::getCreateTime);
        Map<String, SysStudentEyeSimple> latestSysStudentEyeMap = oneSchoolHalfYearStudentEyeList.stream().collect(Collectors.toMap(SysStudentEyeSimple::getStudentId, Function.identity(), BinaryOperator.maxBy(comparator)));
        List<String> eyeIdList = latestSysStudentEyeMap.values().stream().map(SysStudentEyeSimple::getEyeId).collect(Collectors.toList());

        // 获取所有学生完整筛查数据
        List<SysStudentEye> sysStudentEyeList = sysStudentEyeService.listByIds(eyeIdList);
        screeningDataList.add(new ScreeningDataDO(sysStudentEyeList, newSchoolId, screeningPlan.getScreeningOrgId(), screeningStaffUserId, planId));

        // 迁移过的，不再处理，节省时间
        if (screeningPlanSchoolStudentService.count(new ScreeningPlanSchoolStudent().setScreeningPlanId(planId).setSchoolId(newSchoolId)) > 0) {
            log.warn("【{}】的所有学生 - 已经迁移到计划，不需要再处理，id={}", oneSchoolHalfYearStudentEyeList.get(0).getSchoolName(), newSchoolId);
            return;
        }

        // 转为Map，对于没有IdCard的走虚拟学生
        Map<Boolean, List<SysStudentEye>> isHasIdCardSysStudentEyeMap = sysStudentEyeList.stream()
                .collect(Collectors.partitioningBy(x -> SysStudentEye.isValidIdCard(x.getStudentIdcard())));
        // 无身份证的
        List<Map<Integer, String>> noIdCardStudentInfoList = getNoIdCardStudentInfoList(isHasIdCardSysStudentEyeMap.get(false), newSchoolId, schoolAndGradeClassDO.getGradeMap(), schoolAndGradeClassDO.getClassMap(), screeningPlan);
        // 有身份证的
        List<Map<Integer, String>> hasIdCardStudentInfoList = isHasIdCardSysStudentEyeMap.get(true).stream().map(SysStudentEye::convertToMap).collect(Collectors.toList());
        hasIdCardStudentInfoList.addAll(noIdCardStudentInfoList);

        // 批量插入（模拟通过Excel上传）
        planStudentExcelImportService.insertByUpload(1, hasIdCardStudentInfoList, screeningPlan, newSchoolId);
        screeningPlanService.updateStudentNumbers(1, planId, screeningPlanSchoolStudentService.getCountByScreeningPlanId(planId));

        // 保存没有身份证的计划学生的山西筛查数据ID，方便后续筛查数据迁移
        if (CollectionUtils.isEmpty(noIdCardStudentInfoList)) {
            return;
        }
        Map<Long, String> screeningCodeAndSysEyeIdMap = noIdCardStudentInfoList.stream().collect(Collectors.toMap(x -> Long.valueOf(x.get(ImportExcelEnum.SCREENING_CODE.getIndex())), x -> x.get(SYS_EYE_ID_KEY)));
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningCodes(noIdCardStudentInfoList.stream().map(x -> Long.valueOf(x.get(ImportExcelEnum.SCREENING_CODE.getIndex()))).collect(Collectors.toList()), planId);
        screeningPlanSchoolStudentService.updateBatchById(planSchoolStudentList.stream().map(x -> x.setMigrateStudentScreeningId(screeningCodeAndSysEyeIdMap.get(x.getScreeningCode()))).collect(Collectors.toList()));
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
            ScreeningPlanSchoolStudent screeningPlanSchoolStudent = createVirtualStudent(screeningPlan, schoolId, gradeId, sysStudentEye.getSchoolGrade(), classId, sysStudentEye.getSchoolClazz());
            noIdCardStudent.put(ImportExcelEnum.SCREENING_CODE.getIndex(), String.valueOf(screeningPlanSchoolStudent.getScreeningCode()));
            noIdCardStudent.put(SYS_EYE_ID_KEY, sysStudentEye.getEyeId());
            return noIdCardStudent;
        }).collect(Collectors.toList());
    }

    /**
     * 创建虚拟学生
     *
     * @param plan
     * @param schoolId
     * @param gradeId
     * @param gradeName
     * @param classId
     * @param className
     * @return com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent
     **/
    public ScreeningPlanSchoolStudent createVirtualStudent(ScreeningPlan plan, Integer schoolId, Integer gradeId, String gradeName, Integer classId, String className) {
        GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByName(gradeName);
        Date birthday = SchoolAge.getBirthdayBySchoolAgeType(gradeCodeEnum.getType());
        Long screeningCode = ScreeningCodeGenerator.nextId();
        School school = schoolService.getById(schoolId);
        // 创建管理端学生
        Student student = new Student();
        student.setSchoolId(schoolId);
        student.setCreateUserId(1);
        student.setGradeId(gradeId);
        student.setClassId(classId);
        student.setName(String.valueOf(screeningCode));
        student.setGender(GenderEnum.MALE.type);
        student.setSourceClient(SourceClientEnum.SCREENING_PLAN.type);
        student.setGradeType(gradeCodeEnum.getType());
        student.setBirthday(birthday);
        studentService.save(student);
        // 创建筛查计划学生
        ScreeningPlanSchoolStudent planSchoolStudent = new ScreeningPlanSchoolStudent();
        planSchoolStudent.setSrcScreeningNoticeId(plan.getSrcScreeningNoticeId());
        planSchoolStudent.setScreeningTaskId(plan.getScreeningTaskId());
        planSchoolStudent.setScreeningPlanId(plan.getId());
        planSchoolStudent.setScreeningOrgId(plan.getScreeningOrgId());
        planSchoolStudent.setPlanDistrictId(plan.getDistrictId());
        planSchoolStudent.setSchoolDistrictId(school.getDistrictId());
        planSchoolStudent.setSchoolId(school.getId());
        planSchoolStudent.setSchoolName(school.getName());
        planSchoolStudent.setGradeId(gradeId);
        planSchoolStudent.setGradeName(gradeName);
        planSchoolStudent.setClassId(classId);
        planSchoolStudent.setClassName(className);
        planSchoolStudent.setStudentId(student.getId());
        planSchoolStudent.setGradeType(gradeCodeEnum.getType());
        planSchoolStudent.setBirthday(birthday);
        planSchoolStudent.setGender(GenderEnum.MALE.type);
        planSchoolStudent.setStudentAge(DateUtil.ageOfNow(birthday));
        planSchoolStudent.setStudentName(student.getName());
        planSchoolStudent.setArtificial(ArtificialStatusConstant.ARTIFICIAL);
        planSchoolStudent.setScreeningCode(screeningCode);
        screeningPlanSchoolStudentService.save(planSchoolStudent);
        // 更新筛查计划学生总数
        screeningPlanService.updateStudentNumbers(1, plan.getId(), screeningPlanSchoolStudentService.getCountByScreeningPlanId(plan.getId()));
        return planSchoolStudent;
    }
}
