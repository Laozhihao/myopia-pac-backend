package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.MonthAgeEnum;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BusinessUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.constant.MonthAgeStatusEnum;
import com.wupol.myopia.business.core.hospital.domain.dto.MonthAgeStatusDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.PreschoolCheckRecordMapper;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.domain.query.PreschoolCheckRecordQuery;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Service
public class PreschoolCheckRecordService extends BaseService<PreschoolCheckRecordMapper, PreschoolCheckRecord> {

    @Autowired
    private ReferralRecordService referralRecordService;

    @Autowired
    private HospitalStudentService hospitalStudentService;

    /**
     * 获取眼保健详情
     * @param id
     * @return
     */
    public PreschoolCheckRecordDTO getDetails(Integer id) {
        PreschoolCheckRecordDTO details = baseMapper.getDetails(id);
        details.setCreateTimeAge(DateUtil.getAgeInfo(details.getBirthday(), details.getCreateTime()));
        // 设置检查前后转诊信息
        if (Objects.nonNull(details.getFromReferralId())) {
            details.setFromReferral(referralRecordService.getById(details.getFromReferralId()));
        }
        if (Objects.nonNull(details.getToReferralId())) {
            details.setToReferral(referralRecordService.getById(details.getToReferralId()));
        }

        // TODO wulizhou
        details.setDoctorsName("");
        return details;
    }

    /**
     * 获取默认显示信息
     * @param hospitalId
     * @param studentId
     * @return
     */
    public PreschoolCheckRecordDTO getInit(Integer hospitalId, Integer studentId) {
        List<PreschoolCheckRecord> records = getStudentRecord(hospitalId, studentId);
        HospitalStudent student = hospitalStudentService.getByHospitalIdAndStudentId(hospitalId, studentId);
        Map<Integer, MonthAgeStatusDTO> studentCheckStatus = getStudentCheckStatus(student.getBirthday(), records);
        Integer currentShowCheckMonthAge = getCurrentShowCheckMonthAge(student.getBirthday(), records);
        // TODO wulizhou
        return null;
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
        IPage<PreschoolCheckRecordDTO> records = baseMapper.getListByCondition(pageRequest.toPage(), query);
        // TODO wulizhou
        records.getRecords().forEach(record -> {
            record.setCreateTimeAge(DateUtil.getAgeInfo(record.getBirthday(), record.getCreateTime()));
            record.setDoctorsName("");
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
    public PreschoolCheckRecord get(Integer hospitalId, Integer studentId, Integer monthAge) {
        PreschoolCheckRecord record = new PreschoolCheckRecord().setHospitalId(hospitalId).setStudentId(studentId)
                .setMonthAge(monthAge);
        return findOne(record);
    }

    /**
     * 获取学生13次检查的检查状态
     * @param records
     * @return
     */
    private Map<Integer, MonthAgeStatusDTO> getStudentCheckStatus(Date birthday, List<PreschoolCheckRecord> records) {
        Date now = new Date();
        Map<Integer, MonthAgeStatusDTO> monthAgeStatusDTOS = initMonthAgeStatusMap();
        List<Integer> canCheckMonthAge = BusinessUtil.getCanCheckMonthAgeByDate(birthday);
        // 设置当前可检查年龄段为AGE_STAGE_STATUS_NOT_DATA状态
        canCheckMonthAge.forEach(monthAge -> {
            monthAgeStatusDTOS.put(monthAge, new MonthAgeStatusDTO(monthAge, MonthAgeStatusEnum.AGE_STAGE_STATUS_NOT_DATA.getStatus()));
        });
        records.forEach(record -> {
            // 检查大于3天，无法修改
            if (DateUtil.betweenDay(record.getCreateTime(), now) > 3) {
                monthAgeStatusDTOS.put(record.getMonthAge(), new MonthAgeStatusDTO(record.getMonthAge(),
                        MonthAgeStatusEnum.AGE_STAGE_STATUS_CANNOT_UPDATE.getStatus(), record.getId()));
            } else {
                // 已检查，可修改
                monthAgeStatusDTOS.put(record.getMonthAge(), new MonthAgeStatusDTO(record.getMonthAge(),
                        MonthAgeStatusEnum.AGE_STAGE_STATUS_CAN_UPDATE.getStatus(), record.getId()));
            }
        });
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
        // 首选未检查信息
        List<Integer> collect = records.stream().map(record -> record.getMonthAge()).collect(Collectors.toList());
        List<Integer> unCheckMonthAgeByCanCheck = canCheckMonthAge.stream().filter(x -> !collect.contains(x)).collect(Collectors.toList());
        // 当前需检查的都已检查，选中最后的检查项
        if (CollectionUtils.isEmpty(unCheckMonthAgeByCanCheck)) {
            return canCheckMonthAge.stream().mapToInt(x -> x).max().getAsInt();
        } else {
            // 若存在应检未检的，选中该年龄段最小月龄
            return unCheckMonthAgeByCanCheck.stream().mapToInt(x -> x).min().getAsInt();
        }
    }

    /**
     * 初始化所有月龄为不可点击
     * @return
     */
    private Map<Integer, MonthAgeStatusDTO> initMonthAgeStatusMap() {
        Map<Integer, MonthAgeStatusDTO> monthAgeStatus = new LinkedHashMap();
        for(MonthAgeEnum monAge : MonthAgeEnum.values()) {
            monthAgeStatus.put(monAge.getId(), new MonthAgeStatusDTO(monAge.getId(), MonthAgeStatusEnum.AGE_STAGE_STATUS_DISABLE.getStatus()));
        }
        return monthAgeStatus;
    }

    private List<MonthAgeStatusDTO> createMonthAgeStatusDTOByMap(Map<Integer, MonthAgeStatusDTO> map) {
        return map.keySet().stream().map(monthAge -> map.get(monthAge)).collect(Collectors.toList());
    }

}
