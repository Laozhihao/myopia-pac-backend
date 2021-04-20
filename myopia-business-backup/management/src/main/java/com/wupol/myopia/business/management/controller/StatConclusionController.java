package com.wupol.myopia.business.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.business.management.domain.model.StatConclusion;
import com.wupol.myopia.business.management.service.StatConclusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;

import java.util.List;

/**
 * @Author Jacob
 * @Date 2021-02-22
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/statConclusion")
public class StatConclusionController extends BaseController<StatConclusionService, StatConclusion> {

}
