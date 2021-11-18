package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.framework.core.util.DateFormatUtil;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.management.domain.dto.MockStudentRequestDTO;
import com.wupol.myopia.business.api.management.domain.dto.PlanStudentRequestDTO;
import com.wupol.myopia.business.api.management.domain.vo.SchoolGradeVO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author wulizhou
 * @Date 2021/4/25 10:34
 */
@Service
public class ScreeningPlanSchoolStudentBizService {

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private ScreeningPlanService screeningPlanService;

    /**
     * 获取计划中的学校年级情况
     *
     * @param screeningPlanId
     * @param schoolId
     * @return
     */
    public List<SchoolGradeVO> getSchoolGradeVoByPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        //1. 获取该计划学校的筛查学生所有年级、班级
        List<GradeClassesDTO> gradeClasses = screeningPlanSchoolStudentService.selectSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId);
        //2. 根据年级分组
        Map<Integer, List<GradeClassesDTO>> graderIdClasses = gradeClasses.stream().collect(Collectors.groupingBy(GradeClassesDTO::getGradeId));
        //3. 组装SchoolGradeVo数据
        return graderIdClasses.keySet().stream().map(gradeId -> {
            SchoolGradeVO vo = new SchoolGradeVO();
            vo.setUniqueId(UUID.randomUUID().toString());
            List<GradeClassesDTO> gradeClassesDTOS = graderIdClasses.get(gradeId);
            // 查询并设置年级名称
            vo.setId(gradeId)
                    .setName(schoolGradeService.getGradeNameById(gradeId));
            // 查询并设置班级名称
            vo.setClasses(gradeClassesDTOS.stream().map(dto -> {
                SchoolClassDTO schoolClass = new SchoolClassDTO();
                schoolClass.setUniqueId(UUID.randomUUID().toString());
                schoolClass.setId(dto.getClassId())
                        .setName(schoolClassService.getClassNameById(dto.getClassId()))
                        .setGradeId(gradeId);
                return schoolClass;
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 分页获取筛查计划的学校学生数据
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningStudentDTO> getPage(ScreeningStudentQueryDTO query, PageRequest pageRequest) {
        Assert.notNull(query.getScreeningPlanId(), "筛查计划ID不能为空");
        Assert.notNull(query.getSchoolId(), "筛查学校ID不能为空");
        Page<ScreeningStudentDTO> page = (Page<ScreeningStudentDTO>) pageRequest.toPage();
        if (StringUtils.hasLength(query.getGradeIds())) {
            query.setGradeList(Stream.of(StringUtils.commaDelimitedListToStringArray(query.getGradeIds())).map(Integer::parseInt).collect(Collectors.toList()));
        }
        IPage<ScreeningStudentDTO> studentDTOIPage = screeningPlanSchoolStudentService.selectPageByQuery(page, query);
        // 设置民族、地址
        studentDTOIPage.getRecords().forEach(studentDTO ->
                studentDTO.setNationDesc(NationEnum.getName(studentDTO.getNation()))
                        .setAddress(districtService.getAddressDetails(studentDTO.getProvinceCode(), studentDTO.getCityCode(), studentDTO.getAreaCode(), studentDTO.getTownCode(), studentDTO.getAddress()))
        );
        return studentDTOIPage;
    }

    /**
     * 生成虚拟学生
     *
     * @param requestDTO      请求入参
     * @param screeningPlanId 筛查计划Id
     * @param schoolId        学校Id
     * @param currentUser     登录用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void initMockStudent(MockStudentRequestDTO requestDTO, Integer screeningPlanId,
                                Integer schoolId, CurrentUser currentUser) {
        Integer studentTotal = requestDTO.getStudentTotal();
        if (Objects.isNull(studentTotal) || studentTotal > 500) {
            throw new BusinessException("学生总数异常");
        }
        School school = schoolService.getById(schoolId);
        ScreeningPlan plan = screeningPlanService.getById(screeningPlanId);

        List<MockStudentRequestDTO.GradeItem> gradeItem = requestDTO.getGradeItem();
        if (!CollectionUtils.isEmpty(gradeItem)) {
            gradeItem.forEach(schoolGrade -> {
                List<MockStudentRequestDTO.ClassItem> classItem = schoolGrade.getClassItem();
                classItem.forEach(schoolClass -> {
                    List<Student> mockStudentList = new ArrayList<>(studentTotal);
                    List<ScreeningPlanSchoolStudent> mockPlanStudentList = new ArrayList<>(studentTotal);
                    mockStudent(studentTotal, school, schoolGrade, schoolClass, mockStudentList, currentUser);
                    mockPlanStudent(studentTotal, school, plan, schoolGrade, schoolClass, mockStudentList, mockPlanStudentList);
                });
            });
        }
        // 筛查学生数
        screeningPlanService.updateStudentNumbers(currentUser.getId(), plan.getId(), screeningPlanSchoolStudentService.getCountByScreeningPlanId(plan.getId()));
    }

    /**
     * 多端管理生成虚拟学生
     *
     * @param studentTotal    需要生成的学生数
     * @param school          学校
     * @param schoolGrade     年级
     * @param schoolClass     班级
     * @param mockStudentList 多端管理学生列表
     * @param currentUser     登录用户
     */
    private void mockStudent(Integer studentTotal, School school,
                             MockStudentRequestDTO.GradeItem schoolGrade, MockStudentRequestDTO.ClassItem schoolClass,
                             List<Student> mockStudentList, CurrentUser currentUser) {
        for (int i = 0; i < studentTotal; i++) {
            Student student = new Student();
            student.setSchoolNo(school.getSchoolNo());
            student.setSchoolId(school.getId());
            student.setCreateUserId(currentUser.getId());
            student.setGradeId(schoolGrade.getGradeId());
            student.setClassId(schoolClass.getClassId());
            student.setName(String.valueOf(ScreeningCodeGenerator.nextId()));
            student.setGender(GenderEnum.MALE.type);
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByName(schoolGrade.getGradeName());
            Date date = getDateByGrade(gradeCodeEnum);
            student.setGradeType(gradeCodeEnum.getType());
            student.setBirthday(date);
            mockStudentList.add(student);
        }
        studentService.saveOrUpdateBatch(mockStudentList);
    }

    /**
     * 生成计划学生数据
     *
     * @param studentTotal        需要生成的学生数
     * @param school              学校
     * @param plan                计划
     * @param schoolGrade         年级
     * @param schoolClass         班级
     * @param mockStudentList     多端管理学生列表
     * @param mockPlanStudentList 计划学生
     */
    private void mockPlanStudent(Integer studentTotal, School school, ScreeningPlan plan,
                                 MockStudentRequestDTO.GradeItem schoolGrade, MockStudentRequestDTO.ClassItem schoolClass,
                                 List<Student> mockStudentList, List<ScreeningPlanSchoolStudent> mockPlanStudentList) {
        for (int i = 0; i < studentTotal; i++) {
            ScreeningPlanSchoolStudent planSchoolStudent = new ScreeningPlanSchoolStudent();
            planSchoolStudent.setSrcScreeningNoticeId(plan.getSrcScreeningNoticeId());
            planSchoolStudent.setScreeningTaskId(plan.getScreeningTaskId());
            planSchoolStudent.setScreeningPlanId(plan.getId());
            planSchoolStudent.setScreeningOrgId(plan.getScreeningOrgId());
            planSchoolStudent.setPlanDistrictId(plan.getDistrictId());
            planSchoolStudent.setSchoolDistrictId(school.getDistrictId());
            planSchoolStudent.setSchoolId(school.getId());
            planSchoolStudent.setSchoolNo(school.getSchoolNo());
            planSchoolStudent.setSchoolName(school.getName());
            planSchoolStudent.setGradeId(schoolGrade.getGradeId());
            planSchoolStudent.setGradeName(schoolGrade.getGradeName());
            planSchoolStudent.setClassId(schoolClass.getClassId());
            planSchoolStudent.setClassName(schoolClass.getClassName());
            planSchoolStudent.setStudentId(mockStudentList.get(i).getId());
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByName(schoolGrade.getGradeName());
            planSchoolStudent.setGradeType(gradeCodeEnum.getType());
            Date date = getDateByGrade(gradeCodeEnum);
            planSchoolStudent.setBirthday(date);
            planSchoolStudent.setGender(GenderEnum.MALE.type);
            planSchoolStudent.setStudentAge(DateUtil.ageOfNow(date));
            planSchoolStudent.setStudentName(mockStudentList.get(i).getName());
            planSchoolStudent.setArtificial(1);
            planSchoolStudent.setScreeningCode(Long.valueOf(mockStudentList.get(i).getName()));
            mockPlanStudentList.add(planSchoolStudent);
        }
        screeningPlanSchoolStudentService.batchUpdateOrSave(mockPlanStudentList);
    }

    /**
     * 获取筛查学生列表
     *
     * @param requestDTO 请求入参
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getListByRequest(PlanStudentRequestDTO requestDTO) {
        return screeningPlanSchoolStudentService.getByEntity(new ScreeningPlanSchoolStudent()
                .setScreeningPlanId(requestDTO.getScreeningPlanId())
                .setScreeningOrgId(requestDTO.getScreeningOrgId())
                .setSchoolId(requestDTO.getSchoolId())
                .setGradeId(requestDTO.getGradeId())
                .setClassId(requestDTO.getClassId()));
    }

    /**
     * 通过班级类型获取生日
     *
     * @param gradeCodeEnum 年级编码枚举类
     * @return 生日
     */
    private Date getDateByGrade(GradeCodeEnum gradeCodeEnum) {
        // 幼儿园
        if (SchoolAge.KINDERGARTEN.code.equals(gradeCodeEnum.getType())) {
            return DateFormatUtil.parse("2017-1-1", DateFormatUtil.FORMAT_ONLY_DATE);
        }
        // 中小学
        return DateFormatUtil.parse("2010-1-1", DateFormatUtil.FORMAT_ONLY_DATE);
    }
}
