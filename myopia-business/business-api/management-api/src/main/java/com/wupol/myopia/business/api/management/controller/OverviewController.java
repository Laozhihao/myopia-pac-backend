package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.domain.dto.OverviewDetailDTO;
import com.wupol.myopia.business.api.management.service.OverviewBizService;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OrgAccountListDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OverviewDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OverviewRequestDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.business.core.screening.organization.service.OverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 总览机构控制层
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/overview")
public class OverviewController {

    @Autowired
    private OverviewService overviewService;

    @Autowired
    private OverviewBizService overviewBizService;

    /**
     * 保存总览机构
     *
     * @param overview 总览机构实体
     * @return
     */
    @PostMapping
    public UsernameAndPasswordDTO saveOverview(@RequestBody @Valid OverviewRequestDTO overview) {
        initOverview(overview);
        return overviewService.saveOverview(overview);
    }

    /**
     * 更新总览机构
     *
     * @param overview 总览机构实体
     * @return 总览机构实体
     */
    @PutMapping
    public OverviewDetailDTO updateOverview(@RequestBody @Valid OverviewRequestDTO overview) {
        initOverview(overview);
        overviewService.updateOverview(overview);
        // TODO wulizhou 返回当前数据列表所需数据
        return null;
    }

    /**
     * 通过总览机构ID获取总览机构
     *
     * @param id 总览机构ID
     * @return 总览机构实体
     */
    @GetMapping("{id}")
    public OverviewDetailDTO getOverview(@PathVariable("id") Integer id) {
        return null;
    }

    /**
     * 总览机构列表
     *
     * @param pageRequest 分页请求
     * @param query       分页条件
     * @return 医院列表
     */
    @GetMapping("list")
    public IPage<OverviewDTO> getOverviewList(PageRequest pageRequest, HospitalQuery query) {
        return null;
    }

    /**
     * 更新总览机构管理员状态
     *
     * @param statusRequest 请求入参
     * @return 更新结果
     */
    @PutMapping("/admin/status")
    public boolean updateOverviewAdminUserStatus(@RequestBody @Valid StatusRequest statusRequest) {
        return overviewService.updateOverviewAdminUserStatus(statusRequest);
    }

    /**
     * 重置密码
     *
     * @param request 请求入参
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping("/admin/reset")
    public UsernameAndPasswordDTO resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return overviewService.resetPassword(request);
    }

    /**
     * 获取总览机构管理员用户账号列表
     *
     * @param overviewId 总览机构Id
     * @return List<OrgAccountListDTO>
     */
    @GetMapping("/accountList/{overviewId}")
    public List<OrgAccountListDTO> getAccountList(@PathVariable("overviewId") Integer overviewId) {
        return overviewService.getAccountList(overviewId);
    }

    /**
     * 添加用户
     *
     * @param overviewId 请求入参
     * @return UsernameAndPasswordDTO
     */
    @PostMapping("/add/account/{overviewId}")
    public UsernameAndPasswordDTO addAccount(@PathVariable("overviewId")  Integer overviewId) {
        return overviewService.addOverviewAdminUserAccount(overviewId);
    }

    private Overview initOverview(Overview overview) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        CurrentUserUtil.isNeedPlatformAdminUser(user);
        overview.setCreateUserId(user.getId());
        overview.setGovDeptId(user.getOrgId());
        // 检验总览机构合作信息
        overviewService.checkOverviewCooperation(overview);
        // 设置状态
        overview.setStatus(overview.getCooperationStopStatus());
        return overview;
    }

}