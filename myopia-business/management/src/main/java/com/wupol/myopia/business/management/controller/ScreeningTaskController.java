package com.wupol.myopia.business.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.ScreeningTaskDTO;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.ScreeningTaskOrg;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningTaskQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningTaskOrgVo;
import com.wupol.myopia.business.management.service.GovDeptService;
import com.wupol.myopia.business.management.service.ScreeningTaskOrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import com.wupol.myopia.business.management.service.ScreeningTaskService;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
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
     * @return Object
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
        if (user.isGovDeptUser()) {
            // 政府部门，设置为用户自身所在的部门层级
            GovDept govDept = govDeptService.getById(user.getOrgId());
            screeningTaskDTO.setDistrictId(govDept.getDistrictId()).setGovDeptId(user.getOrgId());
        }
        // 已创建校验
        if (screeningTaskService.checkIsCreated(screeningTaskDTO.getScreeningNoticeId(), screeningTaskDTO.getGovDeptId())) {
            throw new ValidationException("该部门任务已创建");
        }
        screeningTaskService.saveOrUpdateWithScreeningOrgs(user, screeningTaskDTO, true);
    }

    /**
     * 查看筛查任务
     *
     * @param id ID
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
     * @return Object
     */
    @PutMapping()
    public void updateInfo(@RequestBody @Valid ScreeningTaskDTO screeningTaskDTO) {
        validateExistWithReleaseStatus(screeningTaskDTO.getId(), CommonConst.STATUS_RELEASE);
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        //TODO 校验部门
        screeningTaskService.saveOrUpdateWithScreeningOrgs(user, screeningTaskDTO, false);
    }

    /**
     * 校验筛查任务是否存在且校验发布状态
     *
     * @param id
     */
    private void validateExistWithReleaseStatus(Integer id, Integer releaseStatus) {
        ScreeningTask screeningTask = validateExist(id);
        Integer taskStatus = screeningTask.getReleaseStatus();
        if (releaseStatus.equals(taskStatus)) {
            throw new BusinessException(String.format("该任务%s", CommonConst.STATUS_RELEASE.equals(taskStatus) ? "已发布" : "未发布"));
        }
    }

    /**
     * 校验筛查通知是否存在
     *
     * @param id
     * @return
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
    public IPage queryInfo(PageRequest page, ScreeningTaskQuery query) {
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
     * @return Object
     */
    @PostMapping("orgs/{screeningTaskId}")
    public void addOrgsInfo(@PathVariable Integer screeningTaskId, @RequestBody @Valid List<ScreeningTaskOrg> screeningTaskOrgs) {
        if (CollectionUtils.isEmpty(screeningTaskOrgs)) {
            return;
        }
        // 任务状态判断
        validateExistWithReleaseStatus(screeningTaskId, CommonConst.STATUS_NOT_RELEASE);
        // 是否已存在
        screeningTaskOrgService.saveOrUpdateBatchByTaskId(screeningTaskId, screeningTaskOrgs);
    }

    /**
     * 获取筛查机构相同时间段内已有已发布的任务
     *
     * @param orgId 机构ID
     * @param screeningTaskQuery 查询参数，必须有govDeptId、startCreateTime、endCreateTime
     * @return List
     */
    @PostMapping("orgs/period/{orgId}")
    public List<ScreeningTaskOrgVo> hasTaskOrgVoInPeriod(@PathVariable Integer orgId, @RequestBody ScreeningTaskQuery screeningTaskQuery) {
        return screeningTaskOrgService.getHasTaskOrgVoInPeriod(orgId, screeningTaskQuery);
    }

    /**
     * 根据ID删除（这里默认所有表的主键字段都为“id”,且自增）
     *
     * @param id ID
     * @return void
     */
    @DeleteMapping("{id}")
    public void deleteInfo(@PathVariable Integer id) {
        // 判断是否已发布
        validateExistWithReleaseStatus(id, CommonConst.STATUS_RELEASE);
        screeningTaskService.removeWithOrgs(id, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 发布
     *
     * @param id ID
     * @return void
     */
    @PostMapping("{id}")
    public void release(@PathVariable Integer id) {
        // 已发布，直接返回
        validateExistWithReleaseStatus(id, CommonConst.STATUS_RELEASE);
        //TODO 非政府部门，直接报错

        //没有筛查机构，直接报错
        if (CollectionUtils.isEmpty(screeningTaskOrgService.getOrgListsByTaskId(id))){
            throw new ValidationException("无筛查机构");
        }
        screeningTaskService.release(id, CurrentUserUtil.getCurrentUser());
    }

}