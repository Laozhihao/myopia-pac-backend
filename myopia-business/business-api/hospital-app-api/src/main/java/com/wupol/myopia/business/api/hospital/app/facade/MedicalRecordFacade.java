package com.wupol.myopia.business.api.hospital.app.facade;

import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.hospital.domain.vo.HospitalStudentVO;
import com.wupol.myopia.business.aggregation.hospital.service.HospitalAggService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.hospital.constant.HospitalCacheKey;
import com.wupol.myopia.business.core.hospital.domain.model.*;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.hospital.service.MedicalRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author HaoHao
 * @Date 2021/4/21
 **/
@Slf4j
@Service
public class MedicalRecordFacade {

    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private HospitalStudentService hospitalStudentService;
    @Autowired
    private HospitalStudentFacade hospitalStudentFacade;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HospitalAggService hospitalAggService;

    /**
     * 追加检查检查数据到检查单, 如果该学生未建档，则自动建档
     *  @param consultation 问诊
     * @param vision       视力检查检查数据
     * @param biometrics   生物测量检查数据
     * @param diopter      屈光检查数据
     * @param tosca        角膜地形图检查数据
     * @param hospitalId   医院id
     * @param doctorId     医生id
     * @param studentId    学生id
     * @param clientId   系统编码
     */
    @Transactional(rollbackFor = Exception.class)
    public void addCheckDataAndCreateStudent(Consultation consultation,
                                             VisionMedicalRecord vision,
                                             BiometricsMedicalRecord biometrics,
                                             DiopterMedicalRecord diopter,
                                             ToscaMedicalRecord tosca,
                                             EyePressure eyePressure,
                                             Integer hospitalId,
                                             Integer doctorId,
                                             Integer studentId,
                                             String clientId) {
        if (Objects.isNull(studentId)) {
            throw new BusinessException("学生id不能为空");
        }
        // 追加检查单数据
        medicalRecordService.addCheckDataToMedicalRecord(consultation, vision, biometrics, diopter, tosca, eyePressure, hospitalId, -1, doctorId, studentId);
        // 已建档则跳过
        String cacheKey = String.format(HospitalCacheKey.EXIST_HOSPITAL_STUDENT_ID, hospitalId, studentId);
        if (redisUtil.hasKey(cacheKey)) {
            updateHospitalStudentStatus(hospitalId, studentId, clientId);
            return;
        }
        if (hospitalStudentService.count(new HospitalStudent().setStudentId(studentId).setHospitalId(hospitalId)) != 0) {
            // 设置标识，一天内只通过缓存查询患者信息
            redisUtil.set(cacheKey, "", TimeUnit.DAYS.toSeconds(1));
            updateHospitalStudentStatus(hospitalId, studentId, clientId);
            return;
        }
        HospitalStudentVO hospitalStudentVO = hospitalAggService.getStudentById(hospitalId, studentId).getFirst();
        hospitalStudentVO.setHospitalId(hospitalId);
        hospitalStudentVO.setStatus(CommonConst.STATUS_NOT_DELETED);
        // 未建档则建档
        hospitalStudentVO.setStudentType(hospitalStudentService.getStudentType(clientId, hospitalStudentVO.getStudentType()));
        hospitalStudentFacade.saveStudent(hospitalStudentVO, false);
        // 设置标识，一天内只通过缓存查询患者信息
        redisUtil.set(cacheKey, "", TimeUnit.DAYS.toSeconds(1));
    }

    /**
     * 更新医院学生状态
     *
     * @param hospitalId 医院Id
     * @param studentId  学生Id
     * @param clientId   客户端Id
     */
    private void updateHospitalStudentStatus(Integer hospitalId, Integer studentId,String clientId) {
        HospitalStudentVO hospitalStudentVO = hospitalAggService.getStudentById(hospitalId, studentId).getFirst();
        if (Objects.isNull(hospitalStudentVO)) {
            return;
        }
        hospitalStudentVO.setStudentType(hospitalStudentService.getStudentType(clientId, hospitalStudentVO.getStudentType()));
        hospitalStudentService.updateById(hospitalStudentVO);
        if (CommonConst.STATUS_NOT_DELETED.equals(hospitalStudentVO.getStatus())) {
            return;
        }
        // 删除的学生重新启用
        hospitalStudentVO.setHospitalId(hospitalId);
        hospitalStudentVO.setStatus(CommonConst.STATUS_NOT_DELETED);
        hospitalStudentService.updateById(hospitalStudentVO);
    }

}
