package com.wupol.myopia.business.core.screening.flow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.constant.ContrastTypeEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.ScreeningPlanSchoolStudentMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ScreeningPlanSchoolStudentService extends BaseService<ScreeningPlanSchoolStudentMapper, ScreeningPlanSchoolStudent> {

    @Autowired
    private ScreeningPlanService screeningPlanService;

    /**
     * 根据学生id获取筛查计划学校学生
     *
     * @param studentId 学生ID
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getByStudentId(Integer studentId) {
        return baseMapper.findByStudentId(studentId);
    }

    /**
     * 批量查找数据
     *
     * @param screeningResultSearchDTO
     * @return
     */
    public List<StudentScreeningInfoWithResultDTO> getStudentInfoWithResult(ScreeningResultSearchDTO screeningResultSearchDTO) {
        return baseMapper.selectStudentInfoWithResult(screeningResultSearchDTO);
    }

    /**
     * 根据学校ID和筛查机构ID获取计划的学生
     *
     * @param schoolId 学校ID
     * @param deptId 筛查机构ID
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getCurrentPlanStudentByOrgIdAndSchoolId(Integer schoolId, Integer deptId) {
        if (deptId == null) {
            throw new ManagementUncheckedException("deptId 不能为空");
        }
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(deptId);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningOrgId, deptId).eq(ScreeningPlanSchoolStudent::getSchoolId, schoolId).in(ScreeningPlanSchoolStudent::getScreeningPlanId, currentPlanIds);
        return baseMapper.selectList(queryWrapper);
    }

    public List<ScreeningPlanSchoolStudent> getCurrentPlanStudentByGradeIdAndScreeningOrgId(Integer gradeId, Integer screeningOrgId) {
        if (screeningOrgId == null) {
            throw new ManagementUncheckedException("screeningOrgId 不能为空");
        }
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(screeningOrgId);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningOrgId, screeningOrgId).in(ScreeningPlanSchoolStudent::getScreeningPlanId, currentPlanIds).eq(ScreeningPlanSchoolStudent::getGradeId, gradeId);
        return baseMapper.selectList(queryWrapper);
    }


    /**
     * 删除筛查计划中，除了指定学校ID的其它学校学生信息
     *
     * @param screeningPlanId
     * @param excludeSchoolIds
     */
    public void deleteByPlanIdAndExcludeSchoolIds(Integer screeningPlanId, List<Integer> excludeSchoolIds) {
        Assert.notNull(screeningPlanId, "筛查计划ID不能为空");
        baseMapper.deleteByPlanIdAndExcludeSchoolIds(screeningPlanId, excludeSchoolIds);
    }

    /**
     * 根据计划ID获取所有筛查学生
     *
     * @param screeningPlanId
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByScreeningPlanId(Integer screeningPlanId) {
        return baseMapper.findByPlanId(screeningPlanId);
    }

    /**
     * 根据计划ID和学校ID获取所有筛查学生
     *
     * @param screeningPlanId
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByScreeningPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        return baseMapper.findByPlanIdAndSchoolId(screeningPlanId, schoolId);
    }

    /**
     * 根据计划ID获取所有筛查学生数量
     *
     * @param screeningPlanId
     * @return
     */
    public Integer getCountByScreeningPlanId(Integer screeningPlanId) {
        return baseMapper.countByPlanId(screeningPlanId);
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
     * 获取学校筛查学生数
     *
     * @param srcScreeningNoticeId 通知ID
     * @param schoolId             学校ID
     * @return
     */
    public Integer countPlanSchoolStudent(int srcScreeningNoticeId, int schoolId) {
        return baseMapper.countBySchoolIdAndNoticeId(schoolId, srcScreeningNoticeId);
    }

    public List<GradeClassesDTO> selectSchoolGradeVoByPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        return baseMapper.selectSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId);
    }

    public IPage<ScreeningStudentDTO> selectPageByQuery(Page<ScreeningStudentDTO> page, ScreeningStudentQueryDTO query) {
        return baseMapper.selectPageByQuery(page, query);
    }

    /**
     * 根据身份证号获取筛查学生
     *
     * @param screeningPlanId
     * @param schoolId
     * @param idCardList
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByIdCards(Integer screeningPlanId, Integer schoolId, List<String> idCardList) {
        return Lists.partition(idCardList, 50).stream().map(list -> baseMapper.selectByIdCards(screeningPlanId, schoolId, list)).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * 根据年级班级ID获取筛查学生
     *
     * @param gradeId
     * @param classId
     * @return
     */
    public List<ScreeningStudentDTO> getByGradeAndClass(Integer screeningPlanId, Integer gradeId, Integer classId) {
        return baseMapper.selectByGradeAndClass(screeningPlanId, gradeId, classId);
    }

    /**
     * 根据查询条件获取当前进行中的计划的学生
     *
     * @param screeningStudentQuery 查询条件
     * @param page 页码
     * @param size 条数
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent>
     **/
    public IPage<ScreeningPlanSchoolStudent> getCurrentPlanScreeningStudentList(ScreeningStudentQueryDTO screeningStudentQuery, Integer page, Integer size) {
        // 获取当前计划
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(screeningStudentQuery.getScreeningOrgId());
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new Page<>(page, size);
        }
        screeningStudentQuery.setPlanIds(new ArrayList<>(currentPlanIds));
        return selectPlanStudentListByPage(page, size, screeningStudentQuery);
    }

    /**
     * 根据实体条件查询
     *
     * @param screeningPlanSchoolStudent 查询条件
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent>
     **/
    public List<ScreeningPlanSchoolStudent> listByEntityDescByCreateTime(ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        // 获取当前计划
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(screeningPlanSchoolStudent.getScreeningOrgId());
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return Collections.emptyList();
        }
        // 查询学生
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.setEntity(screeningPlanSchoolStudent).in(ScreeningPlanSchoolStudent::getScreeningPlanId, currentPlanIds).orderByDesc(ScreeningPlanSchoolStudent::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据筛查通知Id获取筛查学校所在层级的计划筛查学生总数
     *
     * @param screeningNoticeId
     * @return
     */
    public Map<Integer, Long> getDistrictPlanStudentCountBySrcScreeningNoticeId(Integer screeningNoticeId) {
        List<ScreeningPlanSchoolStudent> results =
                this.getPlanStudentCountByScreeningItemId(screeningNoticeId, ContrastTypeEnum.NOTIFICATION);
        if (CollectionUtils.isEmpty(results)) {
            return Collections.emptyMap();
        }
        return results.stream().collect(
                Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolDistrictId, Collectors.counting()));
    }

    /**
     * 根据筛查任务Id获取筛查学校所在层级的计划筛查学生总数
     *
     * @param taskId
     * @return
     */
    public Map<Integer, Long> getDistrictPlanStudentCountByScreeningTaskId(Integer taskId) {
        List<ScreeningPlanSchoolStudent> results =
                this.getPlanStudentCountByScreeningItemId(taskId, ContrastTypeEnum.TASK);
        if (CollectionUtils.isEmpty(results)) {
            return Collections.emptyMap();
        }
        return results.stream().collect(
                Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolDistrictId, Collectors.counting()));
    }

    /**
     * 根据筛查计划Id获取筛查学校所在层级的计划筛查学生总数
     *
     * @param planId
     * @return
     */
    public Map<Integer, Long> getDistrictPlanStudentCountByScreeningPlanId(Integer planId) {
        List<ScreeningPlanSchoolStudent> results =
                this.getPlanStudentCountByScreeningItemId(planId, ContrastTypeEnum.PLAN);
        if (CollectionUtils.isEmpty(results)) {
            return Collections.emptyMap();
        }
        return results.stream().collect(
                Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolDistrictId, Collectors.counting()));
    }

    /**
     * 根据通知、任务或者计划获取计划筛查学生记录
     *
     * @param itemId
     * @param itemType
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getPlanStudentCountByScreeningItemId(
            Integer itemId, ContrastTypeEnum itemType) {
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        switch (itemType) {
            case NOTIFICATION:
                lambdaQueryWrapper.eq(ScreeningPlanSchoolStudent::getSrcScreeningNoticeId, itemId);
                break;
            case PLAN:
                lambdaQueryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningPlanId, itemId);
                break;
            case TASK:
                lambdaQueryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningTaskId, itemId);
                break;
            default:
                return Collections.emptyList();
        }
        return baseMapper.selectList(lambdaQueryWrapper);
    }

    /**
     * 根据实体查找数据
     *
     * @param screeningPlanSchoolStudent
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByEntity(ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.setEntity(screeningPlanSchoolStudent);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据计划学生查找数据
     * @param planStudentIdSet
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByIds(Set<String> planStudentIdSet) {
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ScreeningPlanSchoolStudent::getId,planStudentIdSet);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 通过screeningCode获取列表
     *
     * @param screeningCode 筛查编号
     * @param planId        计划Id
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getByScreeningCodes(List<Long> screeningCode, Integer planId) {
        return baseMapper.getByScreeningCodes(screeningCode, planId);
    }

    /**
     * 获取筛查列表
     *
     * @param planId   计划Id
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getByPlanIdAndSchoolIdAndGradeId(Integer planId, Integer schoolId, Integer gradeId) {
        return baseMapper.getByPlanIdAndSchoolIdAndGradeId(planId, schoolId, gradeId);
    }

    /**
     * 通过学生id获取筛查学生
     *
     * @param studentIds 学生Ids
     * @return 筛查学生
     */
    public List<ScreeningPlanSchoolStudent> getByStudentIds(List<Integer> studentIds) {
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ScreeningPlanSchoolStudent::getStudentId, studentIds);
        return baseMapper.selectList(queryWrapper);
    }

    public StudentScreeningProgressVO getStudentScreeningProgress(VisionScreeningResult screeningResult) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = getById(screeningResult.getScreeningPlanSchoolStudentId());
        StudentVO studentVO = StudentVO.getInstance(screeningPlanSchoolStudent);
        return StudentScreeningProgressVO.getInstanceWithDefault(screeningResult, studentVO);
    }

    /**
     * 获取筛查学生列表
     *
     * @param screeningPlanId 计划Id
     * @param schoolId        学校Id
     * @param gradeId         年级Id
     * @param classId         班级Id
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getByPlanIdAndSchoolIdAndGradeIdAndClassId(Integer screeningPlanId, Integer schoolId,
                                                                                       Integer gradeId, Integer classId) {
        return baseMapper.getByPlanIdAndSchoolIdAndGradeIdAndClassId(screeningPlanId, schoolId, gradeId, classId);
    }

    /**
     * 分页查询
     *
     * @param page 页码
     * @param size 条数
     * @param screeningStudentQueryDTO 查询条件
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent>
     **/
    public IPage<ScreeningPlanSchoolStudent> selectPlanStudentListByPage(Integer page, Integer size, ScreeningStudentQueryDTO screeningStudentQueryDTO) {
        return baseMapper.selectPlanStudentListByPage(new PageRequest().setCurrent(page).setSize(size).toPage(), screeningStudentQueryDTO);
    }

    public ScreeningPlanSchoolStudent getOneByStudentName(String name) {
        return baseMapper.getOneByStudentName(name);
    }
}
