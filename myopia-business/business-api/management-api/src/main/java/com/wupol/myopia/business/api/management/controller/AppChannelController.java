package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.service.Html2PdfService;
import com.wupol.myopia.business.core.system.domain.model.AppChannel;
import com.wupol.myopia.business.core.system.service.AppChannelService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @Author HaoHao
 * @Date 2021-11-26
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/app/channel")
@Log4j2
public class AppChannelController extends BaseController<AppChannelService, AppChannel> {

    @Autowired
    private Html2PdfService html2PdfService;

    @GetMapping("ab")
    public PdfResponseDTO ab() {
        return html2PdfService.asyncGeneratorPDF("https://t-myopia-pac-report.tulab.cn?planId=9&schoolId=14", "abc.pdf", UUID.randomUUID().toString());
//        return html2PdfService.syncGeneratorPDF("https://t-myopia-pac-report.tulab.cn?planId=9&schoolId=14", "abc.pdf", UUID.randomUUID().toString());
    }

}
