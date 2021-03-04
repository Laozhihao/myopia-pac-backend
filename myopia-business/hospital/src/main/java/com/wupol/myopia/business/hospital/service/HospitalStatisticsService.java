package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.hospital.domain.mapper.MedicalReportMapper;
import com.wupol.myopia.business.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.hospital.domain.vo.MedicalReportVo;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.Student;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
        // 医院的学生信息列表
        List<HospitalStudent> hospitalStudentList = hospitalStudentService.findByList(new HospitalStudent().setHospitalId(hospitalId));
        // 报告列表
        List<MedicalReport> reportList = medicalReportService.findByList(new MedicalReport().setHospitalId(hospitalId));

        // 累计就诊的人数
        map.put("totalMedicalPersonCount", reportList.size());
        // 获取今天的统计信息
        map.putAll(getTodayInfo(hospitalStudentList, reportList));
        // 眼镜信息
        map.putAll(getGlassesInfo(reportList));
        // 月新增信息
        map.putAll(getMonthInfo(hospitalStudentList, reportList));

        return map;
    }

    /** 获取今天的统计信息 */
    private Map<String, Object> getTodayInfo(List<HospitalStudent> hospitalStudentList,
                                             List<MedicalReport> reportList) {
        Map<String, Object> map = new HashMap<>();
        List<HospitalStudent> todayHospitalStudentList = hospitalStudentList.stream()
                .filter(item-> DateUtils.isSameDay(item.getCreateTime(), new Date())).collect(Collectors.toList());
        List<MedicalReport> todayReportList = reportList.stream()
                .filter(item-> DateUtils.isSameDay(item.getCreateTime(), new Date())).collect(Collectors.toList());
        // Map<学生id，学生信息>，总的医院的学生信息
        Map<Integer, HospitalStudent> hospitalStudentMap = hospitalStudentList.stream().collect(Collectors.toMap(HospitalStudent::getStudentId, Function.identity()));
        // 就诊人数
        map.put("medicalPersonCount", todayReportList.size());
        // 建档人数
        map.put("consultationPersonCount", todayHospitalStudentList.size());
        // 复诊人数
        map.put("subsequentVisitPersonCount", getSubsequentVisitReport(hospitalStudentMap, todayReportList).size());
        return map;
    }

    /**
     * 获取复诊的报告, 报告日期与建档日期不是同一天，则为复诊
     */
    private List<MedicalReport> getSubsequentVisitReport(Map<Integer, HospitalStudent> hospitalStudentMap,
                                                 List<MedicalReport> reportList) {
        Map<Integer, List<MedicalReport>> studentReportMap = reportList.stream().collect(Collectors.groupingBy(MedicalReport::getStudentId));
        List<MedicalReport> subsequentVisitReportList = new ArrayList<>();
        for (Integer key : studentReportMap.keySet()) {
            List<MedicalReport> itemReportList = studentReportMap.get(key);
            // 报告日期与建档日期不是同一天，则为复诊
            MedicalReport lastReport = itemReportList.get(itemReportList.size()-1);
            if (DateUtils.isSameDay(hospitalStudentMap.get(key).getCreateTime(), lastReport.getCreateTime())) {
                subsequentVisitReportList.add(lastReport);
            }
        }
        return subsequentVisitReportList;
    }

    /** 获取眼镜信息 */
    private Map<String, Object> getGlassesInfo(List<MedicalReport> reportList) {
        Map<String, Object> map = new HashMap<>();
        // 隐形眼镜人数
        map.put("contactLensCount", reportList.stream().filter(item-> item.getGlassesSituation().equals(MedicalReport.GLASSES_SITUATION_CONTACT_LENS)).count());
        // ok镜人数
        map.put("okGlassesCount", reportList.stream().filter(item-> item.getGlassesSituation().equals(MedicalReport.GLASSES_SITUATION_OK_GLASSES)).count());
        // 配框架镜人数
        map.put("commonGlassesCount", reportList.stream().filter(item-> item.getGlassesSituation().equals(MedicalReport.GLASSES_SITUATION_COMMON_GLASSES)).count());
        return map;
    }

    /** 获取月新增信息 */
    private Map<String, Object> getMonthInfo(List<HospitalStudent> hospitalStudentList,
                                             List<MedicalReport> reportList) {
        Map<String, Object> map = new HashMap<>();
        // Map<学生id，学生信息>
        Map<Integer, HospitalStudent> hospitalStudentMap = hospitalStudentList.stream().collect(Collectors.toMap(HospitalStudent::getStudentId, Function.identity()));
        // 就诊人数
        map.put("medicalPersonStatistics", reportList.stream().collect(Collectors.groupingBy(item-> groupingByMonthFormat(item.getCreateTime()), LinkedHashMap::new, Collectors.counting())));
        // 建档人数
        map.put("consultationPersonStatistics", hospitalStudentList.stream().collect(Collectors.groupingBy(item-> groupingByMonthFormat(item.getCreateTime()), LinkedHashMap::new, Collectors.counting())));
        // 复诊人数
        map.put("subsequentVisitPersonStatistics", getSubsequentVisitReport(hospitalStudentMap, reportList).stream().collect(Collectors.groupingBy(item-> groupingByMonthFormat(item.getCreateTime()), LinkedHashMap::new, Collectors.counting())));
        return map;

    }

    /** 获取按月份分组的格式 */
    private String groupingByMonthFormat(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return String.format("%s年%s月", calendar.get(Calendar.YEAR), String.format("%02d", calendar.get(Calendar.MONTH)));
    }
}