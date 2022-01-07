package com.wupol.myopia.business.api.management.controller;

import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.handler.ResponseResultBody;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

/**
 * pdf回调地址
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/pdf")
@Log4j2
public class PdfCallbackController {

    @PostMapping("callback")
    public void callback(@RequestBody PdfResponseDTO responseDTO) {
        log.info(JSONObject.toJSONString(responseDTO));
    }

}
