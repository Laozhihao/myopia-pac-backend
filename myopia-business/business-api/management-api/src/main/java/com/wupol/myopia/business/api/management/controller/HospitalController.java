package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
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
import com.wupol.myopia.business.core.common.domain.dto.OrgAccountListDTO;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.screening.organization.domain.dto.CacheOverviewInfoDTO;
import com.wupol.myopia.business.core.screening.organization.service.OverviewService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * 医院控制层
 *
 * @author Simple4H
 */
@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/hospital")
public class HospitalController {

    @Resource
    private HospitalService hospitalService;

    @Resource
    private HospitalBizService hospitalBizService;

    @Resource
    private ExportStrategy exportStrategy;

    @Autowired
    private HospitalDoctorService hospitalDoctorService;

    @Autowired
    private OverviewService overviewService;

    @Autowired
    private DistrictService districtService;

    /**
     * 保存医院
     *
     * @param hospital 医院实体
     * @return 医院实体
     */
    @PostMapping
    public UsernameAndPasswordDTO saveHospital(@RequestBody @Valid Hospital hospital) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        hospital.setCreateUserId(user.getId());
        hospital.setGovDeptId(user.getOrgId());
        if (user.isPlatformAdminUser()) {
            hospitalService.checkHospitalCooperation(hospital);
        } else if (user.isOverviewUser()) {
            // 总览机构
            CacheOverviewInfoDTO overview = overviewService.getSimpleOverviewInfo(user.getOrgId());
            // 绑定医院已达上线或不在同一个省级行政区域下
            if ((!overview.isCanAddHospital()) || (!districtService.isSameProvince(hospital.getDistrictId(), overview.getDistrictId()))) {
                throw new BusinessException("非法请求！");
            }
            hospital.setIsCooperation(CommonConst.IS_COOPERATION);
            hospital.initCooperationInfo(overview.getCooperationType(), overview.getCooperationTimeType(),
                    overview.getCooperationStartTime(), overview.getCooperationEndTime());
            hospital.setServiceType(overview.getHospitalServiceType());
            hospital.setAccountNum(Hospital.ACCOUNT_NUM);
        } else {
            throw new BusinessException("非法的用户类型");
        }
        hospital.setStatus(hospital.getCooperationStopStatus());
        return hospitalBizService.saveHospital(hospital, user);
    }

    /**
     * 更新医院
     *
     * @param hospital 医院实体
     * @return 医院实体
     */
    @PutMapping
    public HospitalResponseDTO updateHospital(@RequestBody @Valid Hospital hospital) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        overviewService.checkHospital(user, hospital.getId());
        if (user.isPlatformAdminUser()){
            hospitalService.checkHospitalCooperation(hospital);
            // 设置医院状态
            hospital.setStatus(hospital.getCooperationStopStatus());
        } else {
            // 非平台管理员无法更新以下信息
            hospital.clearCooperationInfo();
            hospital.setStatus(null);
            hospital.setAccountNum(null);
            hospital.setServiceType(null);
        }
        return hospitalBizService.updateHospital(hospital);
    }

    /**
     * 删除医院
     *
     * @param id 医院ID
     * @return 删除医院
     */
    @DeleteMapping("{id}")
    public Integer deletedHospital(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        overviewService.checkHospital(user, id);
        return hospitalService.deletedHospital(id, user.getId(), user.getOrgId());
    }

    /**
     * 通过医院ID获取医院
     *
     * @param id 医院ID
     * @return 医院实体
     */
    @GetMapping("{id}")
    public HospitalResponseDTO getHospital(@PathVariable("id") Integer id) {
        overviewService.checkHospital(CurrentUserUtil.getCurrentUser(), id);
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
    public IPage<HospitalResponseDTO> getHospitalList(PageRequest pageRequest, HospitalQuery query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isOverviewUser()) {
            query.setIds(overviewService.getBindHospital(user.getOrgId()));
            if (CollectionUtils.isEmpty(query.getIds())) {
                return new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
            }
        }
        return hospitalBizService.getHospitalList(pageRequest, query, user);
    }

    /**
     * 更新医院状态
     *
     * @param statusRequest 请求入参
     * @return 更新医院
     */
    @PutMapping("status")
    public Integer updateStatus(@RequestBody @Valid StatusRequest statusRequest) {
        overviewService.checkHospital(CurrentUserUtil.getCurrentUser(), statusRequest.getId());
        return hospitalService.updateStatus(statusRequest);
    }

    /**
     * 更新医院管理员状态
     *
     * @param statusRequest 请求入参
     * @return 更新结果
     */
    @PutMapping("/admin/status")
    public boolean updateHospitalAdminUserStatus(@RequestBody @Valid StatusRequest statusRequest) {
        overviewService.checkHospital(CurrentUserUtil.getCurrentUser(), statusRequest.getId());
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
        overviewService.checkHospital(CurrentUserUtil.getCurrentUser(), request.getId());
        return hospitalService.resetPassword(request);
    }

    /**
     * 导出医院
     *
     * @param districtId 行政区域ID
     * @see ExcelFacade
     */
    @GetMapping("/export")
    public void getHospitalExportData(Integer districtId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        exportStrategy.doExport(new ExportCondition()
                        .setNotShowTestData(currentUser.isGovDeptUser() ? Boolean.TRUE : Boolean.FALSE)
                        .setApplyExportFileUserId(currentUser.getId())
                        .setDistrictId(districtId),
                ExportExcelServiceNameConstant.HOSPITAL_EXCEL_SERVICE);
    }


    /**
     * 获取医院管理员用户账号列表
     *
     * @param hospitalId 医院Id
     * @return List<OrgAccountListDTO>
     */
    @GetMapping("/accountList/{hospitalId}")
    public List<OrgAccountListDTO> getAccountList(@PathVariable("hospitalId") Integer hospitalId) {
        overviewService.checkHospital(CurrentUserUtil.getCurrentUser(), hospitalId);
        return hospitalBizService.getAccountList(hospitalId);
    }

    /**
     * 添加用户
     *
     * @param hospitalId 请求入参
     * @return UsernameAndPasswordDTO
     */
    @PostMapping("/add/account/{hospitalId}")
    public UsernameAndPasswordDTO addAccount(@PathVariable("hospitalId")  Integer hospitalId) {
        overviewService.checkHospital(CurrentUserUtil.getCurrentUser(), hospitalId);
        return hospitalBizService.addHospitalAdminUserAccount(hospitalId);
    }

    /**
     * 通过医院名称获取医院列表
     * @param name
     * @return
     */
    @GetMapping("/byName")
    public List<HospitalResponseDTO> getByName(String name) {
        return hospitalBizService.getHospitalByName(name, null);
    }

    /**
     * 通过医院名称及行政区域（同省级下）获取医院列表
     * @param name
     * @param provinceDistrictCode
     * @param serviceType
     * @return
     */
    @GetMapping("/province/list")
    public List<HospitalResponseDTO> getProvinceList(@NotBlank(message = "筛查机构名称不能为空") String name,
                                                     @NotNull(message = "省行政区域编码不能为空") Long provinceDistrictCode,
                                                     Integer serviceType) {
        return hospitalService.getProvinceList(name, provinceDistrictCode, serviceType);
    }

}