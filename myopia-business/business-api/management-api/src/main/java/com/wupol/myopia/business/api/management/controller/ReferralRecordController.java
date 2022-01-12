package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.hospital.domain.dto.ReferralDTO;
import com.wupol.myopia.business.core.hospital.service.ReferralRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 转诊记录
 * @Author wulizhou
 * @Date 2022/1/4 20:20
 */
@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/referral")
@Slf4j
public class ReferralRecordController {

    @Autowired
    private ReferralRecordService referralRecordService;

    /**
     * 获取转诊单详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ReferralDTO getDetails(@PathVariable("id") Integer id) {
        return referralRecordService.getDetailsById(id);
    }

}
