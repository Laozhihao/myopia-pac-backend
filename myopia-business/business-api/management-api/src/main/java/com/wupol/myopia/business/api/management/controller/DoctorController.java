package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dto.DoctorDTO;
import com.wupol.myopia.business.core.hospital.domain.query.DoctorQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
     * 获取医生详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public DoctorDTO getDoctor(@PathVariable("id") Integer id) {
        baseService.checkId(id);
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
        baseService.checkId(doctor.getId());
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
        baseService.checkId(statusRequest.getId());
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
        baseService.checkId(request.getId());
        return baseService.resetPassword(request);
    }

}
