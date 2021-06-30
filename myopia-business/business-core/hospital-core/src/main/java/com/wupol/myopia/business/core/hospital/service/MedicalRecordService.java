package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.mapper.MedicalRecordMapper;
import com.wupol.myopia.business.core.hospital.domain.model.*;
import com.wupol.myopia.business.core.hospital.domain.query.MedicalRecordQuery;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 医院-检查单
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class MedicalRecordService extends BaseService<MedicalRecordMapper, MedicalRecord> {


    @Autowired
    private MedicalReportService medicalReportService;
    @Autowired
    private ResourceFileService resourceFileService;

    /**
     * 查找某段时候内, studentIds是否存在就诊记录.
     * @param studentIds
     * @return 返回的结果中, null是代表没有存在记录, true代表有存在记录. 方便判断
     */
    public  Set<Integer> getMedicalRecordStudentIds(Set<Integer> studentIds, Date startDate, Date endDate) {
        if (CollectionUtils.isEmpty(studentIds)|| startDate == null || endDate == null) {
            return Collections.emptySet();
        }
        LambdaQueryWrapper<MedicalRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.le(MedicalRecord::getUpdateTime,startDate).gt(MedicalRecord::getUpdateTime,endDate);
        lambdaQueryWrapper.in(MedicalRecord::getStudentId,studentIds);
        List<MedicalRecord> medicalRecords = list(lambdaQueryWrapper);
        return medicalRecords.stream().map(MedicalRecord::getStudentId).collect(Collectors.toSet());
    }

    /**
     * 获取检查单信息，包含数据对比的内容
     * @param hospitalId        医院id
     * @param medicalRecordId   检查单id
     * @param studentId         学生id
     */
    public CompareMedicalRecord getMedicalRecordWithCompare(Integer hospitalId, Integer medicalRecordId, Integer studentId) {
        MedicalRecord medicalRecord = getMedicalRecord(hospitalId, medicalRecordId);
        CompareMedicalRecord compareMedicalRecord = new CompareMedicalRecord();
        BeanUtils.copyProperties(medicalRecord, compareMedicalRecord);
        // 除最新一条外的最近6条就诊记录
        List<MedicalRecordDate> medicalRecordDateList = baseMapper.getMedicalRecordDateList(hospitalId, studentId);

        // 设置角膜地形图的图片
        generateToscaImageUrls(medicalRecord);

        // 没有旧记录，直接返回
        if (CollectionUtils.isEmpty(medicalRecordDateList)) {
            return compareMedicalRecord;
        }
        // 获取除最新一条外的最新记录
        MedicalRecord lastMedicalRecord = getMedicalRecord(hospitalId, medicalRecordDateList.stream().findFirst().get().getMedicalRecordId());
        compareMedicalRecord.setCompareDateList(medicalRecordDateList)
                .setCompareBiometrics(lastMedicalRecord.getBiometrics())
                .setCompareDiopter(lastMedicalRecord.getDiopter());

        return compareMedicalRecord;
    }


    /**
     * 获取学生今天最后一条角膜地形图数据
     * @param hospitalId 医院id
     * @param studentId 学生id
     */
    public ToscaMedicalRecord getTodayLastToscaMedicalRecord(Integer hospitalId, Integer studentId) {
        MedicalRecord medicalRecord = getTodayLastMedicalRecord(hospitalId, studentId);
        if (Objects.isNull(medicalRecord) || Objects.isNull(medicalRecord.getTosca())) {
            return new ToscaMedicalRecord();
        }
        // 设置角膜地形图的图片
        generateToscaImageUrls(medicalRecord);
        return medicalRecord.getTosca();
    }

    /**
     * 追加检查检查数据到检查单
     * @param consultation    问诊
     * @param vision    视力检查检查数据
     * @param biometrics    生物测量检查数据
     * @param diopter    屈光检查数据
     * @param tosca    角膜地形图检查数据
     * @param hospitalId 医院id
     * @param departmentId 科室id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public void addCheckDataToMedicalRecord(Consultation consultation,
                                            VisionMedicalRecord vision,
                                            BiometricsMedicalRecord biometrics,
                                            DiopterMedicalRecord diopter,
                                            ToscaMedicalRecord tosca,
                                            Integer hospitalId,
                                            Integer departmentId,
                                            Integer doctorId,
                                            Integer studentId) {
        MedicalRecord medicalRecord = getOrCreateTodayMedicalRecord(hospitalId, departmentId, doctorId, studentId);
        if (Objects.nonNull(consultation)) {
            medicalRecord.setConsultation(consultation);
        }
        if (Objects.nonNull(vision)) medicalRecord.setVision(vision);
        if (Objects.nonNull(biometrics)) medicalRecord.setBiometrics(biometrics);
        if (Objects.nonNull(diopter)) medicalRecord.setDiopter(diopter);
        if (Objects.nonNull(tosca)) medicalRecord.setTosca(tosca);
        if (!updateById(medicalRecord)) {
            throw new BusinessException("修改失败");
        }
    }

    /**
     * 获取 或者 创建 学生今天最后一条问诊。
     * 如果学生未建档，则自动建档
     * @param hospitalId 医院id
     * @param departmentId 科室id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public MedicalRecord getOrCreateTodayMedicalRecord(Integer hospitalId,
                                                       Integer departmentId,
                                                       Integer doctorId,
                                                       Integer studentId) {
        MedicalRecord medicalRecord = getTodayLastMedicalRecord(hospitalId, studentId);
        if (Objects.nonNull(medicalRecord)) {
            return medicalRecord;
        }
        medicalRecord = createMedicalRecord(hospitalId, departmentId, doctorId, studentId);
        // 创建检查单的同时,创建对应的报告
        medicalReportService.createMedicalReport(medicalRecord.getId(), hospitalId, departmentId, doctorId, studentId);
        return medicalRecord;
    }
    
    /**
     * 获取学生最后一条检查记录
     * @param studentId 学生id
     * @return
     */
    public MedicalRecord getLastOneByStudentId(Integer studentId) {
        return baseMapper.getLastOneByStudentId(studentId);
    }

    public List<MedicalRecord> getBy(MedicalRecordQuery query) {
        return baseMapper.getBy(query);
    }

    /**
     * 获取今天最新的3个就诊的学生id
     * @param hospitalId 医院id
     * @throws IOException
     */
    public List<Integer> getTodayLastThreeStudentList(Integer hospitalId) {
        // 今天建档的患者姓名【前3名】
        return findByPage(new MedicalRecord().setHospitalId(hospitalId), 0, 3)
                .getRecords().stream()
                .filter(item-> DateUtils.isSameDay(item.getCreateTime(), new Date()))
                .map(MedicalRecord::getStudentId).collect(Collectors.toList());
    }

    /**
     * 完成检查单
     * @param medicalRecord    检查单
     */
    public void finishMedicalRecord(MedicalRecord medicalRecord) {
        if (medicalRecord.isFinish()) {
            throw new BusinessException(String.format("该检查单已经完成. id=%s", medicalRecord.getId()));
        }
        medicalRecord.setStatus(MedicalRecord.STATUS_FINISH);
        if (!updateById(medicalRecord)) {
            throw new BusinessException("完成检查单失败");
        }
    }

    /**
     * 获取学生今天最后一条检查单
     * @param hospitalId 医院id
     * @param studentId 学生id
     */
    public MedicalRecord getTodayLastMedicalRecord(Integer hospitalId, Integer studentId) {
        return baseMapper.getTodayLastMedicalRecord(hospitalId, studentId);
    }

    /** 创建检查单 */
    public MedicalRecord createMedicalRecord(Integer hospitalId,
                                             Integer doctorId,
                                             Integer studentId) {
        return createMedicalRecord(hospitalId, -1, doctorId, studentId);
    }

    /**
     * 获取检查单
     * @param hospitalId   医院id
     * @param medicalRecordId   检查单id
     */
    public MedicalRecord getMedicalRecord(Integer hospitalId, Integer medicalRecordId) {
        MedicalRecordQuery query = new MedicalRecordQuery();
        query.setHospitalId(hospitalId).setId(medicalRecordId);
        return baseMapper.getBy(query).stream().findFirst().orElseThrow(()-> new BusinessException("未找到检查单. id:"+medicalRecordId));
    }

    /** 生成并设置角膜地形图的图片url */
    public void generateToscaImageUrls(MedicalRecord medicalRecord) {
        if (Objects.isNull(medicalRecord.getTosca())) {
            return;
        }
        ToscaMedicalRecord.Tosco nonMydriasis = medicalRecord.getTosca().getNonMydriasis();
        if (Objects.nonNull(nonMydriasis)) {
            nonMydriasis.setImageUrlList(resourceFileService.getBatchResourcePath(nonMydriasis.getImageIdList()));
        }
        ToscaMedicalRecord.Tosco mydriasis = medicalRecord.getTosca().getMydriasis();
        if (Objects.nonNull(mydriasis)) {
            mydriasis.setImageUrlList(resourceFileService.getBatchResourcePath(mydriasis.getImageIdList()));
        }
    }

   /** 创建检查单 */
    private MedicalRecord createMedicalRecord(Integer hospitalId,
                                             Integer departmentId,
                                             Integer doctorId,
                                             Integer studentId) {
        MedicalRecord medicalRecord = new MedicalRecord().setHospitalId(hospitalId)
                .setDepartmentId(departmentId)
                .setDoctorId(doctorId)
                .setStudentId(studentId);
        baseMapper.insert(medicalRecord);
        return medicalRecord;
    }
}