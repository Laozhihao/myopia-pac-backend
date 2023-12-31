package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.MonthAgeEnum;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BusinessUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.hospital.constant.BaseConstant;
import com.wupol.myopia.business.core.hospital.constant.CheckReferralInfoEnum;
import com.wupol.myopia.business.core.hospital.constant.MonthAgeStatusEnum;
import com.wupol.myopia.business.core.hospital.domain.dto.*;
import com.wupol.myopia.business.core.hospital.domain.mapper.PreschoolCheckRecordMapper;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.domain.query.PreschoolCheckRecordQuery;
import com.wupol.myopia.business.core.hospital.util.HospitalUtil;
import com.wupol.myopia.business.core.hospital.util.PreschoolCheckRecordUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Service
@Log4j2
public class PreschoolCheckRecordService extends BaseService<PreschoolCheckRecordMapper, PreschoolCheckRecord> {

    @Autowired
    private ReferralRecordService referralRecordService;

    @Autowired
    private HospitalStudentService hospitalStudentService;

    @Autowired
    private HospitalDoctorService hospitalDoctorService;

    /**
     * 获取眼保健详情
     * @param id
     * @return
     */
    public PreschoolCheckRecordDTO getDetail(Integer id) {
        PreschoolCheckRecordDTO details = baseMapper.getDetail(id);
        if (Objects.isNull(details)) {
            log.error("获取报告数据异常, 报告Id:{}", id);
            throw new BusinessException("眼保健数据异常");
        }
        // 设置家长信息
        HospitalUtil.setParentInfo(details);
        details.setCreateTimeAge(DateUtil.getAgeInfo(details.getBirthday(), details.getCreateTime()));
        // 检查单
        if (Objects.nonNull(details.getToReferralId())) {
            details.setToReferral(referralRecordService.getById(details.getToReferralId()));
        }
        // 设置医师名称
        Map<Integer, String> doctorNames = hospitalDoctorService.getDoctorNameByIds(details.getDoctorIds());
        details.setDoctorsName(HospitalUtil.getName(details.getDoctorIds(), doctorNames, BaseConstant.DOCTOR_NAME_SEPARATOR));
        return details;
    }

    /**
     * 获取默认显示信息
     * @param hospitalId
     * @param studentId
     * @return
     */
    public HospitalStudentPreschoolCheckRecordDTO getInit(Integer hospitalId, Integer currentShowCheckMonthAge, Integer studentId) {
        HospitalStudentPreschoolCheckRecordDTO init = new HospitalStudentPreschoolCheckRecordDTO();
        // 学生信息
        HospitalStudentResponseDTO student = hospitalStudentService.getByHospitalIdAndStudentId(hospitalId, studentId);
        init.setStudent(student);
        // 对应当前医院各年龄段状态
        List<PreschoolCheckRecord> records = getStudentRecord(hospitalId, studentId);
        Map<Integer, MonthAgeStatusDTO> studentCheckStatus = getStudentCheckStatus(student.getBirthday(), records);
        init.setAgeStageStatusList(createMonthAgeStatusDTOByMap(studentCheckStatus));
        // 设置当前选中的检查
        if (Objects.isNull(currentShowCheckMonthAge)) {
            currentShowCheckMonthAge = getCurrentShowCheckMonthAge(student.getBirthday(), records);
        }
        if (Objects.nonNull(studentCheckStatus.get(currentShowCheckMonthAge).getPreschoolCheckRecordId())) {
            PreschoolCheckRecordDTO details = getDetail(studentCheckStatus.get(currentShowCheckMonthAge).getPreschoolCheckRecordId());
            init.setPreschoolMedicalRecord(details);
        } else {
            PreschoolCheckRecordDTO emptyCheck = new PreschoolCheckRecordDTO();
            emptyCheck.setMonthAge(currentShowCheckMonthAge).setIsReferral(CheckReferralInfoEnum.NOT_REFERRAL.getStatus());
            init.setPreschoolMedicalRecord(emptyCheck);
        }
        // 设置学生转诊信息
        init.setFromReferral(referralRecordService.getByStudentId(studentId));
        return init;
    }

    /**
     * 获取学生检查记录
     * @param hospitalId
     * @param studentId
     * @return
     */
    public List<PreschoolCheckRecord> getStudentRecord(Integer hospitalId, Integer studentId) {
        PreschoolCheckRecord record = new PreschoolCheckRecord().setHospitalId(hospitalId).setStudentId(studentId);
        return findByList(record);
    }


