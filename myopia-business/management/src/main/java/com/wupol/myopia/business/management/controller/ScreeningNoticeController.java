package com.wupol.myopia.business.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sun.javafx.binding.StringFormatter;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.query.ScreeningNoticeQuery;
import com.wupol.myopia.business.management.service.ScreeningNoticeDeptOrgService;
import com.wupol.myopia.business.management.service.ScreeningNoticeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningNotice")
public class ScreeningNoticeController extends BaseController<ScreeningNoticeService, ScreeningNotice> {

    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    /**
     * 新增
     *
     * @param screeningNotice 新增参数
     * @return Object
     */
    @Override
    @PostMapping()
    public void createInfo(@RequestBody @Valid ScreeningNotice screeningNotice) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // TODO 看前端是否能拿到用户的层级与部门再做处理
//        if (user.isPlatformAdminUser()) {
//            Assert.notNull(screeningNotice.getDistrictId());
//            Assert.notNull(screeningNotice.getGovDeptId());
//        }
        screeningNotice.setCreatorId(user.getId()).setOperatorId(user.getId());
        if (!baseService.save(screeningNotice)) {
            throw new BusinessException("创建失败");
        }
    }

    /**
     * 根据ID更新
     *
     * @param screeningNotice 更新参数
     * @return Object
     */
    @Override
    @PutMapping()
    public void updateInfo(@RequestBody @Valid ScreeningNotice screeningNotice) {
        validateExistWithReleaseStatus(screeningNotice.getId(), Const.STATUS_RELEASE);
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (!baseService.updateById(screeningNotice, user.getId())) {
            throw new BusinessException("修改失败");
        }
    }

    /**
     * 校验筛查通知是否存在且校验发布状态
     *
     * @param id
     */
    private void validateExistWithReleaseStatus(Integer id, Integer releaseStatus) {
        ScreeningNotice notice = validateExist(id);
        Integer noticeStatus = notice.getReleaseStatus();
        if (releaseStatus.equals(noticeStatus)) {
            throw new BusinessException(String.format("该通知%s", Const.STATUS_RELEASE.equals(noticeStatus) ? "已发布" : "未发布"));
        }
    }

    /**
     * 校验筛查通知是否存在
     *
     * @param id
     * @return
     */
    private ScreeningNotice validateExist(Integer id) {
        if (Objects.isNull(id)) {
            throw new BusinessException("参数ID不存在");
        }
        ScreeningNotice notice = baseService.getById(id);
        if (Objects.isNull(notice)) {
            throw new BusinessException("查无该通知");
        }
        return notice;
    }

    /**
     * 分页查询创建的通知（发布筛查通知页）
     * 1. 管理员：所有
     * 2. 政府机构：自己部门创建的
     *
     * @param query   查询参数
     * @param current 页码
     * @param size    条数
     * @return Object
     */
    @GetMapping("dept/page")
    public IPage queryDeptPage(ScreeningNoticeQuery query,
                               @RequestParam(defaultValue = "1") Integer current,
                               @RequestParam(defaultValue = "10") Integer size) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        query.setType(0);
        if (user.isPlatformAdminUser()) {
            query.setDistrictId(null).setGovDeptId(null);
        } else if (user.isGovDeptUser()) {
            query.setGovDeptId(user.getOrgId());
        }
        return baseService.getPage(query, current, size);
    }

    /**
     * 分页查询通知（筛查通知页）
     * 1. 政府机构：上级创建的创建的筛查通知
     * 2. 筛查机构：政府机构创建的筛查任务通知
     *
     * @param query   查询参数
     * @param current 页码
     * @param size    条数
     * @return Object
     */
    @GetMapping("page")
    public IPage queryPage(ScreeningNoticeQuery query,
                           @RequestParam(defaultValue = "1") Integer current,
                           @RequestParam(defaultValue = "10") Integer size) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isGovDeptUser()) {
            query.setType(0);
            query.setGovDeptId(user.getOrgId());
        } else if (user.isScreeningUser()) {
            query.setType(1);
            query.setGovDeptId(user.getOrgId());
        }
        return screeningNoticeDeptOrgService.getPage(query, current, size);
    }

    /**
     * 根据ID删除（这里默认所有表的主键字段都为“id”,且自增）
     *
     * @param id ID
     * @return void
     */
    @DeleteMapping("{id}")
    @Override
    public void deleteInfo(@PathVariable Integer id) {
        // 判断是否已发布
        validateExistWithReleaseStatus(id, Const.STATUS_RELEASE);
        if (!baseService.removeById(id)) {
            throw new BusinessException("删除失败，请重试");
        }
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
        //TODO 非政府部门，直接报错
        validateExistWithReleaseStatus(id, Const.STATUS_RELEASE);
        baseService.release(id, CurrentUserUtil.getCurrentUser());
    }
}
