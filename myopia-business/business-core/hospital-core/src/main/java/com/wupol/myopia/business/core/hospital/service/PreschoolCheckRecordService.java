package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BusinessUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.base.constant.MonthAgeEnum;
import com.wupol.myopia.business.core.hospital.constant.MonthAgeStatusEnum;
import com.wupol.myopia.business.core.hospital.domain.dto.MonthAgeStatusDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.PreschoolCheckRecordMapper;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.domain.query.PreschoolCheckRecordQuery;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Service
public class PreschoolCheckRecordService extends BaseService<PreschoolCheckRecordMapper, PreschoolCheckRecord> {

    /**
     * 获取眼保健详情
     * @param id
     * @return
     */
    public PreschoolCheckRecordDTO getDetails(Integer id) {
        PreschoolCheckRecordDTO details = baseMapper.getDetails(id);
        // TODO wulizhou
        details.setCreateTimeAge(DateUtil.getAgeInfo(details.getBirthday(), details.getCreateTime()));
        details.setDoctorsName("");
        return details;
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
     * 获取学生13次检查的检查状态
     * @param records
     * @return
     */
    private List<MonthAgeStatusDTO> getStudentCheckStatus(Date birthday, List<PreschoolCheckRecordDTO> records) {
        Date now = new Date();
        Map<Integer, Integer> monthAgeStatusDTOS = initMonthAgeStatusMap();
        List<Integer> canCheckMonthAge = BusinessUtil.getCanCheckMonthAgeByDate(birthday);
        // 设置当前可检查年龄段为AGE_STAGE_STATUS_NOT_DATA状态
        canCheckMonthAge.forEach(monthAge -> {
            monthAgeStatusDTOS.put(monthAge, MonthAgeStatusEnum.AGE_STAGE_STATUS_NOT_DATA.getStatus());
        });
        records.forEach(record -> {
            // 检查大于3天，无法修改
            if (DateUtil.betweenDay(record.getCreateTime(), now) > 3) {
                monthAgeStatusDTOS.put(record.getMonthAge(), MonthAgeStatusEnum.AGE_STAGE_STATUS_CANNOT_UPDATE.getStatus());
            } else {
                // 已检查，可修改
                monthAgeStatusDTOS.put(record.getMonthAge(), MonthAgeStatusEnum.AGE_STAGE_STATUS_CAN_UPDATE.getStatus());
            }
        });
        // 设置最小可检查年龄段为AGE_STAGE_STATUS_CURRENT
        List<Integer> collect = records.stream().map(record -> record.getMonthAge()).collect(Collectors.toList());
        List<Integer> unCheckMonthAgeByCanCheck = canCheckMonthAge.stream().filter(x -> !collect.contains(x)).collect(Collectors.toList());
        // 当前需检查的都已检查，选中最后的检查项
        if (CollectionUtils.isEmpty(unCheckMonthAgeByCanCheck)) {
            monthAgeStatusDTOS.put(canCheckMonthAge.stream().mapToInt(x -> x).max().getAsInt(), MonthAgeStatusEnum.AGE_STAGE_STATUS_CURRENT.getStatus());
        } else {
            // 若存在应检未检的，选中该年龄段
            monthAgeStatusDTOS.put(canCheckMonthAge.stream().mapToInt(x -> x).min().getAsInt(), MonthAgeStatusEnum.AGE_STAGE_STATUS_CURRENT.getStatus());
        }
        return createMonthAgeStatusDTOByMap(monthAgeStatusDTOS);
    }

    /**
     * 初始化所有月龄为不可点击
     * @return
     */
    private Map<Integer, Integer> initMonthAgeStatusMap() {
        Map monthAgeStatus = new LinkedHashMap();
        for(MonthAgeEnum monAge : MonthAgeEnum.values()) {
            monthAgeStatus.put(monAge.getId(), MonthAgeStatusEnum.AGE_STAGE_STATUS_DISABLE.getStatus());
        }
        return monthAgeStatus;
    }

    private List<MonthAgeStatusDTO> createMonthAgeStatusDTOByMap(Map<Integer, Integer> map) {
        return map.keySet().stream().map(monthAge -> new MonthAgeStatusDTO(monthAge, map.get(monthAge))).collect(Collectors.toList());
    }

}
