package com.wupol.myopia.business.api.management.controller;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.domain.dto.ContrastTypeYearItemsDTO;
import com.wupol.myopia.business.api.management.domain.dto.DataContrastFilterParamsDTO;
import com.wupol.myopia.business.api.management.domain.dto.DataContrastFilterResultDTO;
import com.wupol.myopia.business.api.management.domain.dto.StatWholeResultDTO;
import com.wupol.myopia.business.api.management.service.StatReportService;
import com.wupol.myopia.business.api.management.service.StatService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningClassStat;
import com.wupol.myopia.business.core.stat.domain.dto.WarningInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
     *
     * @return
     */
    @GetMapping("warningList")
    public WarningInfo getWarningList() {
        return statService.getWarningList(CurrentUserUtil.getCurrentUser());
    }

    /**
     * 导出筛查对比数据
     *
     * @param dataContrastExportParams
     */
    @PostMapping("/exportContrast")
    public void exportScreeningDataContrast(@RequestBody DataContrastFilterParamsDTO dataContrastExportParams)
            throws UtilException, IOException {
        statService.exportStatContrast(dataContrastExportParams, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 获取区域筛查报告
     *
     * @param notificationId 通知ID
     * @param districtId     区域ID
     * @return
     */
    @GetMapping("/getDistrictReport")
    public Map<String, Object> getDistrictReport(@RequestParam("notificationId") Integer notificationId,
                                                 @RequestParam("districtId") Integer districtId) {
        return statReportService.getDistrictStatData(notificationId, districtId);
    }

    /**
     * 获取学校筛查报告
     *
     * @param notificationId 通知ID
     * @param schoolId       学校ID
     * @return
     */
    @GetMapping("/getSchoolReport")
    public Map<String, Object> getSchoolReport(
            @RequestParam(name = "notificationId", required = false) Integer notificationId,
            @RequestParam(name = "planId", required = false) Integer planId,
            @RequestParam("schoolId") Integer schoolId) {
        return statReportService.getSchoolStatData(notificationId, planId, schoolId);
    }

    /**
     * 获取计划下学校统计数据
     * @param planId
     * @return
     */
    @GetMapping("/getAllSchoolReport")
    public StatWholeResultDTO getSchoolReportByPlanId(@RequestParam Integer planId) {
        return statReportService.getPlanStatData(planId);
    }

    /**
     * 分类统计数据
     *
     * @param notificationId 通知ID
     * @return
     */
    @GetMapping("/dataClass")
    public ScreeningClassStat getScreeningClassStat(@RequestParam("nid") Integer notificationId) throws IOException {
        return statService.getScreeningClassStat(notificationId, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 获取用户相关的历年通知、任务、计划用户统计对比筛选项
     *
     * @return
     */
    @GetMapping("/dataContrastYear")
    public Map<Integer, List<ContrastTypeYearItemsDTO>> getDataContrastYear() {
        return statService.composeContrastTypeFilter(CurrentUserUtil.getCurrentUser());
    }

    /**
     * 返回数据对比的筛查项以及结果数据
     *
     * @param contrastType 对比项类型
     * @param contrastId   对比项ID
     * @return DataContrastFilterResultDTO
     */
    @GetMapping("/dataContrastFilter")
    public DataContrastFilterResultDTO getDataContrastFilter(
            @RequestParam("ctype") Integer contrastType,
            @RequestParam("cid") Integer contrastId,
            @RequestParam(value = "districtId", required = false) Integer districtId,
            @RequestParam(value = "schoolAge", required = false) Integer schoolAge,
            @RequestParam(value = "schoolId", required = false) Integer schoolId,
            @RequestParam(value = "schoolGradeCode", required = false) String schoolGradeCode,
            @RequestParam(value = "schoolClass", required = false) String schoolClass
    ) throws IOException {
        DataContrastFilterParamsDTO.Params params = new DataContrastFilterParamsDTO.Params();
        params.setContrastId(contrastId);
        params.setDistrictId(districtId);
        params.setSchoolAge(schoolAge);
        params.setSchoolId(schoolId);
        params.setSchoolGradeCode(schoolGradeCode);
        params.setSchoolClass(schoolClass);
        return statService.getDataContrastFilter(contrastType, params, CurrentUserUtil.getCurrentUser());
    }
}