    /**
     * 获取眼保健列表
     * @param pageRequest
     * @param query
     * @return
     */
    public IPage<PreschoolCheckRecordDTO> getList(PageRequest pageRequest, PreschoolCheckRecordQuery query) {
        // 如果有查询医师，先获取相关医师id再查询
        if (StringUtils.isNotBlank(query.getDoctorsName())) {
            query.setDoctorIds(hospitalDoctorService.getDoctorIdByName(query.getHospitalId(), query.getDoctorsName()));
        }
        IPage<PreschoolCheckRecordDTO> records = baseMapper.getListByCondition(pageRequest.toPage(), query);
        // 获取医生信息集
        Set<Integer> doctorIds = new HashSet<>();
        records.getRecords().forEach(record -> doctorIds.addAll(record.getDoctorIds()));
        Map<Integer, String> doctorNames = hospitalDoctorService.getDoctorNameByIds(doctorIds);
        // 设置名称
        records.getRecords().forEach(record -> {
            record.setCreateTimeAge(DateUtil.getAgeInfo(record.getBirthday(), record.getCreateTime()));
            record.setDoctorsName(HospitalUtil.getName(record.getDoctorIds(), doctorNames, BaseConstant.DOCTOR_NAME_SEPARATOR));
        });
        return records;
    }

    /**
     * 获取学生指定年龄段检查信息
     * @param hospitalId
     * @param studentId
     * @param monthAge
     * @return
     */
    public PreschoolCheckRecord getRecord(Integer hospitalId, Integer studentId, Integer monthAge) {
        Assert.notNull(hospitalId, "hospitalId不能为空");
        Assert.notNull(studentId, "studentId不能为空");
        Assert.notNull(monthAge, "monthAge不能为空");
        PreschoolCheckRecordQuery query = new PreschoolCheckRecordQuery();
        query.setHospitalId(hospitalId).setStudentId(studentId)
                .setMonthAge(monthAge);
        return baseMapper.getByOne(query);
    }

    /** 根据id获取检查单 */
    public PreschoolCheckRecord getById(Integer id, Integer hospitalId) {
        PreschoolCheckRecordQuery query = new PreschoolCheckRecordQuery();
        query.setId(id).setHospitalId(hospitalId);
        return baseMapper.getByOne(query);
    }


    /**
     * 追加检查检查数据到检查单
     */
    public void saveCheckRecord(PreschoolCheckRecord checkRecord) {

        PreschoolCheckRecord dbCheckRecord;
        if (Objects.isNull(checkRecord.getId())) {
            // 一个患者在一个医院下指定月龄只能做一次检查
            dbCheckRecord = getRecord(checkRecord.getHospitalId(), checkRecord.getStudentId(), checkRecord.getMonthAge());
            if (Objects.isNull(dbCheckRecord)) {
                addConclusionAndStatus(checkRecord);
                save(checkRecord);
                return;
            }
        } else {
            dbCheckRecord = getById(checkRecord.getId(), checkRecord.getHospitalId());
        }
        if (Objects.nonNull(checkRecord.getIsReferral())) {dbCheckRecord.setIsReferral(checkRecord.getIsReferral());}
        if (Objects.nonNull(checkRecord.getFromReferral())) {dbCheckRecord.setFromReferral(checkRecord.getFromReferral());}
        if (Objects.nonNull(checkRecord.getOuterEye())) {dbCheckRecord.setOuterEye(checkRecord.getOuterEye());}
        if (Objects.nonNull(checkRecord.getVisionData())) {dbCheckRecord.setVisionData(checkRecord.getVisionData());}
        if (Objects.nonNull(checkRecord.getRefractionData())) {dbCheckRecord.setRefractionData(checkRecord.getRefractionData());}
        if (Objects.nonNull(checkRecord.getEyeDiseaseFactor())) {dbCheckRecord.setEyeDiseaseFactor(checkRecord.getEyeDiseaseFactor());}
        if (Objects.nonNull(checkRecord.getLightReaction())) {dbCheckRecord.setLightReaction(checkRecord.getLightReaction());}
        if (Objects.nonNull(checkRecord.getBlinkReflex())) {dbCheckRecord.setBlinkReflex(checkRecord.getBlinkReflex());}
        if (Objects.nonNull(checkRecord.getRedBallTest())) {dbCheckRecord.setRedBallTest(checkRecord.getRedBallTest());}
        if (Objects.nonNull(checkRecord.getVisualBehaviorObservation())) {dbCheckRecord.setVisualBehaviorObservation(checkRecord.getVisualBehaviorObservation());}
        if (Objects.nonNull(checkRecord.getRedReflex())) {dbCheckRecord.setRedReflex(checkRecord.getRedReflex());}
        if (Objects.nonNull(checkRecord.getOcularInspection())) {dbCheckRecord.setOcularInspection(checkRecord.getOcularInspection());}
        if (Objects.nonNull(checkRecord.getMonocularMaskingAversionTest())) {dbCheckRecord.setMonocularMaskingAversionTest(checkRecord.getMonocularMaskingAversionTest());}
        if (Objects.nonNull(checkRecord.getGuideContent())) {dbCheckRecord.setGuideContent(checkRecord.getGuideContent());}
        dbCheckRecord.setUpdateTime(new Date());
        addConclusionAndStatus(dbCheckRecord);
        if (!updateById(dbCheckRecord)) {
            throw new BusinessException("修改失败");
        }
    }

