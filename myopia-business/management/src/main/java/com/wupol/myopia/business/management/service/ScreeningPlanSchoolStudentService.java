package com.wupol.myopia.business.management.service;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.management.constant.NationEnum;
import com.wupol.myopia.business.management.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.mapper.ScreeningPlanSchoolStudentMapper;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.domain.vo.SchoolGradeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ScreeningPlanSchoolStudentService extends BaseService<ScreeningPlanSchoolStudentMapper, ScreeningPlanSchoolStudent> {

    @Autowired
    private StudentService studentService;

    /**
     * 根据学生id获取筛查计划学校学生
     *
     * @param studentId 学生ID
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getByStudentId(Integer studentId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningPlanSchoolStudent>().eq("student_id", studentId));
    }

    /**
     * 删除筛查计划中，除了指定学校ID的其它学校学生信息
     *
     * @param screeningPlanId
     * @param excludeSchoolIds
     */
    public void deleteByPlanIdAndExcludeSchoolIds(Integer screeningPlanId, List<Integer> excludeSchoolIds) {
        Assert.notNull(screeningPlanId, "筛查计划ID不能为空");
        QueryWrapper<ScreeningPlanSchoolStudent> query = new QueryWrapper<ScreeningPlanSchoolStudent>().eq("screening_plan_id", screeningPlanId);
        if (!CollectionUtils.isEmpty(excludeSchoolIds)) {
            query.notIn("school_id", excludeSchoolIds);
        }
        baseMapper.delete(query);
    }

    /**
     * 根据计划ID获取所有筛查学生
     *
     * @param screeningPlanId
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByScreeningPlanId(Integer screeningPlanId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningPlanSchoolStudent>().eq("screening_plan_id", screeningPlanId));
    }

    /**
     * 根据计划ID获取学校ID的学生数Map
     *
     * @param screeningPlanId
     * @return
     */
    public Map<Integer, Long> getSchoolStudentCountByScreeningPlanId(Integer screeningPlanId) {
        return getByScreeningPlanId(screeningPlanId).stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolId, Collectors.counting()));
    }

    /**
     * 获取计划中的学校年级情况
     *
     * @param screeningPlanId
     * @param schoolId
     * @return
     */
    public List<SchoolGradeVo> getSchoolGradeVoByPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        List<GradeClassesDTO> gradeClasses = baseMapper.selectSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId);
        List<SchoolGradeVo> schoolGradeVos = new ArrayList<>();
        Map<Integer, List<GradeClassesDTO>> graderIdClasses = gradeClasses.stream().collect(Collectors.groupingBy(GradeClassesDTO::getGradeId));
        graderIdClasses.keySet().forEach(gradeId -> {
            SchoolGradeVo vo = new SchoolGradeVo();
            List<GradeClassesDTO> gradeClassesDTOS = graderIdClasses.get(gradeId);
            vo.setId(gradeId).setName(gradeClassesDTOS.get(0).getGradeName());
            vo.setClasses(gradeClassesDTOS.stream().map(dto -> {
                SchoolClass schoolClass = new SchoolClass();
                schoolClass.setId(dto.getClassId()).setName(dto.getClassName());
                return schoolClass;
            }).collect(Collectors.toList()));
            schoolGradeVos.add(vo);
        });
        return schoolGradeVos;
    }

    /**
     * 分页获取筛查计划的学校学生数据
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<StudentDTO> getPage(StudentQuery query, PageRequest pageRequest) {
        Assert.notNull(query.getScreeningPlanId(), "筛查计划ID不能为空");
        Assert.notNull(query.getSchoolId(), "筛查学校ID不能为空");
        Page<StudentDTO> page = (Page<StudentDTO>) pageRequest.toPage();
        IPage<StudentDTO> studentDTOIPage = baseMapper.selectPageByQuery(page, query);
        studentDTOIPage.getRecords().forEach(studentDTO -> studentDTO.setNationDesc(NationEnum.getName(studentDTO.getNation())));
        return studentDTOIPage;
    }

    /**
     * 根据身份证号获取筛查学生
     * @param screeningPlanId
     * @param schoolId
     * @param idCardList
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByIdCards(Integer screeningPlanId, Integer schoolId, List<String> idCardList) {
        return Lists.partition(idCardList, 50).stream().map(list -> baseMapper.selectByIdCards(screeningPlanId, schoolId, list)).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * 处理上传的筛查学生数据
     *
     * @param listMap
     */
    public void insertByUpload(Integer userId, List<Map<Integer, String>> listMap, Integer screeningPlanId, Integer schoolId) {
        // 获取所有身份证号
        List<String> idCardList = listMap.stream().map(item -> item.getOrDefault(8, null)).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        // 身份证号是否符合规则
        if (!idCardList.stream().allMatch(RegularUtils::isIdCard)) {
            throw new BusinessException("存在不正确的身份证号");
        }
        // 根据身份证号分批获取学生
        Map<String, Student> idCardExistStudents = studentService.getByIdCards(idCardList).stream().collect(Collectors.toMap(Student::getIdCard, Function.identity()));
        // 根据身份证号分批获取已有的筛查学生数据
        Map<String, ScreeningPlanSchoolStudent> idCardExistScreeningStudents = getByIdCards(screeningPlanId, schoolId, idCardList).stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getIdCard, Function.identity()));
        // excel格式：序号、姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、年级、班级、学号、身份证号、手机号码、居住地址
        List<Student> needUpdateStudents = new ArrayList<>();
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = listMap.stream()
                .map(item -> {
                    if (false) {
                        // 需要更新的学生
                        needUpdateStudents.add(idCardExistStudents.get(item.get(8)));
                    }
                    return new ScreeningPlanSchoolStudent()
                            .setScreeningPlanId(screeningPlanId)
                            .setSchoolId(schoolId)
                            .setSchoolName(item.get(3));
                }).collect(Collectors.toList());
        // 批量新增, 并设置返回的userId
        studentService.saveOrUpdateBatch(needUpdateStudents);
        //TODO 已有处理
        saveBatch(screeningPlanSchoolStudents);
    }
}
