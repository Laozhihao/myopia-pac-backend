package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.hospital.domain.dto.ReceiptDTO;
import com.wupol.myopia.business.core.hospital.service.ReceiptListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 回执单
 * @Author wulizhou
 * @Date 2022/1/4 20:20
 */
@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/receipt")
@Slf4j
public class ReceiptListController {

    @Autowired
    private ReceiptListService receiptListService;

    /**
     * 获取回执单详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ReceiptDTO getDetails(@PathVariable("id") Integer id) {
        return receiptListService.getDetailsById(id);
    }

}
