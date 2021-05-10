package com.wupol.myopia.business.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.ScreeningTaskDTO;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import com.wupol.myopia.business.management.domain.model.ScreeningTaskOrg;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningTaskQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningTaskOrgVo;
import com.wupol.myopia.business.management.domain.vo.ScreeningTaskVo;
import com.wupol.myopia.business.management.service.GovDeptService;
import com.wupol.myopia.business.management.service.ScreeningTaskOrgService;
import com.wupol.myopia.business.management.service.ScreeningTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Alix
 * date 2021-01-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningTask")
public class ScreeningTaskController {

    @Autowired
    protected ScreeningTaskService screeningTaskService;
    @Autowired
    private ScreeningTaskOrgService screeningTaskOrgService;
    @Autowired
    private GovDeptService govDeptService;

    /**
     * 新增
     *
     * @param screeningTaskDTO 新增参数
     */
    @PostMapping()
    public void createInfo(@RequestBody @Valid ScreeningTaskDTO screeningTaskDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        //校验部门
        if (user.isPlatformAdminUser()) {
            Assert.notNull(screeningTaskDTO.getDistrictId(), "请选择行政区域");
            Assert.notNull(screeningTaskDTO.getGovDeptId(), "请选择所处部门");
        }
        if (user.isScreeningUser()) {
            throw new ValidationException("无权限");
        }
        if (CollectionUtils.isEmpty(screeningTaskDTO.getScreeningOrgs()) || screeningTaskDTO.getScreeningOrgs().stream().map(ScreeningTaskOrg::getScreeningOrgId).distinct().count() != screeningTaskDTO.getScreeningOrgs().size()) {
            throw new ValidationException("无筛查机构或筛查机构重复");
        }
        if (user.isGovDeptUser()) {
            // 政府部门，设置为用户自身所在的部门层级
            GovDept govDept = govDeptService.getById(user.getOrgId());
            screeningTaskDTO.setDistrictId(govDept.getDistrictId()).setGovDeptId(user.getOrgId());
        }
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningTaskDTO.getStartTime())) {
            throw new ValidationException("筛查开始时间不能早于今天");
        }
        // 已创建校验
        if (screeningTaskService.checkIsCreated(screeningTaskDTO.getScreeningNoticeId(), screeningTaskDTO.getGovDeptId())) {
            throw new ValidationException("该部门任务已创建");
        }
        screeningTaskDTO.setCreateUserId(user.getId());
        screeningTaskService.saveOrUpdateWithScreeningOrgs(user, screeningTaskDTO, true);
    }

    /**
     * 查看筛查任务
     *
     * @param id 筛查通知ID
     * @return Object
     */
    @GetMapping("{id}")
    public Object getInfo(@PathVariable Integer id) {
        return screeningTaskService.getDTOById(id);
    }

    /**
     * 更新筛查通知
     *
     * @param screeningTaskDTO 更新参数
     */
    @PutMapping()
    public void updateInfo(@RequestBody @Valid ScreeningTaskDTO screeningTaskDTO) {
        validateExistAndAuthorize(screeningTaskDTO.getId());
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningTaskDTO.getStartTime())) {
            throw new ValidationException("筛查开始时间不能早于今天");
        }
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (CollectionUtils.isEmpty(screeningTaskDTO.getScreeningOrgs()) || screeningTaskDTO.getScreeningOrgs().stream().map(ScreeningTaskOrg::getId).distinct().count() != screeningTaskDTO.getScreeningOrgs().size()) {
            throw new ValidationException("无筛查机构或筛查机构重复");
        }
        screeningTaskService.saveOrUpdateWithScreeningOrgs(user, screeningTaskDTO, false);
    }

    /**
     * 校验计划是否存在与发布状态
     * 同时校验权限
     *
     * @param screeningTaskId 筛查通知ID
     * @return ScreeningTask 筛查通知
     */
    private ScreeningTask validateExistAndAuthorize(Integer screeningTaskId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // 校验用户机构
        if (user.isScreeningUser()) {
            // 筛查机构，无权限处理
            throw new ValidationException("无权限");
        }
        ScreeningTask screeningTask = validateExistWithReleaseStatus(screeningTaskId, CommonConst.STATUS_RELEASE);
        if (user.isGovDeptUser()) {
            // 政府部门人员，需校验是否同部门
            Assert.isTrue(user.getOrgId().equals(screeningTask.getGovDeptId()), "无该部门权限");
        }
        return screeningTask;
    }

    /**
     * 校验筛查任务是否存在且校验发布状态
     *
     * @param id 筛查通知id
     * @return 筛查通知
     */
    private ScreeningTask validateExistWithReleaseStatus(Integer id, Integer releaseStatus) {
        ScreeningTask screeningTask = validateExist(id);
        Integer taskStatus = screeningTask.getReleaseStatus();
        if (releaseStatus.equals(taskStatus)) {
            throw new BusinessException(String.format("该任务%s", CommonConst.STATUS_RELEASE.equals(taskStatus) ? "已发布" : "未发布"));
        }
        return screeningTask;
    }

    /**
     * 校验筛查通知是否存在
     *
     * @param id 筛查通知ID
     * @return 筛查通知
     */
    private ScreeningTask validateExist(Integer id) {
        if (Objects.isNull(id)) {
            throw new BusinessException("参数ID不存在");
        }
        ScreeningTask screeningTask = screeningTaskService.getById(id);
        if (Objects.isNull(screeningTask)) {
            throw new BusinessException("查无该任务");
        }
        return screeningTask;
    }

    /**
     * 分页查询任务列表
     *
     * @param query   查询参数
     * @param page    分页数据
     * @return Object
     */
    @GetMapping("page")
    public IPage<ScreeningTaskVo> queryInfo(PageRequest page, ScreeningTaskQuery query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (!user.isPlatformAdminUser()) {
            query.setGovDeptId(user.getOrgId());
        }
        return screeningTaskService.getPage(query, page);
    }

    /**
     * 获取任务筛查机构
     *
     * @param screeningTaskId 任务ID
     * @return Object
     */
    @GetMapping("orgs/{screeningTaskId}")
    public List<ScreeningTaskOrgVo> queryOrgsInfo(@PathVariable Integer screeningTaskId) {
        // 任务状态判断
        validateExist(screeningTaskId);
        return screeningTaskOrgService.getOrgVoListsByTaskId(screeningTaskId);
    }

    /**
     * 新增筛查机构
     *
     * @param screeningTaskOrgs 新增参数
     */
    @PostMapping("orgs/{screeningTaskId}")
    public void addOrgsInfo(@PathVariable Integer screeningTaskId, @RequestBody @Valid List<ScreeningTaskOrg> screeningTaskOrgs) {
        if (CollectionUtils.isEmpty(screeningTaskOrgs)) {
            return;
        }
        // 任务状态判断
        validateExistWithReleaseStatus(screeningTaskId, CommonConst.STATUS_NOT_RELEASE);
        // 新增
        screeningTaskOrgService.saveOrUpdateBatchByTaskId(CurrentUserUtil.getCurrentUser(), screeningTaskId, screeningTaskOrgs, true);
    }

    /**
     * 获取筛查机构相同时间段内已有已发布的任务（相同起始时间只取第一个）
     *
     * @param orgId 机构ID
     * @param screeningTaskQuery 查询参数，必须有govDeptId、startCreateTime、endCreateTime
     * @return List
     */
    @PostMapping("orgs/period/{orgId}")
    public List<ScreeningTaskOrgVo> hasTaskOrgVoInPeriod(@PathVariable Integer orgId, @RequestBody ScreeningTaskQuery screeningTaskQuery) {
        List<ScreeningTaskOrgVo> periodList = new ArrayList<>();
        List<String> existStartTimeEndTimeList = new ArrayList<>();
        List<ScreeningTaskOrgVo> hasTaskOrgVoInPeriod = screeningTaskOrgService.getHasTaskOrgVoInPeriod(orgId, screeningTaskQuery);
        hasTaskOrgVoInPeriod.forEach(vo -> {
            String startTimeEndTime = String.format("%s--%s", DateFormatUtil.format(vo.getStartTime(), DateFormatUtil.FORMAT_ONLY_DATE), DateFormatUtil.format(vo.getEndTime(), DateFormatUtil.FORMAT_ONLY_DATE));
            if (!existStartTimeEndTimeList.contains(startTimeEndTime)) {
                periodList.add(vo);
                existStartTimeEndTimeList.add(startTimeEndTime);
            }
        });
        return periodList;
    }

    /**
     * 根据ID删除（这里默认所有表的主键字段都为“id”,且自增）
     *
     * @param id 筛查通知ID
     */
    @DeleteMapping("{id}")
    public void deleteInfo(@PathVariable Integer id) {
        // 判断是否已发布
        validateExistAndAuthorize(id);
        screeningTaskService.removeWithOrgs(id, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 发布
     *
     * @param id ID
     */
    @PostMapping("{id}")
    public void release(@PathVariable Integer id) {
        // 已发布，直接返回
        ScreeningTask screeningTask = validateExistAndAuthorize(id);
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningTask.getStartTime())) {
            throw new ValidationException("筛查开始时间不能早于今天");
        }
        //没有筛查机构，直接报错
        if (CollectionUtils.isEmpty(screeningTaskOrgService.getOrgListsByTaskId(id))){
            throw new ValidationException("无筛查机构");
        }
        screeningTaskService.release(id, CurrentUserUtil.getCurrentUser());
    }

}
