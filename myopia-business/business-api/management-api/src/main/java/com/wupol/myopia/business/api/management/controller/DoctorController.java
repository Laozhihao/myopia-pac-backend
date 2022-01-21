package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.domain.vo.DoctorVO;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dto.DoctorDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.query.DoctorQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

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
    private HospitalDoctorService doctorService;
    @Resource
    private HospitalService hospitalService;

    /**
     * 获取医生详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public DoctorDTO getDoctor(@PathVariable("id") Integer id) {
        doctorService.checkId(id);
        return doctorService.getDetails(id);
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
        return doctorService.getPage(pageRequest, query);
    }

    /**
     * 查询当前医院有多少医生
     *
     * @param hospitalId 医院id
     * @return
     */
    @GetMapping("/findDoctorNum")
    public int  findDoctorNum(Integer hospitalId) {
        List<Doctor> doctorList = doctorService.findByList(new Doctor().setHospitalId(hospitalId));
        return doctorList.size();
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
        // 非平台管理员
        if (!user.isPlatformAdminUser()) {
            Hospital hospital = hospitalService.getById(doctor.getHospitalId());
            int totalNum = doctorService.countByHospitalId(doctor.getHospitalId());
            Assert.isTrue(totalNum < hospital.getAccountNum(), "超过人数限制");
        }
        int totalNum = doctorService.countByHospitalId(doctor.getHospitalId());
        UsernameAndPasswordDTO usernameAndPasswordDTO = doctorService.saveDoctor(doctor);
        return DoctorVO.parseFromUsernameAndPasswordDTO(usernameAndPasswordDTO).setDoctorTotalNum(totalNum);
    }

    /**
     * 更新医生
     * @param doctor
     * @return
     */
    @PutMapping
    public UsernameAndPasswordDTO updateDoctor(@RequestBody @Valid DoctorDTO doctor) {
        doctorService.checkId(doctor.getId());
        return doctorService.updateDoctor(doctor);
    }

    /**
     * 更新医生状态
     *
     * @param statusRequest 请求入参
     * @return 更新结果
     */
    @PutMapping("/status")
    public User updateDoctorStatus(@RequestBody @Valid StatusRequest statusRequest) {
        doctorService.checkId(statusRequest.getId());
        return doctorService.updateStatus(statusRequest);
    }

    /**
     * 重置密码
     *
     * @param request 请求入参
     * @return 账号密码 {@link UsernameAndPasswordDTO}
     */
    @PutMapping("/reset")
    public UsernameAndPasswordDTO resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        doctorService.checkId(request.getId());
        return doctorService.resetPassword(request);
    }

}
