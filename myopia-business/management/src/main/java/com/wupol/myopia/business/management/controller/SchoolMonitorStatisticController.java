package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.SchoolMonitorStatistic;
import com.wupol.myopia.business.management.service.SchoolMonitorStatisticService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author HaoHao
 * @Date 2021-02-24
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/business/schoolMonitorStatistic")
public class SchoolMonitorStatisticController extends BaseController<SchoolMonitorStatisticService, SchoolMonitorStatistic> {

}
