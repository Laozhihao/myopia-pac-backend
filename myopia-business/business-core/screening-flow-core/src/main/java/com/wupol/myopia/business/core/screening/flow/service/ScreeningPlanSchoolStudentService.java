package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.ContrastTypeEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningBizTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SchoolCountDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.ScreeningPlanSchoolStudentMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
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
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private SchoolService schoolService;

    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private SchoolGradeService schoolGradeService;

    /**
     * 根据学生id获取筛查计划学校学生
     *
     * @param studentId 学生ID
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getReleasePlanStudentByStudentId(Integer studentId) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.getReleasePlanStudentByStudentId(studentId);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
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
    public List<ScreeningPlanSchoolStudent> getCurrentReleasePlanStudentByOrgIdAndSchoolId(Integer schoolId, Integer deptId, Integer channel) {
        if (deptId == null) {
            throw new ManagementUncheckedException("deptId 不能为空");
        }
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentReleasePlanIds(deptId, channel);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningOrgId, deptId).eq(ScreeningPlanSchoolStudent::getSchoolId, schoolId).in(ScreeningPlanSchoolStudent::getScreeningPlanId, currentPlanIds);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(queryWrapper);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
    }


    public List<ScreeningPlanSchoolStudent> getCurrentRelePlanStudentByGradeIdAndScreeningOrgId(Integer gradeId, Integer screeningOrgId, Integer channel) {
        if (screeningOrgId == null) {
            throw new ManagementUncheckedException("screeningOrgId 不能为空");
        }
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentReleasePlanIds(screeningOrgId, channel);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningOrgId, screeningOrgId).in(ScreeningPlanSchoolStudent::getScreeningPlanId, currentPlanIds).eq(ScreeningPlanSchoolStudent::getGradeId, gradeId);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(queryWrapper);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
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
     *  删除筛查计划中学校学生信息
     *
     * @param screeningPlanId 筛查计划ID
     */
    public void deleteByPlanId(Integer screeningPlanId){
        baseMapper.delete(Wrappers.lambdaQuery(ScreeningPlanSchoolStudent.class)
                .eq(ScreeningPlanSchoolStudent::getScreeningPlanId,screeningPlanId));
    }

    /**
     * 根据计划ID获取所有筛查学生
     *
     * @param screeningPlanId
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByScreeningPlanId(Integer screeningPlanId) {
        return getByScreeningPlanId(screeningPlanId,Boolean.TRUE);
    }

    /**
     * 根据计划ID获取所有筛查学生
     *
     * @param screeningPlanId 筛查计划ID
     * @param isSchoolDistrictId 是否获取学校区域ID
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByScreeningPlanId(Integer screeningPlanId,Boolean isSchoolDistrictId) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.findByPlanId(screeningPlanId);
        if (Objects.equals(Boolean.TRUE,isSchoolDistrictId)){
            return setSchoolDistrictId(screeningPlanSchoolStudentList);
        }else {
            return screeningPlanSchoolStudentList;
        }
    }


    public List<ScreeningPlanSchoolStudent> getByScreeningPlanIds(List<Integer> screeningPlanIds) {
        return getByScreeningPlanIds(screeningPlanIds, null);
    }

    /**
     * 仅查询部分字段(screeningPlanId,schoolId,gradeType)
     * @param screeningPlanIds
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getSomeInfoByScreeningPlanIds(List<Integer> screeningPlanIds) {
        return getByScreeningPlanIds(screeningPlanIds, ScreeningPlanSchoolStudent::getScreeningPlanId, ScreeningPlanSchoolStudent::getSchoolId, ScreeningPlanSchoolStudent::getGradeType);
    }

    /**
     * 查询筛查学校学生信息
     * @param screeningPlanIds
     * @param columns
     * @return
     */
    private List<ScreeningPlanSchoolStudent> getByScreeningPlanIds(List<Integer> screeningPlanIds, SFunction<ScreeningPlanSchoolStudent, ?>... columns) {
        if (CollectionUtils.isEmpty(screeningPlanIds)){
            return Lists.newArrayList();
        }
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(columns)) {
            // 仅查询部分字段
            queryWrapper.select(columns);
        }
        queryWrapper.in(ScreeningPlanSchoolStudent::getScreeningPlanId,screeningPlanIds);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(queryWrapper);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
    }

    /**
     * 根据计划ID获取所学校ID
     *
     * @param screeningPlanId
     * @return
     */
    public List<Integer> findSchoolIdsByPlanId(Integer screeningPlanId) {
        return baseMapper.findSchoolIdsByPlanId(screeningPlanId);
    }

    /**
     * 根据计划ID和学校ID获取所有筛查学生
     *
     * @param screeningPlanId
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByScreeningPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.findByPlanIdAndSchoolId(screeningPlanId, schoolId);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
    }

    /**
     * 根据筛查计划和学校ID获取筛查学生
     * @param screeningPlanIds 筛查计划ID集合
     * @param schoolId 学校ID(非必须)
     */
    public List<ScreeningPlanSchoolStudent> getByPlanIdOrSchoolId(List<Integer> screeningPlanIds, Integer schoolId) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(Wrappers.lambdaQuery(ScreeningPlanSchoolStudent.class)
                .in(ScreeningPlanSchoolStudent::getScreeningPlanId, screeningPlanIds)
                .eq(Objects.nonNull(schoolId), ScreeningPlanSchoolStudent::getSchoolId, schoolId));
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
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
    public Map<Integer, Integer> getSchoolStudentCountByScreeningPlanId(Integer screeningPlanId) {
        return baseMapper.getSchoolCountByPlanId(screeningPlanId).stream().collect(Collectors.toMap(SchoolCountDO::getSchoolId, SchoolCountDO::getSchoolCount));
    }

    /**
     * 根据计划ID集合批量获取学校ID的学生数Map
     *
     * @param screeningPlanIds 筛查计划ID集合
     * @return
     */
    public Map<Integer, Map<Integer, Long>> getSchoolStudentCountByScreeningPlanIds(List<Integer> screeningPlanIds) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = getSomeInfoByScreeningPlanIds(screeningPlanIds);
        Map<Integer, List<ScreeningPlanSchoolStudent>> planSchoolStudentMap = screeningPlanSchoolStudentList.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getScreeningPlanId));
        Map<Integer, Map<Integer, Long>> map = Maps.newHashMap();
        planSchoolStudentMap.forEach((screeningPlanId,planSchoolStudentList)->{
            Map<Integer, Long> collect = planSchoolStudentList.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolId, Collectors.counting()));
            map.put(screeningPlanId,collect);
        });
        return map;
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

    public List<GradeClassesDTO> selectSchoolGradeVoByPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId, Integer gradeId) {
        return baseMapper.selectSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId, gradeId);
    }

    public IPage<ScreeningStudentDTO> selectPageByQuery(Page<?> page, ScreeningStudentQueryDTO query) {
        return baseMapper.selectPageByQuery(page, query);
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
     * 根据年级班级ID获取筛查学生
     *
     * @param gradeId
     * @param classId
     * @return
     */
    public List<ScreeningStudentDTO> selectBySchoolGradeAndClass(Integer screeningPlanId, Integer schoolId,Integer gradeId, Integer classId,List<Integer> studentIds) {
        return baseMapper.selectBySchoolGradeAndClass(screeningPlanId, schoolId,gradeId, classId,studentIds);
    }


    /**
     * 根据查询条件获取当前进行中的计划的学生
     *
     * @param screeningStudentQuery 查询条件
     * @param page 页码
     * @param size 条数
     * @param channel 0：视力筛查，1：常见病。入口不同
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent>
     **/
    public IPage<ScreeningPlanSchoolStudent> getCurrentReleasePlanScreeningStudentList(ScreeningStudentQueryDTO screeningStudentQuery, Integer page, Integer size, Integer channel) {
        // 获取当前计划
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentReleasePlanIds(screeningStudentQuery.getScreeningOrgId(), channel);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new Page<>(page, size);
        }
        screeningStudentQuery.setPlanIds(currentPlanIds);
        return selectPlanStudentListByPage(page, size, screeningStudentQuery);
    }


    /**
     * 根据实体条件查询
     *
     * @param screeningPlanSchoolStudent 查询条件
     * @param channel 0 : 视力筛查，1：常见病
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent>
     **/
    public List<ScreeningPlanSchoolStudent> listReleasePlanStudentByEntityDescByCreateTime(ScreeningPlanSchoolStudent screeningPlanSchoolStudent, Integer channel) {
        // 获取当前计划
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentReleasePlanIds(screeningPlanSchoolStudent.getScreeningOrgId(), channel);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return Collections.emptyList();
        }
        // 查询学生
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.setEntity(screeningPlanSchoolStudent).in(ScreeningPlanSchoolStudent::getScreeningPlanId, currentPlanIds).orderByDesc(ScreeningPlanSchoolStudent::getCreateTime);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(queryWrapper);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
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
        Map<Integer, Integer> schoolDistrictIdMap = getSchoolDistrictIdMap(results);
        return results.stream()
                .map(planSchoolStudent -> {
                    Optional.ofNullable(schoolDistrictIdMap.get(planSchoolStudent.getSchoolId())).ifPresent(planSchoolStudent::setSchoolDistrictId);
                    return planSchoolStudent;
                })
                .collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolDistrictId, Collectors.counting()));
    }

    /**
     * 根据筛查通知Id获取筛查学校所在层级的计划筛查学生记录
     *
     * @param screeningNoticeId
     * @return
     */
    public Map<Integer, List<ScreeningPlanSchoolStudent>> getPlanStudentCountBySrcScreeningNoticeId(Integer screeningNoticeId) {
        List<ScreeningPlanSchoolStudent> results = getPlanStudentCountByScreeningItemId(screeningNoticeId, ContrastTypeEnum.NOTIFICATION);
        if (CollectionUtils.isEmpty(results)) {
            return Collections.emptyMap();
        }
        Map<Integer, Integer> schoolDistrictIdMap = getSchoolDistrictIdMap(results);
        return results.stream()
                .map(planSchoolStudent -> {
                    Optional.ofNullable(schoolDistrictIdMap.get(planSchoolStudent.getSchoolId())).ifPresent(planSchoolStudent::setSchoolDistrictId);
                    return planSchoolStudent;
                })
                .collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolDistrictId));
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
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(lambdaQueryWrapper);
        return setSchoolDistrictId(filterOutPlanStudentOfReleasePlan(screeningPlanSchoolStudentList));
    }

    /**
     * 过滤出属于发布计划的筛查学生
     *
     * @param planSchoolStudentList 筛查学生列表
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent>
     **/
    public List<ScreeningPlanSchoolStudent> filterOutPlanStudentOfReleasePlan(List<ScreeningPlanSchoolStudent> planSchoolStudentList) {
        List<Integer> planIds = planSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getScreeningPlanId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(planIds)) {
            return new ArrayList<>();
        }
        List<ScreeningPlan> planList = screeningPlanService.getByIds(planIds);
        List<Integer> releasePlanIdList = planList.stream().filter(x -> CommonConst.STATUS_RELEASE.equals(x.getReleaseStatus())).map(ScreeningPlan::getId).collect(Collectors.toList());
        return planSchoolStudentList.stream().filter(x -> releasePlanIdList.contains(x.getScreeningPlanId())).collect(Collectors.toList());
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
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(queryWrapper);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
    }

    /**
     * 根据计划学生查找数据
     * @param planStudentIdSet
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getByIds(Set<String> planStudentIdSet) {
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ScreeningPlanSchoolStudent::getId,planStudentIdSet);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(queryWrapper);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
    }

    /**
     * 通过screeningCode获取列表
     *
     * @param screeningCode 筛查编号
     * @param planId        计划Id
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getByScreeningCodes(List<Long> screeningCode, Integer planId) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.getByScreeningCodes(screeningCode, planId);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
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
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.getByPlanIdAndSchoolIdAndGradeId(planId, schoolId, gradeId);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
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
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(queryWrapper);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
    }

    public StudentScreeningProgressVO getStudentScreeningProgress(VisionScreeningResult screeningResult) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = getById(screeningResult.getScreeningPlanSchoolStudentId());
        StudentVO studentVO = StudentVO.getInstance(screeningPlanSchoolStudent, schoolGradeService.getById(screeningPlanSchoolStudent.getGradeId()), schoolClassService.getById(screeningPlanSchoolStudent.getClassId()));
        return StudentScreeningProgressVO.getInstanceWithDefault(screeningResult, studentVO,screeningPlanSchoolStudent);
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
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.getByPlanIdAndSchoolIdAndGradeIdAndClassId(screeningPlanId, schoolId, gradeId, classId);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
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
        ScreeningPlanSchoolStudent planSchoolStudent = baseMapper.getOneByStudentName(name);
        return setSchoolDistrictId(planSchoolStudent);
    }

    /**
     * 通过学生Id获取最新一条筛查学生
     *
     * @param studentId 学生Id
     * @return ScreeningPlanSchoolStudent
     */
    public ScreeningPlanSchoolStudent getLastByStudentId(Integer studentId) {
        ScreeningPlanSchoolStudent planSchoolStudent = baseMapper.getLastByStudentId(studentId);
        return setSchoolDistrictId(planSchoolStudent);
    }

    /**
     * 检查学生是否有筛查计划
     *
     * @param studentId 学生ID
     * @return true-存在筛查计划 false-不存在
     */
    public boolean checkStudentHavePlan(Integer studentId) {
        return !CollectionUtils.isEmpty(findByList(new ScreeningPlanSchoolStudent().setStudentId(studentId)));
    }

    /**
     * 通过条件获取筛查学生
     *
     * @param planIds              计划Id
     * @param schoolId             学校Id
     * @param gradeId              年级Id
     * @param classId              班级Id
     * @param planStudentId        筛查学生Id
     * @param planStudentName      学生名称
     * @param isFilterDoubleScreen 是否过滤复筛
     *
     * @return List<ScreeningStudentDTO>
     */
    public List<ScreeningStudentDTO> getScreeningNoticeResultStudent(List<Integer> planIds, Integer schoolId, Integer gradeId, Integer classId, List<Integer> planStudentId, String planStudentName, Boolean isFilterDoubleScreen) {
        return baseMapper.getScreeningNoticeResultStudent(planIds, schoolId, gradeId, classId, planStudentId, planStudentName, isFilterDoubleScreen);
    }

    /**
     * 获取班级信息
     *
     * @param planId   计划Id
     * @param schoolId 学校Id
     * @param ids      ids
     * @return List<GradeClassesDTO>
     */
    public List<GradeClassesDTO> getByPlanIdAndSchoolIdAndId(Integer planId, Integer schoolId, List<Integer> ids) {
        return baseMapper.getByPlanIdAndSchoolIdAndId(planId, schoolId, ids);
    }

    public List<ScreeningPlanSchoolStudent> getByIdCardAndPassport(String idCard, String passport, Integer id) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.getByIdCardAndPassport(idCard, passport, id);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
    }

    /**
     * 通过学生Ids删除筛查学生
     * <p>删库操作，谨慎使用</p>
     *
     * @param studentIds 学生Id
     */
    public void deleteByStudentIds(List<Integer> studentIds) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return;
        }
        baseMapper.deleteByStudentIds(studentIds);
    }

    /**
     * 校验学号是否重复
     *
     * @param existPlanSchoolStudentList 已经存在的学生
     * @param sno                        学号
     * @param idCard                     身份证
     * @param passport                   护照
     * @param schoolId                   学校Id
     */
    public void checkSno(List<ScreeningPlanSchoolStudent> existPlanSchoolStudentList, String sno, String idCard, String passport, Integer schoolId) {
        if (org.springframework.util.CollectionUtils.isEmpty(existPlanSchoolStudentList) || StringUtils.isBlank(sno)) {
            return;
        }
        for (ScreeningPlanSchoolStudent s : existPlanSchoolStudentList) {
            // 学号是否被使用
            if (StringUtils.equals(sno, s.getStudentNo()) && schoolId.equals(s.getSchoolId()) && ((StringUtils.isNotBlank(idCard) && !StringUtils.equals(idCard, s.getIdCard())) || (StringUtils.isNotBlank(passport) && !StringUtils.equals(passport, s.getPassport())))) {
                throw new BusinessException("学籍号:" + sno + "重复");
            }
        }
    }

    /**
     * 根据计划学生查找数据
     *
     * @param ids id
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getByIds(List<Integer> ids) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectBatchIds(ids);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
    }

    /**
     * 获取筛查学生列表
     *
     * @param planId 计划Id
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getByNePlanId(Integer planId) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.getByNePlanId(planId);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
    }

    /**
     * 获取一条筛查学生列表
     *
     * @param planId 计划Id
     * @return List<ScreeningPlanSchoolStudent>
     */
    public ScreeningPlanSchoolStudent getOneByPlanId(Integer planId) {
        ScreeningPlanSchoolStudent planSchoolStudent = baseMapper.getOneByPlanId(planId);
        if (Objects.isNull(planSchoolStudent)) {
            return null;
        }
        return setSchoolDistrictId(planSchoolStudent);
    }

    public List<GradeClassesDTO> getGradeByPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        return baseMapper.getGradeByPlanIdAndSchoolId(screeningPlanId, schoolId);
    }

    /**
     * 根据筛查编号字符串获取
     *
     * @param screeningCodeStr 筛查编号字符串
     * @return com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent
     **/
    public ScreeningPlanSchoolStudent getByScreeningCodeStr(String screeningCodeStr) {
        try {
            return findOne(new ScreeningPlanSchoolStudent().setScreeningCode(Long.parseLong(screeningCodeStr)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 通过指定条件获取筛查学生
     *
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getReviewStudentList(Integer planId, Integer orgId, Integer schoolId, Integer gradeId, Integer classId) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.getReviewStudentList(planId, orgId, schoolId, gradeId, classId);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
    }

    /**
     * 通过证件号获取筛查学生
     */
    public List<ScreeningPlanSchoolStudent> getByPlanIdIdCardAndPassport(Integer planId, String idCard, String passport, Integer id) {
        return baseMapper.getByPlanIdIdCardAndPassport(planId, idCard, passport, id);
    }

    /**
     * 获取常见病筛查学生
     *
     * @param schoolId 学校ID
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dto.CommonDiseasePlanStudent>
     **/
    public List<CommonDiseasePlanStudent> getCommonDiseaseScreeningPlanStudent(Integer schoolId) {
        List<CommonDiseasePlanStudent> commonDiseasePlanStudents = baseMapper.selectCommonDiseaseScreeningPlanStudent(schoolId);
        if (CollectionUtils.isEmpty(commonDiseasePlanStudents)){
            return commonDiseasePlanStudents;
        }
        Map<Integer, Integer> schoolDistrictIdMap = getCommonDiseasePlanStudentDistrictIdMap(commonDiseasePlanStudents);
        commonDiseasePlanStudents.forEach(planSchoolStudent -> Optional.ofNullable(schoolDistrictIdMap.get(planSchoolStudent.getSchoolId())).ifPresent(planSchoolStudent::setSchoolDistrictId));
        return commonDiseasePlanStudents;
    }

    /**
     * 获取参与筛查计划的学生集合
     *
     * @param noticeIds 通知ID集合
     * @param schoolIds 学校ID集合
     */
    public List<ScreeningPlanSchoolStudent> getByNoticeIdsAndSchoolIds(List<Integer> noticeIds, List<Integer> schoolIds) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.getByNoticeIdsAndSchoolIds(noticeIds, schoolIds);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);

    }

    /**
     * 获取参与筛查计划的学生集合
     * @param planId 计划ID
     * @param schoolId 学校ID
     */
    public List<ScreeningPlanSchoolStudent> getByPlanIdAndSchoolId(Integer planId, Integer schoolId) {
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningPlanId, planId)
                .eq(ScreeningPlanSchoolStudent::getSchoolId, schoolId);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(queryWrapper);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
    }

    public List<ScreeningPlanSchoolStudent> getByPlanIdsAndSchoolId(List<Integer> planIds, Integer schoolId,Boolean isSchoolDistrict) {
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ScreeningPlanSchoolStudent::getScreeningPlanId, planIds)
                .eq(ScreeningPlanSchoolStudent::getSchoolId, schoolId);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(queryWrapper);
        if (Objects.equals(Boolean.TRUE,isSchoolDistrict)){
            return setSchoolDistrictId(screeningPlanSchoolStudentList);
        }
        return screeningPlanSchoolStudentList;
    }


    /**
     * 获取参与筛查计划的学生集合
     *
     * @param noticeId 通知ID
     * @param districtIds 区域ID集合
     */
    public List<ScreeningPlanSchoolStudent> getByNoticeIdDistrictIds(Integer noticeId, List<Integer> districtIds) {
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getSrcScreeningNoticeId,noticeId);
        queryWrapper.in(ScreeningPlanSchoolStudent::getSchoolDistrictId,districtIds);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(queryWrapper);
        return setSchoolDistrictId(filterOutPlanStudentOfReleasePlan(screeningPlanSchoolStudentList));
    }


    /**
     * 获取参与筛查计划的学生集合
     *
     * @param schoolId 学校ID
     */
    public List<ScreeningPlanSchoolStudent> getBySchoolId(Integer schoolId) {
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getSchoolId, schoolId);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = baseMapper.selectList(queryWrapper);
        return setSchoolDistrictId(screeningPlanSchoolStudentList);
    }

    /**
     * 参与筛查计划的学生
     * @param screeningPlanSchoolStudent 参与筛查计划的学生表对象
     */
    @Override
    public ScreeningPlanSchoolStudent findOne(ScreeningPlanSchoolStudent screeningPlanSchoolStudent){
        ScreeningPlanSchoolStudent planSchoolStudent = super.findOne(screeningPlanSchoolStudent);
        return setSchoolDistrictId(planSchoolStudent);
    }

    /**
     * 根据ID查询参与筛查计划的学生
     *
     * @param id 主键
     */
    @Override
    public ScreeningPlanSchoolStudent getById(Serializable id){
        ScreeningPlanSchoolStudent planSchoolStudent = super.getById(id);
        return setSchoolDistrictId(planSchoolStudent);
    }

    /**
     * 设置学校区域ID
     * @param screeningPlanSchoolStudentList 参与筛查计划的学生集合
     */
    private List<ScreeningPlanSchoolStudent> setSchoolDistrictId(List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList){
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudentList)){
            return screeningPlanSchoolStudentList;
        }
        Map<Integer, Integer> schoolDistrictIdMap = getSchoolDistrictIdMap(screeningPlanSchoolStudentList);
        screeningPlanSchoolStudentList.forEach(planSchoolStudent -> Optional.ofNullable(schoolDistrictIdMap.get(planSchoolStudent.getSchoolId())).ifPresent(planSchoolStudent::setSchoolDistrictId));
        return screeningPlanSchoolStudentList;
    }

    /**
     * 设置学校区域ID
     * @param planSchoolStudent 参与筛查计划的学生
     */
    private ScreeningPlanSchoolStudent setSchoolDistrictId(ScreeningPlanSchoolStudent planSchoolStudent){
        if (Objects.isNull(planSchoolStudent)) {
            return null;
        }
        School school = schoolService.getById(planSchoolStudent.getSchoolId());
        planSchoolStudent.setSchoolDistrictId(school.getDistrictId());
        return planSchoolStudent;
    }

    /**
     * 获取学校区域ID
     * @param results  参与筛查计划的学生集合
     */
    private Map<Integer,Integer> getSchoolDistrictIdMap(List<ScreeningPlanSchoolStudent> results){
        Set<Integer> schoolIds = results.stream().map(ScreeningPlanSchoolStudent::getSchoolId).collect(Collectors.toSet());
        List<School> schoolList = schoolService.getByIds(Lists.newArrayList(schoolIds));
        if (CollectionUtils.isEmpty(schoolList)){
            return Collections.emptyMap();
        }
        return schoolList.stream().collect(Collectors.toMap(School::getId,School::getDistrictId));
    }

    /**
     * 获取学校区域ID
     *
     * @param results 常见病筛查学生ID集合
     */
    private Map<Integer,Integer> getCommonDiseasePlanStudentDistrictIdMap(List<CommonDiseasePlanStudent> results){
        Set<Integer> schoolIds = results.stream().map(CommonDiseasePlanStudent::getSchoolId).collect(Collectors.toSet());
        List<School> schoolList = schoolService.getByIds(Lists.newArrayList(schoolIds));
        if (CollectionUtils.isEmpty(schoolList)){
            return Collections.emptyMap();
        }
        return schoolList.stream().collect(Collectors.toMap(School::getId,School::getDistrictId));
    }

    /**
     * 通过姓名与证件号并指定筛查类型获取信息
     *
     * @param credentialNo
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getLastByCredentialNoAndStudentName(String credentialNo, String studentName) {
        return baseMapper.getLastByCredentialNoAndStudentName(credentialNo, studentName);
    }

    public ScreeningPlanSchoolStudent getLastByCredentialNoAndStudentIds(Integer type, List<Integer> planId, List<Integer> studentId) {
        return baseMapper.getLastByCredentialNoAndStudentIds(type, planId, studentId);
    }

    /**
     * 根据taskid 和schoolid 获得学生
     *
     * @param taskId
     * @param schoolIds
     * @return
     */
    public List<ScreeningPlanSchoolStudent> findStudentByTaskIdAndSchoolsIds(Integer taskId, Set<Integer> schoolIds) {
        return filterOutPlanStudentOfReleasePlan(baseMapper.selectList(new LambdaQueryWrapper<ScreeningPlanSchoolStudent>()
                .in(ScreeningPlanSchoolStudent::getSchoolId, schoolIds)
                .eq(ScreeningPlanSchoolStudent::getScreeningTaskId, taskId)
        ));
    }

    /**
     * 新增筛查学生
     * @param twoTuple 筛查计划学校学生
     * @param screeningPlanId 筛查计划ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void addScreeningStudent(TwoTuple<List<ScreeningPlanSchoolStudent>, List<Integer>> twoTuple,Integer screeningPlanId,Integer srcScreeningNoticeId,Integer screeningTaskId) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = twoTuple.getFirst();
        deleteByStudentIds(twoTuple.getSecond());
        if (CollUtil.isEmpty(screeningPlanSchoolStudentList)){
            return;
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
        screeningPlanSchoolStudentList.forEach(screeningPlanSchoolStudent -> {
            if (Objects.isNull(screeningPlanSchoolStudent.getScreeningPlanId())){
                screeningPlanSchoolStudent.setScreeningPlanId(screeningPlanId);
            }
            if (Objects.nonNull(srcScreeningNoticeId) && !Objects.equals(srcScreeningNoticeId,CommonConst.DEFAULT_ID)){
                screeningPlanSchoolStudent.setSrcScreeningNoticeId(srcScreeningNoticeId);
            }
            if (Objects.nonNull(screeningTaskId) && !Objects.equals(screeningTaskId,CommonConst.DEFAULT_ID)){
                screeningPlanSchoolStudent.setScreeningTaskId(screeningTaskId);
            }
            // 如果是协助筛查，需要覆盖一下筛查机构ID，避免学校端新增筛查学生时筛查机构ID为学校ID，与实际不符
            if (ScreeningBizTypeEnum.isAssistanceScreeningType(ScreeningBizTypeEnum.getInstanceByOrgType(screeningPlan.getScreeningOrgType()).getType())) {
                screeningPlanSchoolStudent.setScreeningOrgId(screeningPlan.getScreeningOrgId());
            }
        });
        saveOrUpdateBatch(screeningPlanSchoolStudentList);
    }

    /**
     * 通过function 分组
     *
     * @param screeningPlanSchoolStudentList 筛查学生列表
     * @param function                       function
     *
     * @return Map<Integer, List < ScreeningPlanSchoolStudent>>
     */
    public Map<Integer, List<ScreeningPlanSchoolStudent>> groupingByFunction(List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList,
                                                                             Function<ScreeningPlanSchoolStudent, Integer> function) {
        Map<Integer, List<ScreeningPlanSchoolStudent>> gradePlanSchoolStudentMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(screeningPlanSchoolStudentList)) {
            Map<Integer, List<ScreeningPlanSchoolStudent>> map = screeningPlanSchoolStudentList.stream().collect(Collectors.groupingBy(function));
            gradePlanSchoolStudentMap.putAll(map);
        }
        return gradePlanSchoolStudentMap;
    }

    /**
     * 根据计划ID获取所有筛查学生
     *
     * @param screeningPlanId
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getInfoByScreeningPlanId(Integer screeningPlanId) {
        return baseMapper.getInfoByPlanId(screeningPlanId);
    }

    /**
     * 根据计划id,学校id,学籍号获取筛查学生
     * @param schoolId
     * @param screeningPlanId
     * @param snoList
     * @return
     */
    public List<PlanStudentInfoDTO> findStudentBySchoolIdAndScreeningPlanIdAndSno(Integer schoolId, Integer screeningPlanId, List<String> snoList) {
        if (org.springframework.util.CollectionUtils.isEmpty(snoList)) {
            return new ArrayList<>();
        }
        return baseMapper.findStudentBySchoolIdAndScreeningPlanIdAndSno(schoolId,screeningPlanId,snoList);
    }
}
