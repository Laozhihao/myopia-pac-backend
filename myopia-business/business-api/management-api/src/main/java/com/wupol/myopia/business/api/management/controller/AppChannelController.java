package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.system.domain.model.AppChannel;
import com.wupol.myopia.business.core.system.service.AppChannelService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author HaoHao
 * @Date 2021-11-26
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/app/channel")
public class AppChannelController extends BaseController<AppChannelService, AppChannel> {

}
