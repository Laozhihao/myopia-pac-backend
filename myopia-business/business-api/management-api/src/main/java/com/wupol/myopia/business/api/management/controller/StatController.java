package com.wupol.myopia.business.api.management.controller;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.service.StatReportService;
import com.wupol.myopia.business.api.management.service.StatService;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningClassStat;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningDataContrast;
import com.wupol.myopia.business.core.stat.domain.dto.WarningInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/stat")
@Log4j2
public class StatController {
    @Autowired
    private StatService statService;

    @Autowired
    private StatReportService statReportService;

    /**
     * 获取预警信息
     * @return
     */
    @GetMapping("warningList")
    public WarningInfo getWarningList() throws IOException {
        return statService.getWarningList();
    }

    /**
     * 获取筛查对比数据
     * @param notificationId1 1号通知ID
     * @param notificationId2 2号通知ID
     * @param districtId 区域ID
     * @param schoolAge 学龄代码
     * @return
     */
    @GetMapping("/dataContrast")
    public Map<String, ScreeningDataContrast> getScreeningDataContrast(@RequestParam("nid1") Integer notificationId1,
                                                                       @RequestParam(value = "nid2", required = false) Integer notificationId2,
                                                                       Integer districtId, Integer schoolAge) throws IOException {
            return statService.getScreeningDataContrast(notificationId1, notificationId2, districtId, schoolAge);
    }

    /**
     * 导出筛查对比数据
     * @param notificationId1 1号通知ID
     * @param notificationId2 2号通知ID
     * @param districtId 区域ID
     * @param schoolAge 学龄代码
     * @return
     */
    @GetMapping("/exportContrast")
    public void exportScreeningDataContrast(@RequestParam("nid1") Integer notificationId1,
            @RequestParam(value = "nid2", required = false) Integer notificationId2,
            Integer districtId, Integer schoolAge) throws IOException, UtilException {
            statService.exportStatContrast(notificationId1, notificationId2, districtId, schoolAge);
    }

    /**
     * 获取区域筛查报告
     * @param notificationId 通知ID
     * @param districtId 区域ID
     * @return
     */
    @GetMapping("/getDistrictReport")
    public Map<String, Object> getDistrictReport(@RequestParam("notificationId") Integer notificationId,
                                 @RequestParam("districtId") Integer districtId) throws IOException {
            return statReportService.getDistrictStatData(notificationId, districtId);
    }

    /**
     * 获取学校筛查报告
     * @param notificationId 通知ID
     * @param schoolId 学校ID
     * @return
     */
    @GetMapping("/getSchoolReport")
    public Map<String, Object> getSchoolReport(
            @RequestParam(name = "notificationId", required = false) Integer notificationId,
            @RequestParam(name = "planId", required = false) Integer planId,
            @RequestParam("schoolId") Integer schoolId) throws IOException {
            return statReportService.getSchoolStatData(notificationId, planId, schoolId);
    }

    /**
     * 分类统计数据
     *
     * @param notificationId 通知ID
     * @return
     */
    @GetMapping("/dataClass")
    public ScreeningClassStat getScreeningClassStat(@RequestParam("nid") Integer notificationId) throws IOException {
        return   statService.getScreeningClassStat(notificationId);
    }

    /**
     * 获取用户对应权限的可对比区域ID
     *
     * @param notificationId1
     * @param notificationId2
     * @return
     */
    @GetMapping("/dataContrastDistrictTree")
    public List<District> getDataContrastDistrictTree(@RequestParam("nid1") Integer notificationId1,
                                                      @RequestParam(value = "nid2", required = false) Integer notificationId2) throws IOException {
        return statService.getDataContrastDistrictTree(notificationId1, notificationId2);
    }
}
