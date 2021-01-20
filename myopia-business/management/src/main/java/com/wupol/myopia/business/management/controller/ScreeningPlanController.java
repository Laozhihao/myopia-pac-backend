package com.wupol.myopia.business.management.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import com.wupol.myopia.business.management.service.ScreeningPlanService;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningPlan")
public class ScreeningPlanController extends BaseController<ScreeningPlanService, ScreeningPlan> {

}
