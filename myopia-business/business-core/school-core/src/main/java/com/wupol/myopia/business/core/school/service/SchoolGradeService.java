package com.wupol.myopia.business.core.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.domain.dto.*;
import com.wupol.myopia.business.core.school.domain.mapper.SchoolGradeMapper;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校-年级Service
 *
 * @author Simple4H
 */
@Service
public class SchoolGradeService extends BaseService<SchoolGradeMapper, SchoolGrade> {

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private StudentService studentService;


    /**
     * 新增年级
     *
     * @param schoolGrade 年级实体类
     * @return 新增个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveGrade(SchoolGrade schoolGrade) {
        // 查询code是否存在
        if (countGradeByCode(schoolGrade.getSchoolId(), schoolGrade.getGradeCode()) > 0) {
            throw new BusinessException("该年级已经存在，请确认");
        }
        return baseMapper.insert(schoolGrade);
    }

    /**
     * 删除年级
     *
     * @param id          年级id
     * @param currentUser 当前登录用户
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedGrade(Integer id, CurrentUser currentUser) {

        // 判断年级是否班级使用
        List<SchoolClass> schoolClasses = schoolClassService.getByGradeId(id);
        if (!schoolClasses.isEmpty()) {
            throw new BusinessException("当前年级被班级依赖，不能删除");
        }

        // 判断是否给学生使用
        List<Student> students = studentService.getStudentsByGradeId(id);
        if (!students.isEmpty()) {
            throw new BusinessException("当前年级被学生依赖，不能删除");
        }
        SchoolGrade schoolGrade = new SchoolGrade();
        schoolGrade.setId(id);
        schoolGrade.setCreateUserId(currentUser.getId());
        schoolGrade.setStatus(CommonConst.STATUS_IS_DELETED);
        return baseMapper.updateById(schoolGrade);
    }

    /**
     * 年级列表
     *
     * @param pageRequest 分页请求
     * @param schoolId    学校id
     * @return IPage<SchoolGradeItemsDTO> 返回体
     */
    public IPage<SchoolGradeItemsDTO> getGradeList(PageRequest pageRequest, Integer schoolId) {

        // 获取年级
        IPage<SchoolGradeItemsDTO> schoolGrades = baseMapper.getGradeBySchool(pageRequest.toPage(), schoolId);
        if (schoolGrades.getRecords().isEmpty()) {
            return schoolGrades;
        }
        // 获取班级，并且封装成Map
        Map<Integer, List<SchoolClassDTO>> classMaps = schoolClassService
                .getByGradeIds(schoolGrades
                        .getRecords()
                        .stream()
                        .map(SchoolGradeItemsDTO::getId)
                        .collect(Collectors.toList()), schoolId).stream()
                .collect(Collectors.groupingBy(SchoolClassDTO::getGradeId));

        schoolGrades.getRecords().forEach(g -> g.setChild(classMaps.get(g.getId())));
        return schoolGrades;
    }

    /**
     * 年级列表(没有分页)
     *
     * @param schoolId 学校id
     * @return List<SchoolGradeItemsDTO> 返回体
     */
    public List<SchoolGradeItemsDTO> getAllGradeList(Integer schoolId) {

        // 获取年级
        List<SchoolGradeItemsDTO> schoolGrades = baseMapper.getAllBySchoolId(schoolId);
        Map<Integer, String> gradeMap = schoolGrades.stream().collect(Collectors.toMap(SchoolGradeItemsDTO::getId, SchoolGradeItemsDTO::getName));

        // 获取班级，并且封装成Map
        Map<Integer, List<SchoolClassDTO>> classMaps = schoolClassService.getByGradeIds(schoolGrades.stream()
                        .map(SchoolGradeItemsDTO::getId).collect(Collectors.toList()), schoolId).stream().peek(schoolClass -> {
                    schoolClass.setUniqueId(UUID.randomUUID().toString());
                    schoolClass.setGradeName(gradeMap.get(schoolClass.getGradeId()));
                })
                .collect(Collectors.groupingBy(SchoolClassDTO::getGradeId));
        schoolGrades.forEach(g -> {
            g.setChild(classMaps.get(g.getId()));
            g.setUniqueId(UUID.randomUUID().toString());
        });
        return schoolGrades;
    }

