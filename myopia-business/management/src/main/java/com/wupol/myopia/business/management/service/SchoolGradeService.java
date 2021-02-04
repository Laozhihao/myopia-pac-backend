package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.SchoolGradeItems;
import com.wupol.myopia.business.management.domain.mapper.SchoolGradeMapper;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.SchoolGradeQuery;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import com.wupol.myopia.business.management.domain.vo.SchoolGradeExportVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-22
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
     * @param id 年级id
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedGrade(Integer id) {

        // 判断年级是否班级使用
        List<SchoolClass> schoolClasses = schoolClassService.getSchoolClassByGradeId(id);
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
        schoolGrade.setCreateUserId(1);
        schoolGrade.setStatus(CommonConst.STATUS_IS_DELETED);
        return baseMapper.updateById(schoolGrade);
    }

    /**
     * 年级列表
     *
     * @param pageRequest 分页请求
     * @param schoolId    学校id
     * @return IPage<SchoolGradeItems> 返回体
     */
    public IPage<SchoolGradeItems> getGradeList(PageRequest pageRequest, Integer schoolId) {

        // 获取年级
        IPage<SchoolGradeItems> schoolGrades = baseMapper.getGradeBySchool(pageRequest.toPage(), schoolId);
        if (schoolGrades.getRecords().isEmpty()) {
            return schoolGrades;
        }
        // 获取班级，并且封装成Map
        Map<Integer, List<SchoolClass>> classMaps = schoolClassService
                .getSchoolClassByGradeIds(schoolGrades
                        .getRecords()
                        .stream()
                        .map(SchoolGradeItems::getId)
                        .collect(Collectors.toList()), schoolId).stream()
                .collect(Collectors.groupingBy(SchoolClass::getGradeId));

        schoolGrades.getRecords().forEach(g -> g.setChild(classMaps.get(g.getId())));
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
     * 通过code统计
     *
     * @param schoolId 学校ID
     * @param code     年级code
     * @return 统计
     */
    public Integer countGradeByCode(Integer schoolId, String code) {
        return baseMapper.selectCount(new QueryWrapper<SchoolGrade>()
                .eq("school_id", schoolId)
                .eq("grade_code", code)
                .eq("status", CommonConst.STATUS_NOT_DELETED));
    }

    /**
     * 根据学校铝箔获取年级
     */
    public List<SchoolGrade> getBySchoolName(String schoolName, Integer deptId) {
        SchoolQuery schoolQuery = new SchoolQuery();
        schoolQuery.setName(schoolName).setGovDeptId(deptId);
        Integer schoolId = schoolService.getBy(schoolQuery).stream()
                .findFirst().orElseThrow(() -> new BusinessException("未找到该学校")).getId();
        SchoolGradeQuery schoolGradeQuery = new SchoolGradeQuery();
        schoolGradeQuery.setSchoolId(schoolId);
        return getBy(schoolGradeQuery);
    }

    /**
     * 查询学校年级
     *
     * @param query 查询条件
     * @return
     */
    public List<SchoolGrade> getBy(SchoolGradeQuery query) {
        return baseMapper.getBy(query);
    }

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<SchoolGrade> getByPage(Page<?> page, SchoolGradeQuery query) {
        return baseMapper.getByPage(page, query);
    }

    /**
     * 通过学校id获取年级
     *
     * @param schoolIds 学校ID
     * @return List<SchoolGrade>
     */
    public List<SchoolGradeExportVO> getBySchoolIds(List<Integer> schoolIds) {
        return baseMapper.getBySchoolIds(schoolIds);
    }

    public List<Integer> batchInsertGrade( ) {
        return null;
    }
}
