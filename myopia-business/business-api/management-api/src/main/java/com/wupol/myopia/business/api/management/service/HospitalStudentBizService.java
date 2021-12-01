package com.wupol.myopia.business.api.management.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentRequestDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentResponseDTO;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 医院学生
 *
 * @author Simple4H
 */
@Service
public class HospitalStudentBizService {

    @Resource
    private HospitalStudentService hospitalStudentService;

    /**
     * 获取医院学生
     *
     * @param pageRequest 分页请求
     * @param requestDTO  条件
     * @return IPage<HospitalStudentResponseDTO>
     */
    public IPage<HospitalStudentResponseDTO> getHospitalStudent(PageRequest pageRequest, HospitalStudentRequestDTO requestDTO) {
        return hospitalStudentService.getByList(pageRequest, requestDTO);
    }

    /**
     * 通过Id获取患者
     *
     * @param id 医院学生Id
     * @return HospitalStudentResponseDTO
     */
    public HospitalStudentResponseDTO getByHospitalStudentId(Integer id) {
        HospitalStudentResponseDTO hospitalStudent = hospitalStudentService.getByHospitalStudentId(id);
        return hospitalStudent;
    }
}