    /**
     * 更新年级
     *
     * @param schoolGrade 年级实体类
     * @return SchoolGrade 年级实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public SchoolGrade updateGrade(SchoolGrade schoolGrade) {
        // 查询code是否存在
        if (countGradeByCode(schoolGrade.getSchoolId(), schoolGrade.getGradeCode()) > 0) {
            throw new BusinessException("该年级已经存在，请确认");
        }
        baseMapper.updateById(schoolGrade);
        return baseMapper.selectById(schoolGrade.getId());
    }

    /**
     * 根据id列表查询
     */
    public List<SchoolGrade> getByIds(List<Integer> ids) {
        return baseMapper.getByIds(ids);
    }

    /**
     * 批量通过id获取实体
     *
     * @param ids ids
     * @return Map<Integer, SchoolGrade>
     */
    public Map<Integer, SchoolGrade> getGradeMapByIds(List<Integer> ids) {
        return getByIds(ids).stream()
                .collect(Collectors.toMap(SchoolGrade::getId, Function.identity()));
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
                .collect(Collectors.toMap(SchoolGrade::getId, SchoolGrade::getName));
    }

    /**
     * 通过code统计
     *
     * @param schoolId 学校ID
     * @param code     年级code
     * @return 统计
     */
    public Integer countGradeByCode(Integer schoolId, String code) {
        return baseMapper.countBySchoolIdAndCode(schoolId, code);
    }

    /**
     * 根据学校名称获取年级
     *
     * @param schoolName 学校名称
     * @return 年级列表
     */
    public List<SchoolGrade> getBySchoolName(String schoolName) {
        SchoolQueryDTO schoolQueryDTO = new SchoolQueryDTO();
        schoolQueryDTO.setName(schoolName);
        Integer schoolId = schoolService.getBy(schoolQueryDTO).stream()
                .findFirst().orElseThrow(() -> new BusinessException("未找到该学校")).getId();
        SchoolGradeQueryDTO schoolGradeQueryDTO = new SchoolGradeQueryDTO();
        schoolGradeQueryDTO.setSchoolId(schoolId);
        return getBy(schoolGradeQueryDTO);
    }

    /**
     * 查询学校年级
     *
     * @param query 查询条件
     * @return List<SchoolGrade>
     */
    public List<SchoolGrade> getBy(SchoolGradeQueryDTO query) {
        return baseMapper.getByQuery(query);
    }


    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<SchoolGrade> getByPage(Page<?> page, SchoolGradeQueryDTO query) {
        return baseMapper.getByPage(page, query);
    }

    /**
     * 通过学校id获取年级
     *
     * @param schoolIds 学校ID
     * @return List<SchoolGrade>
     */
    public List<SchoolGradeExportDTO> getBySchoolIds(List<Integer> schoolIds) {
        return baseMapper.getBySchoolIds(schoolIds);
    }

    /**
     * 根据学校Id获取所有年级
     *
     * @param schoolId 学校ID
     * @return List<SchoolGrade>
     */
    public List<SchoolGrade> getBySchoolId(Integer schoolId) {
        return baseMapper.getBySchoolId(schoolId);
    }

    /**
     * 根据schoolId获取年级名
     *
     * @param schoolId  学校ID
     * @param gradeName 年级名称
     * @return SchoolGrade
     */
    public SchoolGrade getByGradeNameAndSchoolId(Integer schoolId, String gradeName) {
        LambdaQueryWrapper<SchoolGrade> schoolGradeExportDTOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        SchoolGrade schoolGrade = new SchoolGrade();
        schoolGrade.setSchoolId(schoolId).setName(gradeName).setStatus(CommonConst.STATUS_NOT_DELETED);
        schoolGradeExportDTOLambdaQueryWrapper.setEntity(schoolGrade);
        return baseMapper.selectOne(schoolGradeExportDTOLambdaQueryWrapper);
    }

    public String getGradeNameById(Integer id) {
        SchoolGrade grade = this.getById(id);
        return Objects.nonNull(grade) ? grade.getName() : "";
    }

    /**
     * 封装年级班级信息
     *
     * @param grades 年级列表
     */
    public void packageGradeInfo(List<SchoolGradeExportDTO> grades) {
        List<Integer> gradeIds = grades.stream().map(SchoolGradeExportDTO::getId).collect(Collectors.toList());
        // 班级统计
        List<SchoolClassExportDTO> classes = schoolClassService.getByGradeIds(gradeIds);
        // 通过班级id分组
        Map<Integer, List<SchoolClassExportDTO>> classMaps = classes.stream().collect(Collectors.groupingBy(SchoolClassExportDTO::getGradeId));
        // 年级设置班级
        grades.forEach(g -> g.setChild(classMaps.get(g.getId())));
    }

}
