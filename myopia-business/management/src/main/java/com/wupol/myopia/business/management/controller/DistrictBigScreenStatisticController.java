package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.business.management.domain.model.DistrictBigScreenStatistic;
import com.wupol.myopia.business.management.service.DistrictBigScreenStatisticService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wupol.myopia.base.handler.ResponseResultBody;

/**
 * @Author HaoHao
 * @Date 2021-03-07
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screening-statistic/big-screen")
public class DistrictBigScreenStatisticController extends BaseController<DistrictBigScreenStatisticService, DistrictBigScreenStatistic> {

}
