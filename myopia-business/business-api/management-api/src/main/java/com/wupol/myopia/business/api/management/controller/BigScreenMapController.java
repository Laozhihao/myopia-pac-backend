package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.BigScreenMap;
import com.wupol.myopia.business.management.service.BigScreenMapService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Author jacob
 * @Date 2021-03-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/business/bigScreenMap")
public class BigScreenMapController extends BaseController<BigScreenMapService, BigScreenMap> {

}
