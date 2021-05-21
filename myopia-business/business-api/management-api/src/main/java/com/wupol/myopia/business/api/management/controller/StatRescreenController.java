package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.domain.vo.RescreenReportVO;
import com.wupol.myopia.business.api.management.service.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author wulizhou
 * @Date 2021/5/20 12:26
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/stat/rescreen")
public class StatRescreenController {

    @Autowired
    private StatService statService;

    @GetMapping("/export")
    public List<RescreenReportVO> export(Integer planId, Integer schoolId, String qualityControllerName, String qualityControllerCommander) {
        return statService.getRescreenStatInfo(planId, schoolId, qualityControllerName, qualityControllerCommander);
    }

}
