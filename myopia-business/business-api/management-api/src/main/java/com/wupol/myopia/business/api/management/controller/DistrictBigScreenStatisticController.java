package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.stat.domian.model.DistrictBigScreenStatistic;
import com.wupol.myopia.business.core.stat.service.DistrictBigScreenStatisticService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