    private void addConclusionAndStatus(PreschoolCheckRecord record) {
        TwoTuple<Integer, String> conclusion = PreschoolCheckRecordUtil.conclusion(record);
        record.setConclusion(conclusion.getSecond());
        record.setStatus(conclusion.getFirst());
    }

    /**
     * 获取学生13次检查的检查状态
     * @param records
     * @return
     */
    public Map<Integer, MonthAgeStatusDTO> getStudentCheckStatus(Date birthday, List<PreschoolCheckRecord> records) {
        Date now = new Date();
        LocalDate birthdayLocalDate = DateUtil.convertToLocalDate(birthday, ZoneId.systemDefault());
        Map<Integer, MonthAgeStatusDTO> monthAgeStatusDTOS = initMonthAgeStatusMap();

        // 如果 40~45岁的，则返回全部可更新的状态
        int age = LocalDate.now().getYear() - birthdayLocalDate.getYear();
        if (45 >= age && age >= 40) {
            // 先把全部修改成可点击，再把有数据的修改成可更新
            monthAgeStatusDTOS.forEach((key,list)-> monthAgeStatusDTOS.get(key).setStatus(MonthAgeStatusEnum.AGE_STAGE_STATUS_NOT_DATA.getStatus()));
            records.forEach(record -> monthAgeStatusDTOS.get(record.getMonthAge())
                    .setStatus(MonthAgeStatusEnum.AGE_STAGE_STATUS_CAN_UPDATE.getStatus())
                    .setPreschoolCheckRecordId(record.getId()));
        } else {
            List<Integer> canCheckMonthAge = BusinessUtil.getCanCheckMonthAgeByDate(birthday);
            // 设置当前可检查年龄段为AGE_STAGE_STATUS_NOT_DATA状态
            canCheckMonthAge.forEach(monthAge -> monthAgeStatusDTOS.get(monthAge).setStatus(MonthAgeStatusEnum.AGE_STAGE_STATUS_NOT_DATA.getStatus()));
            records.forEach(record -> {
                // 检查大于3天，无法修改
                if (DateUtil.betweenDay(record.getCreateTime(), now) > 3) {
                    monthAgeStatusDTOS.get(record.getMonthAge()).setStatus(MonthAgeStatusEnum.AGE_STAGE_STATUS_CANNOT_UPDATE.getStatus())
                            .setPreschoolCheckRecordId(record.getId());
                } else {
                    // 已检查，可修改
                    monthAgeStatusDTOS.get(record.getMonthAge()).setStatus(MonthAgeStatusEnum.AGE_STAGE_STATUS_CAN_UPDATE.getStatus())
                            .setPreschoolCheckRecordId(record.getId());
                }
            });
        }
        return monthAgeStatusDTOS;
    }

