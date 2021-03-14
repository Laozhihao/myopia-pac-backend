package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.hospital.domain.query.HospitalStudentQuery;
import com.wupol.myopia.business.hospital.domain.query.MedicalRecordQuery;
import com.wupol.myopia.business.hospital.domain.query.MedicalReportQuery;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 医院-数据统计
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class HospitalStatisticsService {

    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private MedicalReportService medicalReportService;
    @Autowired
    private HospitalStudentService hospitalStudentService;

    /**
     * 获取数据统计
     * @param hospitalId 医院id
     * @return
     */
    public Map<String, Object> getStatistics(Integer hospitalId) throws IOException {
        Map<String, Object> map = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DATE, -1);
        Date endDate = calendar.getTime();
        calendar.add(Calendar.MONTH, -11);
        calendar.set(Calendar.DATE, 1);
        Date startDate = calendar.getTime();

        // 医院的学生信息列表
        List<HospitalStudent> hospitalStudentList = getHospitalStudentList(hospitalId, startDate, endDate);
        // 检查列表
        List<MedicalRecord> medicalList = getMedicalRecordList(hospitalId, startDate, endDate);
        // 报告列表
        List<MedicalReport> reportList = getMedicalReportList(hospitalId, startDate, endDate);

        // 累计就诊的人数
        map.put("totalMedicalPersonCount", medicalRecordService.count(new MedicalRecord().setHospitalId(hospitalId)));
        // 获取今天的统计信息
        map.putAll(getTodayInfo(hospitalStudentList, medicalList, reportList));
        // 月新增信息
        map.putAll(getMonthInfo(hospitalStudentList, medicalList));

        return map;
    }

    /** 获取需要的医院的学生列表 */
    private List<HospitalStudent> getHospitalStudentList(Integer hospitalId, Date startDate ,Date endDate) {
        HospitalStudentQuery query = new HospitalStudentQuery();
        query.setHospitalId(hospitalId);
        query.setStartDate(startDate).setEndDate(endDate);
        return hospitalStudentService.getBy(query);
    }

    /** 获取需要的检查列表 */
    private List<MedicalRecord> getMedicalRecordList(Integer hospitalId, Date startDate ,Date endDate) {
        MedicalRecordQuery query = new MedicalRecordQuery();
        query.setHospitalId(hospitalId);
        query.setStartDate(startDate).setEndDate(endDate);
        return medicalRecordService.getBy(query);
    }

    /** 获取需要的报告列表 */
    private List<MedicalReport> getMedicalReportList(Integer hospitalId, Date startDate ,Date endDate) {
        MedicalReportQuery query = new MedicalReportQuery();
        query.setHospitalId(hospitalId);
        query.setStartDate(startDate).setEndDate(endDate);
        return medicalReportService.getBy(query);
    }

    /** 获取今天的统计信息 */
    private Map<String, Object> getTodayInfo(List<HospitalStudent> hospitalStudentList,
                                             List<MedicalRecord> medicalList,
                                             List<MedicalReport> reportList) {
        Map<String, Object> map = new HashMap<>();
        List<HospitalStudent> todayHospitalStudentList = hospitalStudentList.stream()
                .filter(item-> DateUtils.isSameDay(item.getCreateTime(), new Date())).collect(Collectors.toList());
        List<MedicalRecord> todayMedicalList = medicalList.stream()
                .filter(item-> DateUtils.isSameDay(item.getCreateTime(), new Date())).collect(Collectors.toList());
        List<MedicalReport> todayReportList = reportList.stream()
                .filter(item-> DateUtils.isSameDay(item.getCreateTime(), new Date())).collect(Collectors.toList());
        // Map<学生id，学生信息>，总的医院的学生信息
        Map<Integer, HospitalStudent> hospitalStudentMap = hospitalStudentList.stream().collect(Collectors.toMap(HospitalStudent::getStudentId, Function.identity()));
        // 就诊人数
        map.put("medicalPersonCount", todayMedicalList.size());
        // 建档人数
        map.put("consultationPersonCount", todayHospitalStudentList.size());
        // 复诊人数
        map.put("subsequentVisitPersonCount", getSubsequentVisitMedical(hospitalStudentMap, todayMedicalList).size());
        // 眼镜信息
        map.putAll(getGlassesInfo(todayReportList));
        return map;
    }

    /**
     * 获取复诊的报告, 报告日期与建档日期不是同一天，则为复诊
     */
    private List<MedicalRecord> getSubsequentVisitMedical(Map<Integer, HospitalStudent> hospitalStudentMap,
                                                          List<MedicalRecord> medicalList) {
        Map<Integer, List<MedicalRecord>> studentMedicalMap = medicalList.stream().collect(Collectors.groupingBy(MedicalRecord::getStudentId));
        List<MedicalRecord> subsequentVisitMedicalList = new ArrayList<>();
        for (Integer key : studentMedicalMap.keySet()) {
            List<MedicalRecord> itemMedicalList = studentMedicalMap.get(key);
            // 报告日期与建档日期不是同一天，则为复诊
            MedicalRecord lastMedical = itemMedicalList.get(itemMedicalList.size()-1);
            if (!DateUtils.isSameDay(hospitalStudentMap.get(key).getCreateTime(), lastMedical.getCreateTime())) {
                subsequentVisitMedicalList.add(lastMedical);
            }
        }
        return subsequentVisitMedicalList;
    }

    /** 获取眼镜信息 */
    private Map<String, Object> getGlassesInfo(List<MedicalReport> reportList) {
        Map<String, Object> map = new HashMap<>();
        // 隐形眼镜人数
        map.put("contactLensCount", reportList.stream().filter(item-> MedicalReport.GLASSES_SITUATION_CONTACT_LENS.equals(item.getGlassesSituation())).count());
        // ok镜人数
        map.put("okGlassesCount", reportList.stream().filter(item-> MedicalReport.GLASSES_SITUATION_OK_GLASSES.equals(item.getGlassesSituation())).count());
        // 配框架镜人数
        map.put("commonGlassesCount", reportList.stream().filter(item-> MedicalReport.GLASSES_SITUATION_COMMON_GLASSES.equals(item.getGlassesSituation())).count());
        return map;
    }

    /** 获取月新增信息 */
    private Map<String, Object> getMonthInfo(List<HospitalStudent> hospitalStudentList,
                                             List<MedicalRecord> medicalList) {
        Map<String, Object> map = new HashMap<>();
        // Map<学生id，学生信息>
        Map<Integer, HospitalStudent> hospitalStudentMap = hospitalStudentList.stream().collect(Collectors.toMap(HospitalStudent::getStudentId, Function.identity()));
        // 就诊人数
        map.put("medicalPersonStatistics", formatStatisticsMap(medicalList.stream().collect(Collectors.groupingBy(item-> groupingByMonthFormat(item.getCreateTime()), LinkedHashMap::new, Collectors.counting()))));
        // 建档人数
        map.put("consultationPersonStatistics", formatStatisticsMap(hospitalStudentList.stream().collect(Collectors.groupingBy(item-> groupingByMonthFormat(item.getCreateTime()), LinkedHashMap::new, Collectors.counting()))));
        // 复诊人数
        map.put("subsequentVisitPersonStatistics", formatStatisticsMap(getSubsequentVisitMedical(hospitalStudentMap, medicalList).stream().collect(Collectors.groupingBy(item-> groupingByMonthFormat(item.getCreateTime()), LinkedHashMap::new, Collectors.counting()))));
        return map;

    }

    /**
     * 将统计的数据转为12个月的
     * @param map 已统计的数据
     */
    private LinkedHashMap<String, Long> formatStatisticsMap(LinkedHashMap<String, Long> map) {
        LinkedHashMap<String, Long> resultMap = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1); // 从昨天开始统计
        calendar.add(Calendar.MONTH, -11); // 从去年同比再下一个月开始
        resultMap.put(groupingByMonthFormat(calendar), 0L);
        for (int i = 0; i < 11; i++) {
            calendar.add(Calendar.MONTH, 1);
            resultMap.put(groupingByMonthFormat(calendar), 0L);
        }
        resultMap.putAll(map); // 覆盖数据
        return resultMap;
    }

    /** 获取按月份分组的格式 */
    private String groupingByMonthFormat(Calendar calendar) {
        return groupingByMonthFormat(calendar, null);
    }

    /** 获取按月份分组的格式 */
    private String groupingByMonthFormat(Date date) {
        return groupingByMonthFormat(null, date);
    }

    /** 获取按月份分组的格式 */
    private String groupingByMonthFormat(Calendar calendar, Date date) {
        if (Objects.isNull(calendar))  {
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        }
//        return String.format("%s月", String.format("%02d", calendar.get(Calendar.MONTH)+1));
        return String.format("%s月", calendar.get(Calendar.MONTH)+1);
    }
}