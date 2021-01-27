package com.wupol.myopia.business.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.query.ScreeningTaskQuery;
import com.wupol.myopia.business.management.service.ScreeningTaskOrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import com.wupol.myopia.business.management.service.ScreeningTaskService;

import javax.validation.Valid;
import java.util.Objects;

/**
 * @Author HaoHao
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
    /**
     * 新增
     *
     * @param screeningTask 新增参数
     * @return Object
     */
    @PostMapping()
    public void createInfo(@RequestBody @Valid ScreeningTask screeningTask) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // TODO 看前端是否能拿到用户的层级与部门再做处理
//        if (user.isPlatformAdminUser()) {
//            Assert.notNull(screeningTask.getDistrictId());
//            Assert.notNull(screeningTask.getGovDeptId());
//        }
        screeningTask.setCreateUserId(user.getId()).setOperatorId(user.getId());
        if (!screeningTaskService.save(screeningTask)) {
            throw new BusinessException("创建失败");
        }
    }

    /**
     * 查看筛查任务
     *
     * @param id ID
     * @return Object
     */
    @GetMapping("{id}")
    public Object getInfo(@PathVariable Integer id) {
        return screeningTaskService.getById(id);
    }

    /**
     * 更新筛查通知
     *
     * @param screeningTask 更新参数
     * @return Object
     */
    @PutMapping()
    public void updateInfo(@RequestBody @Valid ScreeningTask screeningTask) {
        validateExistWithReleaseStatus(screeningTask.getId(), CommonConst.STATUS_RELEASE);
        CurrentUser user = CurrentUserUtil.getCurrentUser();
//        if (!screeningTaskService.updateById(screeningTask, user.getId())) {
//            throw new BusinessException("修改失败");
//        }
    }

    /**
     * 校验筛查通知是否存在且校验发布状态
     *
     * @param id
     */
    private void validateExistWithReleaseStatus(Integer id, Integer releaseStatus) {
        ScreeningTask screeningTask = validateExist(id);
        Integer noticeStatus = screeningTask.getReleaseStatus();
        if (releaseStatus.equals(noticeStatus)) {
            throw new BusinessException(String.format("该通知%s", CommonConst.STATUS_RELEASE.equals(noticeStatus) ? "已发布" : "未发布"));
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
        ScreeningTask notice = screeningTaskService.getById(id);
        if (Objects.isNull(notice)) {
            throw new BusinessException("查无该通知");
        }
        return notice;
    }
//
//    /**
//     * 分页查询任务列表
//     *
//     * @param query   查询参数
//     * @param current 页码
//     * @param size    条数
//     * @return Object
//     */
//    @GetMapping("page")
//    public IPage queryInfo(ScreeningTaskQuery query,
//                           @RequestParam(defaultValue = "1") Integer current,
//                           @RequestParam(defaultValue = "10") Integer size) {
//        CurrentUser user = CurrentUserUtil.getCurrentUser();
//        return screeningTaskService.getPage(query, current, size);
//    }
//
//    /**
//     * 获取任务筛查机构
//     *
//     * @param screeningTaskId 任务ID
//     * @return Object
//     */
//    @GetMapping("orgs/{{screeningTaskId}}")
//    public IPage queryOrgsInfo(@PathVariable Integer screeningTaskId) {
//        CurrentUser user = CurrentUserUtil.getCurrentUser();
//        return screeningTaskService.getPage(query, current, size);
//    }

    /**
     * 新增筛查机构
     *
     * @param screeningTask 新增参数
     * @return Object
     */
    @PostMapping("orgs/{{screeningTaskId}}")
    public void addOrgsInfo(@RequestBody @Valid ScreeningTask screeningTask) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // TODO 看前端是否能拿到用户的层级与部门再做处理
//        if (user.isPlatformAdminUser()) {
//            Assert.notNull(screeningTask.getDistrictId());
//            Assert.notNull(screeningTask.getGovDeptId());
//        }
        screeningTask.setCreateUserId(user.getId()).setOperatorId(user.getId());
        if (!screeningTaskService.save(screeningTask)) {
            throw new BusinessException("创建失败");
        }
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
        if (!screeningTaskService.removeById(id)) {
            throw new BusinessException("删除失败，请重试");
        }
    }

//    /**
//     * 发布
//     *
//     * @param id ID
//     * @return void
//     */
//    @PostMapping("{id}")
//    public void release(@PathVariable Integer id) {
//        // 已发布，直接返回
//        //TODO 非政府部门，直接报错
//        validateExistWithReleaseStatus(id, CommonConst.STATUS_RELEASE);
//        screeningTaskService.release(id, CurrentUserUtil.getCurrentUser());
//    }

}
