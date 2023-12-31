package com.wupol.myopia.business.core.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DigitUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassExportDTO;
import com.wupol.myopia.business.core.school.domain.mapper.SchoolClassMapper;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.Student;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校-班级Service
 *
 * @author Simple4H
 */
@Service
public class SchoolClassService extends BaseService<SchoolClassMapper, SchoolClass> {

    @Resource
    private StudentService studentService;

    /**
     * 新增教室
     *
     * @param schoolClass 教室实体类
     * @return 新增数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveClass(SchoolClass schoolClass) {
        List<SchoolClass> schoolClasses = baseMapper.getByNameNeId(schoolClass.getName(), null,
                schoolClass.getGradeId(), schoolClass.getSchoolId());
        if (!CollectionUtils.isEmpty(schoolClasses)) {
            throw new BusinessException("班级名称重复");
        }
        baseMapper.insert(schoolClass);
        return schoolClass.getId();
    }

    /**
     * 删除年级
     *
     * @param classId      年级id
     * @param createUserId 创建人id
     * @return 成功数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedClass(Integer classId, Integer createUserId) {

        // 判断是否给学生使用
        List<Student> students = studentService.getStudentsByClassId(classId);
        if (!students.isEmpty()) {
            throw new BusinessException("当前班级被学生依赖，不能删除");
        }

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(classId);
        schoolClass.setCreateUserId(createUserId);
        schoolClass.setStatus(CommonConst.STATUS_IS_DELETED);
        return baseMapper.updateById(schoolClass);
    }

    /**
     * 更新班级
     *
     * @param schoolClass 班级实体类
     * @return 班级实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public SchoolClass updateClass(SchoolClass schoolClass) {
        List<SchoolClass> schoolClasses = baseMapper.getByNameNeId(schoolClass.getName(), schoolClass.getId(),
                schoolClass.getGradeId(), schoolClass.getSchoolId());
        if (!CollectionUtils.isEmpty(schoolClasses)) {
            throw new BusinessException("班级名称重复");
        }
        baseMapper.updateById(schoolClass);
        return schoolClass;
    }

    /**
     * 通过年级获取班级
     *
     * @param gradeId 年级id
     * @return 班级列表
     */
    public List<SchoolClass> getByGradeId(Integer gradeId) {
        return baseMapper.getByGradeIdAndStatus(gradeId, CommonConst.STATUS_NOT_DELETED);
    }

    /**
     * 批量通过年级ID和学校ID获取班级
     *
     * @param gradeIds 年级ids
     * @param schoolId 学校id
     * @return 班级列表
     */
    public List<SchoolClassDTO> getByGradeIds(List<Integer> gradeIds, Integer schoolId) {
        return baseMapper.getByGradeIdsAndSchoolIdAndStatus(gradeIds, schoolId, CommonConst.STATUS_NOT_DELETED);
    }

    /**
     * 通过年级ID获取班级信息
     *
     * @param gradeIds 年级ids
     * @return List<SchoolClass>
     */
    public List<SchoolClassExportDTO> getByGradeIds(List<Integer> gradeIds) {
        return baseMapper.getByGradeIds(gradeIds);
    }

    /**
     * 通过年级ID获取班级信息
     *
     * @param gradeIds 年级ID集合
     * @return 班级信息集合
     */
    public List<SchoolClass> listByGradeIds(List<Integer> gradeIds) {
        return baseMapper.selectList(Wrappers.lambdaQuery(SchoolClass.class)
                .in(SchoolClass::getGradeId,gradeIds));
    }

