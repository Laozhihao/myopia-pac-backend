package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.api.management.service.HospitalBizService;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

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

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private ExcelFacade excelFacade;

    @Autowired
    private HospitalBizService hospitalBizService;

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
        return hospitalService.saveHospital(hospital);
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
        hospital.setCreateUserId(user.getId());
        hospital.setGovDeptId(user.getOrgId());
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
        return hospitalBizService.getHospitalList(pageRequest, query, user.getOrgId());
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
     * 重置密码
     *
     * @param request 请求入参
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PostMapping("reset")
    public UsernameAndPasswordDTO resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return hospitalService.resetPassword(request.getId());
    }

    /**
     * 导出医院
     *
     * @param districtId 行政区域ID
     * @return 是否导出成功
     * @throws IOException   IO异常
     * @throws UtilException 工具异常
     * @see ExcelFacade
     */
    @GetMapping("/export")
    public void getHospitalExportData(Integer districtId) throws IOException, UtilException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        excelFacade.generateHospital(currentUser.getId(), districtId);
    }
}