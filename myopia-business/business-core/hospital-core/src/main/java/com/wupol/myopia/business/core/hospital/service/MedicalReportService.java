package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.framework.core.util.DateFormatUtil;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.dos.MedicalReportDO;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalReportRequestDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.StudentReportResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.MedicalReportMapper;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.core.hospital.domain.query.MedicalReportQuery;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 医院-检查报告
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class MedicalReportService extends BaseService<MedicalReportMapper, MedicalReport> {

    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private RedisUtil redisUtil;


    /**
     * 获取学生的今天的最新一份报告, 没有则创建
     *
     * @param hospitalId 医院ID
     * @param studentId 学生ID
     * @return MedicalReport
     */
    public MedicalReportDO getOrCreateTodayLastMedicalReportDO(Integer hospitalId, Integer studentId) {
        MedicalReportDO reportDO = getTodayLastMedicalReport(hospitalId, studentId);
        if (Objects.isNull(reportDO)) {
            return new MedicalReportDO();
        }
        if (!CollectionUtils.isEmpty(reportDO.getImageIdList())) {
            reportDO.setImageUrlList(resourceFileService.getBatchResourcePath(reportDO.getImageIdList()));
        }
        return reportDO;
    }

    /**
     * 保存报告
     * @param medicalReport    检查单
     * @param hospitalId 医院id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveReport(MedicalReport medicalReport,
                           Integer hospitalId,
                           Integer doctorId,
                           Integer studentId) {
        saveReport(medicalReport, hospitalId, -1, doctorId, studentId);
    }

    /**
     * 创建报告
     * @param medicalReport    检查单
     * @param hospitalId 医院id
     * @param departmentId 科室id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveReport(MedicalReport medicalReport, Integer hospitalId, Integer departmentId, Integer doctorId, Integer studentId) {
        MedicalRecord medicalRecord = medicalRecordService.getTodayLastMedicalRecord(hospitalId, studentId);
        if (Objects.isNull(medicalRecord)) {
            throw new BusinessException("无检查数据，不可录入诊断处方");
        }

        MedicalReport dbReport = getById(medicalReport.getId());
        if (Objects.nonNull(dbReport)) {
            dbReport.setGlassesSituation(medicalReport.getGlassesSituation())
                    .setMedicalContent(medicalReport.getMedicalContent())
                    .setImageIdList(medicalReport.getImageIdList())
                    .setDoctorId(medicalReport.getDoctorId());
        } else {
            dbReport = medicalReport;
        }
        dbReport.setMedicalRecordId(medicalRecord.getId());
        saveOrUpdate(dbReport);
    }

    /**
     * 创建报告
     * @param medicalRecordId 检查单id
     * @param hospitalId 医院id
     * @param departmentId 科室id
     * @param doctorId 医生id
     * @param studentId 学生id
     */
    public MedicalReport createMedicalReport(Integer medicalRecordId, Integer hospitalId, Integer departmentId, Integer doctorId, Integer studentId) {
        MedicalReport medicalReport = new MedicalReport()
                .setNo(generateReportSn(hospitalId))
                .setMedicalRecordId(medicalRecordId)
                .setHospitalId(hospitalId)
                .setDepartmentId(departmentId)
                .setDoctorId(doctorId)
                .setStudentId(studentId);
        if (!save(medicalReport)) {
            throw new BusinessException("创建报告失败");
        }
        return medicalReport;
    }

    /**
     * 报告编号：医院ID+生成日期时分秒（202011111111）+6位数排序（000001开始）
     * @param hospitalId 医院id
     * @return
     */
    private String generateReportSn(Integer hospitalId) {
        String sn = DateFormatUtil.format(new Date(), DateFormatUtil.FORMAT_DATE_AND_TIME_WITHOUT_SEPERATOR);
        String key = sn.substring(0,8);
        sn = hospitalId + sn;
        key = hospitalId + key;
        Long count = redisUtil.incr(key, 1L);
        return String.format(sn+"%06d", count);
    }


    /**
     * 获取学生报告列表
     * @param studentId 学生id
     * @return List<MedicalReportVo>
     */
    public List<MedicalReportDO> getReportListByStudentId(Integer hospitalId, Integer studentId) {
        MedicalReportQuery query = new MedicalReportQuery();
        query.setStudentId(studentId).setHospitalId(hospitalId);
        return baseMapper.getMedicalReportDoList(query);
    }

    /**
     * 获取学生的就诊档案详情（报告）
     *
     * @param reportId 报告ID
     * @return responseDTO
     */
    public StudentReportResponseDTO getStudentReport(Integer hospitalId, Integer reportId) {
        StudentReportResponseDTO responseDTO = new StudentReportResponseDTO();
        MedicalReportQuery reportQuery = new MedicalReportQuery();
        reportQuery.setHospitalId(hospitalId).setId(reportId);
        // 报告
        MedicalReport report = getMedicalReportList(reportQuery).stream().findFirst().orElseThrow(()-> new BusinessException("未找到该报告"));
        responseDTO.setReport(report);
        // 检查单
        MedicalRecord record = medicalRecordService.getById(report.getId());
        responseDTO.setRecord(record);

        return responseDTO;
    }


    /**
     * 获取学生的最新一份报告
     *
     * @param studentId 学生ID
     * @return MedicalReport
     */
    public MedicalReport getLastOneByStudentId(Integer studentId) {
        return baseMapper.getLastOneByStudentId(studentId);
    }

    /**
     * 获取最新一条
     *
     * @param query 查询条件
     * @return com.wupol.myopia.business.core.hospital.domain.model.MedicalReport
     **/
    public MedicalReport getLastOne(MedicalReportQuery query) {
        return baseMapper.getLastOne(query);
    }

    public List<MedicalReport> getMedicalReportList(MedicalReportQuery query) {
        return baseMapper.getMedicalReportList(query);
    }

    /**
     * 通过学生ID(只取当前时间的前一天)
     *
     * @param studentId 学生ID
     * @return List<ReportAndRecordVo>
     */
    public List<ReportAndRecordDO> getByStudentId(Integer studentId) {
        return baseMapper.getStudentId(studentId);
    }

    /**
     * 通过学生ID(只取当前时间的前一天)
     *
     * @param pageRequest 分页请求
     * @param studentId   学生Id
     * @param hospitalId  医院Id
     * @return List<ReportAndRecordVo>
     */
    public IPage<ReportAndRecordDO> getByStudentIdWithPage(PageRequest pageRequest, Integer studentId, Integer hospitalId) {
        return baseMapper.getByStudentIdWithPage(pageRequest.toPage(), studentId, hospitalId);
    }

    /**
     * 批量获取列表通过学生Ids(只取当前时间的前一天)
     *
     * @param studentIds 学生Ids
     * @return List<ReportAndRecordVo>
     */
    public List<ReportAndRecordDO> getByStudentIds(List<Integer> studentIds) {
        return baseMapper.getByStudentIds(studentIds);
    }

    /**
     * 获取学生今天最后一条报告
     * @param hospitalId 医院id
     * @param studentId 学生id
     */
    public MedicalReportDO getTodayLastMedicalReport(Integer hospitalId, Integer studentId) {
        return baseMapper.getTodayLastMedicalReportDO(hospitalId, studentId);
    }

    /** 获取未生成固化结论的报告列表 */
    public List<MedicalReport> getInconclusiveReportList() {
        return baseMapper.getInconclusiveReportList();
    }

    /**
     * 通过Ids获取报告
     *
     * @param ids id
     * @return List<MedicalReport>
     */
    public List<MedicalReport> getByIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return baseMapper.getByIds(ids);
    }

    /**
     * 通过学生ID(只取当前时间的前一天)
     *
     * @param pageRequest 分页请求
     * @param requestDTO  医院就诊报告DTO
     * @return List<ReportAndRecordVo>
     */
    public IPage<ReportAndRecordDO> getByHospitalId(PageRequest pageRequest, HospitalReportRequestDTO requestDTO) {
        return baseMapper.getByHospitalId(pageRequest.toPage().setOptimizeCountSql(false), requestDTO);
    }

    /**
     * 通过学生Ids和医院Id获取就诊报告
     *
     * @param studentIds 学生Ids
     * @param hospitalId 医院Id
     * @return List<ReportAndRecordDO>
     */
    public List<ReportAndRecordDO> getByStudentIdsAndHospitalId(List<Integer> studentIds, Integer hospitalId) {
        return baseMapper.getByStudentIdsAndHospitalId(studentIds, hospitalId);
    }

    /**
     * 批量获取列表通过学生Ids(只取当前时间的前一天)
     *
     * @param studentIds 学生Ids
     *
     * @return List<ReportAndRecordVo>
     */
    public Map<Integer, List<ReportAndRecordDO>> getMapByStudentIds(List<Integer> studentIds) {
        List<ReportAndRecordDO> visitLists = getByStudentIds(studentIds);
        return visitLists.stream().collect(Collectors.groupingBy(ReportAndRecordDO::getStudentId));
    }
}