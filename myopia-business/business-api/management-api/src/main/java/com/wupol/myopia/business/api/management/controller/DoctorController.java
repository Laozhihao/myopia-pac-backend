package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dto.DoctorDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.query.DoctorQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2021/11/30 14:34
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/doctor")
public class DoctorController {

    @Autowired
    private HospitalDoctorService baseService;

    /**
     * TODO wulizhou 用于修复医生账号问题
     */
    @PostMapping("/repair")
    public void repair() {
        baseService.repair(CurrentUserUtil.getCurrentUser().getId());
    }

    /**
     * 获取医生详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public DoctorDTO getDoctor(@PathVariable("id") Integer id) {
        checkId(id);
        return baseService.getDetails(id);
    }

    /**
     * 医生列表
     *
     * @param pageRequest 分页请求
     * @param query       分页条件
     * @return
     */
    @GetMapping("/list")
    public IPage<DoctorDTO> getDoctorList(PageRequest pageRequest, DoctorQuery query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isHospitalUser()) {
            query.setHospitalId(user.getOrgId());
        }
        return baseService.getPage(pageRequest, query);
    }

    /**
     * 添加医生
     * @param doctor
     * @return
     */
    @PostMapping
    public UsernameAndPasswordDTO saveDoctor(@RequestBody @Valid DoctorDTO doctor) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        doctor.setCreateUserId(user.getId())
            .setDepartmentId(-1);
        if (user.isHospitalUser()) {
            doctor.setHospitalId(user.getOrgId());
        }
        return baseService.saveDoctor(doctor);
    }

    /**
     * 更新医生
     * @param doctor
     * @return
     */
    @PutMapping
    public UsernameAndPasswordDTO updateDoctor(@RequestBody @Valid DoctorDTO doctor) {
        checkId(doctor.getId());
        return baseService.updateDoctor(doctor);
    }

    /**
     * 更新医生状态
     *
     * @param statusRequest 请求入参
     * @return 更新结果
     */
    @PutMapping("/status")
    public User updateDoctorStatus(@RequestBody @Valid StatusRequest statusRequest) {
        checkId(statusRequest.getId());
        return baseService.updateStatus(statusRequest);
    }

    /**
     * 重置密码
     *
     * @param request 请求入参
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PutMapping("/reset")
    public UsernameAndPasswordDTO resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        checkId(request.getId());
        return baseService.resetPassword(request);
    }

    private void checkId(Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isHospitalUser()) {
            Doctor doctor = baseService.getById(id);
            if (Objects.isNull(doctor) || !user.getOrgId().equals(doctor.getHospitalId())) {
                throw new BusinessException("非法请求", ResultCode.USER_ACCESS_UNAUTHORIZED.getCode());
            }
        }
    }

}
