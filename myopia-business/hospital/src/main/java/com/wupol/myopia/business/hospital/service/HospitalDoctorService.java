package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.hospital.domain.mapper.DepartmentMapper;
import com.wupol.myopia.business.hospital.domain.mapper.DoctorMapper;
import com.wupol.myopia.business.hospital.domain.model.Department;
import com.wupol.myopia.business.hospital.domain.model.Doctor;
import com.wupol.myopia.business.hospital.domain.vo.DoctorVo;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.service.ResourceFileService;
import com.wupol.myopia.business.management.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 医院-医生
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class HospitalDoctorService extends BaseService<DoctorMapper, Doctor> {

    @Autowired
    private UserService userService;
    @Autowired
    private ResourceFileService resourceFileService;

    /**
     * 获取医生列表
     * @param hospitalId 医院id
     * @param like  名称 / 科室 / 职称
     * @return
     */
    public List<DoctorVo> getDoctorVoList(Integer hospitalId,
                                      String like) throws IOException {
        //TODO 待模糊查询
        //TODO 待查询出报告数
        List<Doctor> doctorList = baseMapper.getBy(new Doctor().setHospitalId(hospitalId));
        return doctorList.stream().map(this::getDoctorVo).collect(Collectors.toList());
    }

    /**
     * 获取医生列表
     * @param hospitalId 医院id
     * @param like  名称 / 科室 / 职称
     * @return
     */
    public List<Doctor> getDoctorList(Integer hospitalId,
                                      String like) throws IOException {
        //TODO 待模糊查询
        return baseMapper.getBy(new Doctor().setHospitalId(hospitalId));
    }

    /**
     * 获取医生详情
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @return
     */
    public DoctorVo getDoctor(Integer hospitalId, Integer doctorId) throws IOException {
        Doctor doctor = getById(doctorId);
        return getDoctorVo(doctor);
    }

    /**
     * 更新医生信息
     * @param user 当前用户
     * @param doctor 医生信息
     * @return
     */
    public void saveDoctor(CurrentUser user, Doctor doctor) {
        doctor.setHospitalId(user.getOrgId())
                .setDepartmentId(-1);
        if (!saveOrUpdate(doctor)) {
            throw new BusinessException("保存医生信息失败");
        }
    }

    /**
     * 删除医生
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @return
     */
    public void deleteDoctor(Integer hospitalId, Integer doctorId) throws IOException {
        if (!removeById(doctorId)) {
            throw new BusinessException("删除失败");
        }
    }

    /**
     * 获取医生，带Vo
     * @param doctor
     * @return
     * @throws IOException
     */
    public DoctorVo getDoctorVo(Doctor doctor) {
        DoctorVo doctorVo = new DoctorVo();
        BeanUtils.copyProperties(doctor, doctorVo);
        return doctorVo.setAvatarUrl(resourceFileService.getResourcePath(doctor.getAvatarFileId()))
                .setSignUrl(resourceFileService.getResourcePath(doctor.getSignFileId()));
    }

}