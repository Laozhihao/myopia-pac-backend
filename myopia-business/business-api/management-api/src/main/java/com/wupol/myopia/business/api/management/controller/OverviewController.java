package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.api.management.service.HospitalBizService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OrgAccountListDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.business.core.screening.organization.service.OverviewService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
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
    private HospitalService hospitalService;

    @Autowired
    private HospitalBizService hospitalBizService;

    @Autowired
    private HospitalDoctorService hospitalDoctorService;

    @Autowired
    private OverviewService overviewService;

    /**
     * 保存医院
     *
     * @param overview 医院实体
     * @return 医院实体
     */
    @PostMapping
    public UsernameAndPasswordDTO saveOverview(@RequestBody @Valid Overview overview) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        CurrentUserUtil.isNeedPlatformAdminUser(user);
        overview.setCreateUserId(user.getId());
        overview.setGovDeptId(user.getOrgId());
        // 检验总览机构合作信息
        overviewService.checkOverviewCooperation(overview);
        overview.setStatus(overview.getCooperationStopStatus());
        return hospitalService.saveHospital(hospital);
    }

    /**
     * 更新医院
     *
     * @param overview 医院实体
     * @return 医院实体
     */
    @PutMapping
    public HospitalResponseDTO updateOverview(@RequestBody @Valid Overview overview) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isPlatformAdminUser()){
            hospitalService.checkHospitalCooperation(hospital);
            // 设置医院状态
            hospital.setStatus(hospital.getCooperationStopStatus());
        } else {
            // 非平台管理员无法更新合作信息
            hospital.clearCooperationInfo();
            hospital.setStatus(null);
            hospital.setAccountNum(null);
        }
        return hospitalBizService.updateHospital(hospital);
    }

    /**
     * 通过医院ID获取医院
     *
     * @param id 医院ID
     * @return 医院实体
     */
    @GetMapping("{id}")
    public HospitalResponseDTO getOverview(@PathVariable("id") Integer id) {
        Hospital hospital = hospitalService.getById(id);
        HospitalResponseDTO hospitalResponse = new HospitalResponseDTO();
        BeanUtils.copyProperties(hospital, hospitalResponse);
        return hospitalResponse.setDoctorTotalNum(hospitalDoctorService.count(new Doctor().setHospitalId(id)));
    }

    /**
     * 医院列表
     *
     * @param pageRequest 分页请求
     * @param query       分页条件
     * @return 医院列表
     */
    @GetMapping("list")
    public IPage<HospitalResponseDTO> getOverviewList(PageRequest pageRequest, HospitalQuery query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalBizService.getHospitalList(pageRequest, query, user);
    }

    /**
     * 更新医院管理员状态
     *
     * @param statusRequest 请求入参
     * @return 更新结果
     */
    @PutMapping("/admin/status")
    public boolean updateOverviewAdminUserStatus(@RequestBody @Valid StatusRequest statusRequest) {
        return hospitalService.updateHospitalAdminUserStatus(statusRequest);
    }

    /**
     * 重置密码
     *
     * @param request 请求入参
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping("reset")
    public UsernameAndPasswordDTO resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return hospitalService.resetPassword(request);
    }

    /**
     * 获取医院管理员用户账号列表
     *
     * @param overviewId 医院Id
     * @return List<OrgAccountListDTO>
     */
    @GetMapping("/accountList/{overviewId}")
    public List<OrgAccountListDTO> getAccountList(@PathVariable("overviewId") Integer overviewId) {
        return hospitalBizService.getAccountList(overviewId);
    }

    /**
     * 添加用户
     *
     * @param overviewId 请求入参
     * @return UsernameAndPasswordDTO
     */
    @PostMapping("/add/account/{overviewId}")
    public UsernameAndPasswordDTO addAccount(@PathVariable("overviewId")  Integer overviewId) {
        return hospitalBizService.addHospitalAdminUserAccount(overviewId);
    }

}