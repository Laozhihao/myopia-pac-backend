package com.wupol.myopia.third.party.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.third.party.domain.VisionScreeningResultDTO;
import com.wupol.myopia.third.party.service.XinJiangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 新疆业务控制器
 *
 * @Author lzh
 * @Date 2023-04-13
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/xinjiang")
public class XinJiangController {

    @Autowired
    private XinJiangService xinJiangService;

    @PostMapping("/screening/result/push")
    public void receiveScreeningResultData(@RequestBody @Validated VisionScreeningResultDTO visionScreeningResultDTO) {
        xinJiangService.handleScreeningResultData(visionScreeningResultDTO);
    }
}
