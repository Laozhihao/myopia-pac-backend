package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningNoticeVO;
import com.wupol.myopia.business.api.management.service.ScreeningNoticeBizService;
import com.wupol.myopia.business.api.management.service.ScreeningNoticeDeptOrgBizService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@ResponseResultBody
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/management/screeningNotice")
public class ScreeningNoticeController {

    @Autowired
    protected ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private ScreeningNoticeBizService screeningNoticeBizService;
    @Autowired
    private ScreeningNoticeDeptOrgBizService screeningNoticeDeptOrgBizService;

    /**
     * 新增
     *
     * @param screeningNotice 新增参数
     * @return Object
     */
    @PostMapping()
    public void createInfo(@RequestBody @Valid ScreeningNotice screeningNotice) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // TODO 2021.02.01 与肖肖确认，发布时确认即可 createOrReleaseValidate(screeningNotice);
        if (user.isPlatformAdminUser()) {
            Assert.notNull(screeningNotice.getDistrictId(), "请选择行政区域");
            Assert.notNull(screeningNotice.getGovDeptId(), "请选择所处部门");
        }
        if (user.isScreeningUser()) {
            throw new ValidationException("无权限");
        }
        if (user.isGovDeptUser()) {
            // 政府部门，设置为用户自身所在的部门层级
            GovDept govDept = govDeptService.getById(user.getOrgId());
            screeningNotice.setDistrictId(govDept.getDistrictId()).setGovDeptId(user.getOrgId());
        }
        screeningNotice.setCreateUserId(user.getId()).setOperatorId(user.getId());
        if (!screeningNoticeService.save(screeningNotice)) {
            throw new BusinessException("创建失败");
        }
    }

    /**
     * 创建筛查通知或发布时校验
     * 1. 开始时间只能在今天或以后
     * 2. 一个部门在一个时间段内只能发布一个筛查通知【即时间不允许重叠，且只能创建今天之后的时间段】
     * 3. 同一个部门下，筛查标题唯一性，要进行校验，标题不能相同。
     *
     * @param screeningNotice
     */
    private void createOrReleaseValidate(ScreeningNotice screeningNotice) {
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningNotice.getStartTime())) {
            throw new ValidationException("筛查开始时间不能早于今天");
        }
        // 一个部门在一个时间段内只能发布一个筛查通知【即时间不允许重叠，且只能创建今天之后的时间段】
        if (screeningNoticeService.checkTimeLegal(screeningNotice)) {
            throw new ValidationException("该部门该时间段已存在筛查通知");
        }
        // 同一个部门下，筛查标题唯一性，要进行校验，标题不能相同。
        if (screeningNoticeService.checkTitleExist(screeningNotice.getId(), screeningNotice.getGovDeptId(), screeningNotice.getTitle())) {
            throw new ValidationException("该部门已存在相同标题通知");
        }
    }

    /**
     * 根据ID获取单个信息（这里默认所有表的主键字段都为“id”,且自增）
     *
     * @param id ID
     * @return Object
     */
    @GetMapping("{id}")
    public ScreeningNotice getInfo(@PathVariable Integer id) {
        return screeningNoticeService.getById(id);
    }

    /**
     * 根据ID更新
     *
     * @param screeningNotice 更新参数
     * @return Object
     */
    @PutMapping()
    public void updateInfo(@RequestBody @Valid ScreeningNotice screeningNotice) {
        validateExistWithReleaseStatus(screeningNotice.getId(), CommonConst.STATUS_RELEASE);
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (!screeningNoticeService.updateById(screeningNotice, user.getId())) {
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
            throw new BusinessException(String.format("该通知%s", CommonConst.STATUS_RELEASE.equals(noticeStatus) ? "已发布" : "未发布"));
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
        ScreeningNotice notice = screeningNoticeService.getById(id);
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
     * @param query       查询参数
     * @param pageRequest 分页数据
     * @return Object
     */
    @GetMapping("dept/page")
    public IPage<ScreeningNoticeVO> queryDeptPage(ScreeningNoticeQueryDTO query, PageRequest pageRequest) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        query.setType(0);
        if (user.isPlatformAdminUser()) {
            query.setDistrictId(null).setGovDeptId(null);
        } else if (user.isGovDeptUser()) {
            query.setGovDeptId(user.getOrgId());
        }
        return screeningNoticeBizService.getPage(query, pageRequest);
    }

    /**
     * 分页查询通知（筛查通知页）
     * 1. 政府机构：上级创建的创建的筛查通知
     * 2. 筛查机构：政府机构创建的筛查任务通知
     *
     * @param query       查询参数
     * @param pageRequest 分页数据
     * @return Object
     */
    @GetMapping("page")
    public IPage<ScreeningNoticeVO> queryInfo(ScreeningNoticeQueryDTO query, PageRequest pageRequest) throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        query.setReleaseStatus(CommonConst.STATUS_RELEASE);
        if (user.isGovDeptUser()) {
            query.setType(0);
            query.setGovDeptId(user.getOrgId());
        } else if (user.isScreeningUser()) {
            query.setType(1);
            query.setGovDeptId(user.getOrgId());
        }
        return screeningNoticeDeptOrgBizService.getPage(query, pageRequest);
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
        if (!screeningNoticeService.removeById(id)) {
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
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // 已发布，直接返回
        validateExistWithReleaseStatus(id, CommonConst.STATUS_RELEASE);
        ScreeningNotice notice = screeningNoticeService.getById(id);
        createOrReleaseValidate(notice);
        if (user.isPlatformAdminUser() || user.isGovDeptUser() && user.getOrgId().equals(notice.getGovDeptId())) {
            screeningNoticeBizService.release(id, user);
        } else {
            throw new ValidationException("无权限");
        }
    }

    /**
     * 已读
     * 1. 校验权限；管理员-都可以，筛查通知-确认部门，筛查任务通知-确认机构
     *
     * @param noticeDeptOrgId 筛查通知通知到的部门或者机构表ID
     * @return void
     */
    @PostMapping("read/{noticeDeptOrgId}")
    public void read(@PathVariable Integer noticeDeptOrgId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        ScreeningNoticeDeptOrg noticeDeptOrg = screeningNoticeDeptOrgService.getById(noticeDeptOrgId);
        if (Objects.isNull(noticeDeptOrg)) {
            throw new BusinessException("查无通知");
        }
        // 已读，直接返回
        if (CommonConst.STATUS_NOTICE_READ.equals(noticeDeptOrg.getOperationStatus()) || CommonConst.STATUS_NOTICE_CREATED.equals(noticeDeptOrg.getOperationStatus())) {
            log.info("通知已读");
            return;
        }
        //校验权限；管理员-都可以，筛查通知-确认部门，筛查任务通知-确认机构
        if (user.isPlatformAdminUser() || user.getOrgId().equals(noticeDeptOrg.getAcceptOrgId())) {
            screeningNoticeDeptOrgService.read(noticeDeptOrgId, user);
        }
    }
}
