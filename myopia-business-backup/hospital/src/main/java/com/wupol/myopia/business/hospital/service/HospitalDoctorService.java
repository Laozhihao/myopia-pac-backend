package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.hospital.domain.mapper.DoctorMapper;
import com.wupol.myopia.business.hospital.domain.model.Doctor;
import com.wupol.myopia.business.hospital.domain.query.DoctorQuery;
import com.wupol.myopia.business.hospital.domain.vo.DoctorVo;
import com.wupol.myopia.business.management.service.ResourceFileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 医院-医生
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class HospitalDoctorService extends BaseService<DoctorMapper, Doctor> {

    @Autowired
    private ResourceFileService resourceFileService;

    /**
     * 获取医生列表
     * @param query 查询条件
     * @return
     */
    public List<DoctorVo> getDoctorVoList(DoctorQuery query)  {
        List<DoctorVo> list = baseMapper.getDoctorVoList(query);
        list.forEach(item-> {
            if (Objects.nonNull(item.getAvatarFileId()) && item.getAvatarFileId() != 0) {
                item.setAvatarUrl(resourceFileService.getResourcePath(item.getAvatarFileId()));
            }

        });
        return list;
    }

    /**
     * 获取医生详情
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @return
     */
    public Doctor getDoctor(Integer hospitalId, Integer doctorId) {
        DoctorQuery query = new DoctorQuery();
        query.setHospitalId(hospitalId).setId(doctorId);
        Doctor doctor = baseMapper.getBy(query)
                .stream().findFirst().orElseThrow(()-> new BusinessException("未找到该医生"));
        return doctor;
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
    public void deleteDoctor(Integer hospitalId, Integer doctorId) {
        Doctor doctor = getDoctor(hospitalId, doctorId);
        if (!removeById(doctor.getId())) {
            throw new BusinessException("删除失败");
        }
    }

    /**
     * 获取医生，带Vo
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @return
     */
    public DoctorVo getDoctorVo(Integer hospitalId, Integer doctorId) {
        Doctor doctor = getDoctor(hospitalId, doctorId);
        DoctorVo doctorVo = new DoctorVo();
        BeanUtils.copyProperties(doctor, doctorVo);
        return doctorVo.setAvatarUrl(resourceFileService.getResourcePath(doctor.getAvatarFileId()))
                .setSignUrl(resourceFileService.getResourcePath(doctor.getSignFileId()));
    }


}