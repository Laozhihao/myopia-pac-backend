package com.wupol.myopia.business.core.school.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.QrCodeCacheKey;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.ParentStudentDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentCountDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentQueryDTO;
import com.wupol.myopia.business.core.school.domain.mapper.StudentMapper;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生服务
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class StudentService extends BaseService<StudentMapper, Student> {

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ResourceFileService resourceFileService;

    /**
     * 根据学生id列表获取学生信息
     *
     * @param ids id列表
     * @return List<Student>
     */
    public List<Student> getByIds(List<Integer> ids) {
        return baseMapper.selectBatchIds(ids);
    }

    /**
     * 通过年级id查找学生
     *
     * @param gradeId 年级Id
     * @return 学生列表
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Student> getStudentsByGradeId(Integer gradeId) {
        return baseMapper.getByGradeIdAndStatus(gradeId, CommonConst.STATUS_NOT_DELETED);
    }

    /**
     * 通过班级id查找学生
     *
     * @param classId 班级Id
     * @return 学生列表
     */
    public List<Student> getStudentsByClassId(Integer classId) {
        return baseMapper.getByClassIdAndStatus(classId, CommonConst.STATUS_NOT_DELETED);
    }

    public Student getByIdCardAndName(String idCard, String name) {
        return baseMapper.getByIdCardAndName(idCard, name);
    }

    public List<Student> getByIdsAndName(List<Integer> ids, String name) {
        return baseMapper.getByIdsAndName(ids, name);
    }

    /**
     * 新增学生
     *
     * @param student 学生实体类
     * @return 新增数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveStudent(Student student) {

        // 设置学龄
        if (null != student.getGradeId()) {
            SchoolGrade grade = schoolGradeService.getById(student.getGradeId());
            student.setGradeType(GradeCodeEnum.getByCode(grade.getGradeCode()).getType());
        }
        // 检查学生身份证是否重复
        if (checkIdCard(student.getIdCard(), null)) {
            throw new BusinessException("学生身份证重复");
        }
        baseMapper.insert(student);
        return student.getId();
    }

    /**
     * 更新绑定家长手机号码
     *
     * @param studentId   学生ID
     * @param parentPhone 家长手机号码
     */
    @Transactional
    public void updateMpParentPhone(Integer studentId, String parentPhone) {
        Student student = getById(studentId);
        String parentPhoneStr = student.getMpParentPhone();
        if (StringUtils.isBlank(parentPhoneStr)) {
            // 为空新增
            student.setMpParentPhone(parentPhone);
        } else {
            // 家长手机号码是否已经存在
            if (StringUtils.countMatches(parentPhoneStr, parentPhone) == 0) {
                // 不存在拼接家长手机号码
                student.setMpParentPhone(parentPhoneStr + "," + parentPhone);
            }
        }
        updateById(student);
    }

    /**
     * 通过条件查询
     *
     * @param query StudentQueryDTO
     * @return List<Student>
     */
    public List<Student> getBy(StudentQueryDTO query) {
        return baseMapper.getBy(query);
    }

    /**
     * 条件过滤
     *
     * @param gradeIdsStr     年级ID字符串
     * @param visionLabelsStr 视力标签字符串
     * @return {@link TwoTuple} <p>TwoTuple.getFirst-年级list, TwoTuple.getSecond-视力标签list</p>
     */
    public TwoTuple<List<Integer>, List<Integer>> conditionalFilter(String gradeIdsStr,
                                                                    String visionLabelsStr) {
        TwoTuple<List<Integer>, List<Integer>> result = new TwoTuple<>();

        // 年级条件
        if (StringUtils.isNotBlank(gradeIdsStr)) {
            result.setFirst(Arrays.stream(gradeIdsStr.split(","))
                    .map(Integer::valueOf).collect(Collectors.toList()));
        }

        // 视力标签条件
        if (StringUtils.isNotBlank(visionLabelsStr)) {
            result.setSecond(Arrays.stream(visionLabelsStr.split(","))
                    .map(Integer::valueOf).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<Student> getByPage(Page<?> page, StudentQueryDTO query) {
        return baseMapper.getByPage(page, query);
    }

    /**
     * 通过id获取学生信息
     *
     * @param id 学生ID
     * @return StudentDTO
     */
    public StudentDTO getStudentById(Integer id) {
        StudentDTO student = baseMapper.getStudentById(id);

        if (StringUtils.isNotBlank(student.getSchoolNo())) {
            // 学校编号不为空，则拼接学校信息
            School school = schoolService.getBySchoolNo(student.getSchoolNo());
            student.setSchoolId(school.getId());
            student.setSchoolNo(school.getSchoolNo());
            student.setSchoolName(school.getName());
            if (null != student.getClassId() && null != student.getGradeId()) {
                SchoolGrade schoolGrade = schoolGradeService.getById(student.getGradeId());
                SchoolClass schoolClass = schoolClassService.getById(student.getClassId());
                student.setClassName(schoolClass.getName());
                student.setGradeName(schoolGrade.getName());
            }
        }
        return student;
    }

    /**
     * 通过学校ID、班级ID、年级ID查找学生
     *
     * @param schoolId 学校Id
     * @return 学生列表
     */
    public List<StudentDTO> getBySchoolIdAndGradeIdAndClassId(Integer schoolId, Integer classId, Integer gradeId) {
        return baseMapper.getByOtherId(schoolId, classId, gradeId);
    }

    /**
     * 统计学生人数
     *
     * @return List<StudentCountDTO>
     */
    public List<StudentCountDTO> countStudentBySchoolNo() {
        return baseMapper.countStudentBySchoolNo();
    }


    /**
     * 检查学生身份证号码是否重复
     *
     * @param IdCard 身份证号码
     * @param id     学生ID
     * @return 是否重复
     */
    public Boolean checkIdCard(String IdCard, Integer id) {
        return baseMapper.getByIdCardNeIdAndStatus(IdCard, id, CommonConst.STATUS_NOT_DELETED).size() > 0;
    }

    /**
     * 根据身份证列表获取学生
     *
     * @param idCardList 身份证list
     * @return List<Student>
     */
    public List<Student> getByIdCards(List<String> idCardList) {
        StudentQueryDTO StudentQueryDTO = new StudentQueryDTO();
        return Lists.partition(idCardList, 50).stream().map(list -> {
            StudentQueryDTO.setIdCardList(list);
            return baseMapper.getBy(StudentQueryDTO);
        }).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * 批量检查学生身份证号码是否重复
     *
     * @param IdCards 身份证号码
     * @return 是否重复
     */
    public Boolean checkIdCards(List<String> IdCards) {
        return baseMapper.getByIdCardsAndStatus(IdCards, CommonConst.STATUS_NOT_DELETED).size() > 0;
    }

    /**
     * 通过身份证查找学生
     *
     * @param idCard 身份证
     * @return Student
     */
    public Student getByIdCard(String idCard) {
        return baseMapper.getByIdCard(idCard);
    }

    /**
     * 根据区域层级Id获取其学校的所有学生数据
     *
     * @param districtIds 行政区域id
     * @return List<StudentDTO>
     */
    public List<StudentDTO> getStudentsBySchoolDistrictIds(List<Integer> districtIds) {
        if (CollectionUtils.isEmpty(districtIds)) {
            return Collections.emptyList();
        }
        return baseMapper.selectBySchoolDistrictIds(districtIds);
    }

    /**
     * 获取学生列表
     *
     * @param pageRequest       分页条件
     * @param studentQueryDTO   查询条件
     * @param conditionalFilter 过滤条件
     * @return IPage<StudentDTO>
     */
    public IPage<StudentDTO> getStudentListByCondition(PageRequest pageRequest, StudentQueryDTO studentQueryDTO,
                                                       TwoTuple<List<Integer>, List<Integer>> conditionalFilter) {
        return baseMapper.getStudentListByCondition(pageRequest.toPage(),
                studentQueryDTO.getSno(), studentQueryDTO.getIdCard(), studentQueryDTO.getName(),
                studentQueryDTO.getParentPhone(), studentQueryDTO.getGender(), conditionalFilter.getFirst(),
                conditionalFilter.getSecond(), studentQueryDTO.getStartScreeningTime(), studentQueryDTO.getEndScreeningTime(),
                studentQueryDTO.getSchoolName());
    }

    /**
     * 解析家长端的token，获取学生ID
     *
     * @param token token
     * @return 学生ID
     */
    public Integer parseToken2StudentId(String token) {
        String key = String.format(QrCodeCacheKey.PARENT_STUDENT_QR_CODE, token);
        Integer studentId = (Integer) redisUtil.get(key);
        if (Objects.isNull(studentId)) {
            throw new BusinessException("学生二维码已经失效！");
        }
        return studentId;
    }

    /**
     * 统计学生信息
     *
     * @param studentIds 学生ids
     * @return List<ParentStudentDTO>
     */
    public List<ParentStudentDTO> countParentStudent(List<Integer> studentIds) {
        return baseMapper.countParentStudent(studentIds);
    }

    /**
     * 更新学生
     *
     * @param student 学生实体
     * @return 学生实体
     */
    public StudentDTO updateStudent(Student student) {
        // 设置学龄
        if (null != student.getGradeId()) {
            SchoolGrade grade = schoolGradeService.getById(student.getGradeId());
            student.setGradeType(GradeCodeEnum.getByCode(grade.getGradeCode()).getType());
        }

        // 检查学生身份证是否重复
        if (checkIdCard(student.getIdCard(), student.getId())) {
            throw new BusinessException("学生身份证重复");
        }

        // 更新学生
        baseMapper.updateById(student);
        // 查询信息
        StudentDTO resultStudent = baseMapper.getStudentById(student.getId());
        if (StringUtils.isNotBlank(resultStudent.getSchoolNo())) {
            School school = schoolService.getBySchoolNo(resultStudent.getSchoolNo());
            resultStudent.setSchoolName(school.getName());
            resultStudent.setSchoolId(school.getId());

            // 查询年级和班级
            SchoolGrade schoolGrade = schoolGradeService.getById(resultStudent.getGradeId());
            SchoolClass schoolClass = schoolClassService.getById(resultStudent.getClassId());
            resultStudent.setGradeName(schoolGrade.getName()).setClassName(schoolClass.getName());
        }
        if (null != resultStudent.getAvatarFileId()) {
            resultStudent.setAvatar(resourceFileService.getResourcePath(resultStudent.getAvatarFileId()));
        }
        return resultStudent;
    }
}