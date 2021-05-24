package com.wupol.myopia.business.api.hospital.app.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.hospital.app.service.HospitalStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * 医院的数据统计的App接口
 * @author Chikong
 * @date 2021-02-10
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/hospital/app/statistics")
public class HospitalStatisticsController {

    @Autowired
    private HospitalStatisticsService hospitalStatisticsService;

    @GetMapping()
    public Map<String, Object> getStatistics() throws IOException {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalStatisticsService.getStatistics(user.getOrgId());
    }

}
