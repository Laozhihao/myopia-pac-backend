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
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OrgAccountListDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * 医院控制层
 *
 * @author Simple4H
 */
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
        } else { // 非平台管理员默认为合作医院
            hospital.setIsCooperation(CommonConst.IS_COOPERATION);
            hospital.initCooperationInfo();     // 默认合作信息
        }
        hospital.setStatus(hospital.getCooperationStopStatus());
        UsernameAndPasswordDTO usernameAndPasswordDTO = hospitalService.saveHospital(hospital);
        // 非平台管理员屏蔽账号密码信息
        if (!user.isPlatformAdminUser()) {
            usernameAndPasswordDTO.setNoDisplay();
        }
        return usernameAndPasswordDTO;
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
        if (user.isPlatformAdminUser()){
            hospitalService.checkHospitalCooperation(hospital);
            // 设置医院状态
            hospital.setStatus(hospital.getCooperationStopStatus());
        } else {
            // 非平台管理员无法更新合作信息
            hospital.clearCooperationInfo();
            hospital.setStatus(null);
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
        return hospitalService.deletedHospital(id, user.getId(), user.getOrgId());
    }

    /**
     * 通过医院ID获取医院
     *
     * @param id 医院ID
     * @return 医院实体
     */
    @GetMapping("{id}")
    public Hospital getHospital(@PathVariable("id") Integer id) {
        return hospitalService.getById(id);
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
     * 导出医院
     *
     * @param districtId 行政区域ID
     * @see ExcelFacade
     */
    @GetMapping("/export")
    public void getHospitalExportData(Integer districtId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        exportStrategy.doExport(new ExportCondition()
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
     * 处理医院历史数据，给医院管理员账号绑定角色
     *
     * @return void
     **/
    @PostMapping("/dealHistoryData")
    public void dealHistoryData() {
        hospitalBizService.dealHistoryData();
    }

}