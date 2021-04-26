package com.wupol.myopia.business.api.hospital.app.service;

import com.wupol.myopia.business.api.hospital.app.domain.vo.DoctorVO;
import com.wupol.myopia.business.core.hospital.domain.dto.DoctorDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.query.DoctorQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021/4/20
 **/
@Service
public class DoctorService {

    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private HospitalDoctorService hospitalDoctorService;

    /**
     * 获取医生列表
     * @param query 查询条件
     * @return
     */
    public List<DoctorVO> getDoctorVoList(DoctorQuery query)  {
        List<DoctorDTO> list = hospitalDoctorService.getDoctorVoList(query);
        List<DoctorVO> voList = new ArrayList<>();
        list.forEach(item-> {
            DoctorVO doctorVo = new DoctorVO();
            BeanUtils.copyProperties(item, doctorVo);
            if (Objects.nonNull(item.getAvatarFileId()) && item.getAvatarFileId() != 0) {
                doctorVo.setAvatarUrl(resourceFileService.getResourcePath(item.getAvatarFileId()));
            }
            voList.add(doctorVo);
        });
        return voList;
    }

    /**
     * 获取医生，带Vo
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @return
     */
    public DoctorVO getDoctorVo(Integer hospitalId, Integer doctorId) {
        Doctor doctor = hospitalDoctorService.getDoctor(hospitalId, doctorId);
        DoctorVO doctorVo = new DoctorVO();
        BeanUtils.copyProperties(doctor, doctorVo);
        return doctorVo.setAvatarUrl(resourceFileService.getResourcePath(doctor.getAvatarFileId()))
                .setSignUrl(resourceFileService.getResourcePath(doctor.getSignFileId()));
    }
}
