package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.domain.vo.RescreenReportVO;
import com.wupol.myopia.business.api.management.service.StatService;
import com.wupol.myopia.business.core.screening.flow.service.StatRescreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @Author wulizhou
 * @Date 2021/5/20 12:26
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/stat/rescreen")
public class StatRescreenController {

    @Autowired
    private StatService statService;

    @Autowired
    private StatRescreenService statRescreenService;

    @GetMapping("/export")
    public List<RescreenReportVO> export(@RequestParam Integer planId, @RequestParam Integer schoolId,
                                         @RequestParam String qualityControllerName, @RequestParam String qualityControllerCommander,
                                         @RequestParam Date screeningData) {
        return statService.getRescreenStatInfo(planId, schoolId, qualityControllerName, qualityControllerCommander, screeningData);
    }

    @GetMapping("/manualStat")
    public void manualStat() {
        statService.rescreenStat(new Date());
    }

    /**
     * 获取学校日期
     *
     * @param planId   计划Id
     * @param schoolId 学校Id
     * @return 日期
     */
    @GetMapping("/schoolDate")
    public List<Date> getSchoolDate(Integer planId, Integer schoolId) {
        return statRescreenService.getSchoolDate(planId, schoolId);
    }

}