    /**
     * 批量通过id获取实体
     *
     * @param ids ids
     * @return Map<Integer, SchoolClass>
     */
    public Map<Integer, SchoolClass> getClassMapByIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        return baseMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(SchoolClass::getId, Function.identity()));
    }

    /**
     * 批量通过id获取名称
     *
     * @param ids ids
     * @return Map<Integer, String>
     */
    public Map<Integer, String> getClassNameMapByIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Maps.newHashMap();
        }
        List<Integer> distinctIds = ids.stream().distinct().collect(Collectors.toList());
        return baseMapper.selectBatchIds(distinctIds).stream()
                .collect(Collectors.toMap(SchoolClass::getId, SchoolClass::getName));
    }

    public List<SchoolClass> getByIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return baseMapper.selectBatchIds(ids);
    }

    /**
     * 获取班级
     *
     * @param schoolName 学校名称
     * @param gradeName  年级名称
     * @return List<SchoolClass>
     */
    public List<SchoolClass> getBySchoolNameAndGradeName(String schoolName, String gradeName) {
        return baseMapper.getBySchoolNameAndGradeName(schoolName, gradeName);
    }

    /**
     * 根据学校Id获取所有班级
     *
     * @param schoolId 学校ID
     * @return List<SchoolClassDTO>
     */
    public List<SchoolClassDTO> getVoBySchoolId(Integer schoolId) {
        return baseMapper.selectVoList(new SchoolClass().setSchoolId(schoolId));
    }

    /**
     * 根据学校Id获取所有班级
     *
     * @param schoolId 学校ID
     * @return 班级集合
     */
    public List<SchoolClass> listBySchoolId(Integer schoolId){
        return baseMapper.selectList(Wrappers.lambdaQuery(SchoolClass.class)
                .eq(SchoolClass::getSchoolId,schoolId)
                .eq(SchoolClass::getStatus,CommonConst.STATUS_NOT_DELETED));
    }

    /**
     * 根据schoolId 获取班级名
     *
     * @param schoolId  学校ID
     * @param className 班级名称
     * @return SchoolClass
     */
    public SchoolClass getByClassNameAndSchoolId(Integer schoolId, Integer gradeId, String className) {
        if (Objects.isNull(gradeId) || StringUtils.isBlank(className))  {
            return new SchoolClass();
        }
        LambdaQueryWrapper<SchoolClass> queryWrapper = new LambdaQueryWrapper<>();
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setSchoolId(schoolId).setName(className).setGradeId(gradeId).setStatus(CommonConst.STATUS_NOT_DELETED);
        queryWrapper.setEntity(schoolClass);
        return baseMapper.selectOne(queryWrapper);
    }

    public String getClassNameById(Integer id) {
        SchoolClass classById = this.getById(id);
        return Objects.nonNull(classById) ? classById.getName() : "";
    }

    /**
     * 通过名称获取班级
     *
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @param names    名称
     * @return List<SchoolClass>
     */
    public List<SchoolClass> getByGradeIdAndNames(Integer schoolId, Integer gradeId, List<String> names) {
        return baseMapper.getByGradeIdAndNames(schoolId, gradeId, names);
    }

    /**
     * 批量通过id获取实体
     */
    public <T> Map<Integer, SchoolClass> getClassMapByIds(List<T> list, Function<T, Integer> function) {
        List<Integer> classIds = list.stream().map(function).collect(Collectors.toList());
        return getClassMapByIds(classIds);
    }

    /**
     * 批量通过年级ID和学校ID获取班级
     *
     * @param ids 年级ids
     *
     * @return 班级列表
     */
    public List<SchoolClassDTO> getClassDTOByIds(List<Integer> ids) {
        return baseMapper.getByIds(ids);
    }

    /**
     * 排序
     */
    public void sortStatList(List<SchoolClass> classList) {
        try {
            classList.sort(Comparator.comparing(s -> Integer.valueOf(s.getName().substring(0, s.getName().length() - 1))));
            return;
        } catch (Exception ignored) {
        }
        try {
            classList.sort(Comparator.comparing(s -> DigitUtil.chineseNumToArabicNum(s.getName().substring(0, s.getName().length() - 1))));
        } catch (Exception ignored) {
        }
    }

}
