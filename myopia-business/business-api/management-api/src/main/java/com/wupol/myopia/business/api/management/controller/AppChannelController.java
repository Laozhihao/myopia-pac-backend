package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.service.Html2PdfService;
import com.wupol.myopia.business.core.system.domain.model.AppChannel;
import com.wupol.myopia.business.core.system.service.AppChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Autowired
    private Html2PdfService html2PdfService;

    @GetMapping("ab")
    public void ab() {
        html2PdfService.asyncGeneratorPDF("https://t-myopia-pac-report.tulab.cn?planId=9&schoolId=14", "abc.pdf", "123");
    }

}