    /**
     * 获取当前应显示年龄段
     * @param birthday
     * @param records
     * @return
     */
    private Integer getCurrentShowCheckMonthAge(Date birthday, List<PreschoolCheckRecord> records) {
        List<Integer> canCheckMonthAge = BusinessUtil.getCanCheckMonthAgeByDate(birthday);
        // 唯一确定一次检查
        if (CollectionUtils.isNotEmpty(canCheckMonthAge) && 1== canCheckMonthAge.size()) {
            return canCheckMonthAge.get(0);
        }
        // 获取当前可检查的已检查记录(按创建时间倒序)
        List<PreschoolCheckRecord> recordOnMonthAgeCheck = records.stream().filter(x -> canCheckMonthAge.contains(x.getMonthAge()))
                .sorted(Comparator.comparing(PreschoolCheckRecord::getCreateTime).reversed()).collect(Collectors.toList());
        // 当前都未检查，选中最早的检查
        if (CollectionUtils.isEmpty(recordOnMonthAgeCheck)) {
            return canCheckMonthAge.get(0);
        }
        Date now = new Date();
        // 其中一个已检查 可修改时选择当前，不可修改时选择另一个时间段
        if (1 == recordOnMonthAgeCheck.size()) {
            PreschoolCheckRecord hasCheck = recordOnMonthAgeCheck.get(0);
            return DateUtil.betweenDay(hasCheck.getCreateTime(), now) > 3 ?
                    canCheckMonthAge.get(0).equals(hasCheck.getMonthAge()) ? canCheckMonthAge.get(1) : canCheckMonthAge.get(0)
                    : hasCheck.getMonthAge();
        }
        // 两个都已检查
        // 都已无法修改，选择最迟的检查
        if (DateUtil.betweenDay(recordOnMonthAgeCheck.get(0).getCreateTime(), now) > 3) {
            return canCheckMonthAge.get(1);
        }
        // 一个无法修改，取可修改的
        if (DateUtil.betweenDay(recordOnMonthAgeCheck.get(1).getCreateTime(), now) > 3) {
            return recordOnMonthAgeCheck.get(0).getMonthAge();
        }
        // 两个都可修改，取最新修改的
        Optional<PreschoolCheckRecord> max = recordOnMonthAgeCheck.stream().max(Comparator.comparing(PreschoolCheckRecord::getUpdateTime));
        return max.map(PreschoolCheckRecord::getMonthAge).orElse(null);
    }

    /**
     * 初始化所有月龄为不可点击
     * @return
     */
    private Map<Integer, MonthAgeStatusDTO> initMonthAgeStatusMap() {
        Map<Integer, MonthAgeStatusDTO> monthAgeStatus = new LinkedHashMap<>();
        for(MonthAgeEnum monAge : MonthAgeEnum.values()) {
            monthAgeStatus.put(monAge.getId(), new MonthAgeStatusDTO(monAge, MonthAgeStatusEnum.AGE_STAGE_STATUS_DISABLE.getStatus()));
        }
        return monthAgeStatus;
    }

    /**
     * 组装各月龄段状态信息
     * @param map
     * @return
     */
    public List<MonthAgeStatusDTO> createMonthAgeStatusDTOByMap(Map<Integer, MonthAgeStatusDTO> map) {
        return map.keySet().stream().map(map::get).collect(Collectors.toList());
    }

    /**
     * 通过学生Id获取报告列表
     *
     * @param studentId 学生Id
     * @return List<EyeHealthyReportResponseDTO>
     */
    public List<EyeHealthyReportResponseDTO> getByStudentId(Integer studentId) {
        return baseMapper.getByStudentId(studentId);
    }

    /**
     * 通过学生Ids获取报告列表
     *
     * @param studentIds 学生Ids
     * @return List<EyeHealthyReportResponseDTO>
     */
    public List<PreschoolCheckRecord> getByStudentIds(List<Integer> studentIds) {
        return baseMapper.getByStudentIds(studentIds);
    }

    /**
     * 检验操作合法性
     * @param orgId
     * @param preschoolCheckRecordId
     */
    public PreschoolCheckRecord checkOrgOperation(Integer orgId, Integer preschoolCheckRecordId) {
        PreschoolCheckRecord record = getById(preschoolCheckRecordId, orgId);
        if (Objects.isNull(record)) {
            throw new BusinessException("非法请求", ResultCode.USER_ACCESS_UNAUTHORIZED.getCode());
        }
        return record;
    }

    /**
     * 获取学生所做检查数（月龄去重）
     * @param studentIds
     * @return
     */
    public Map<Integer, Integer> getStudentCheckCount(Integer hospitalId, List<Integer> studentIds) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return MapUtils.EMPTY_SORTED_MAP;
        }
        return baseMapper.getStudentCheckCount(hospitalId, studentIds).stream().collect(
                Collectors.toMap(StudentPreschoolCheckDTO::getStudentId, StudentPreschoolCheckDTO::getCount));
    }

}
