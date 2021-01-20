package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

/**
 * 筛查机构-筛查记录
 *
 * @author Simple4H
 */
@Log4j2
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningOrganizationRecord")
public class ScreeningOrganizationRecordController {

    @GetMapping("{orgId}")
    public Object getRecordLists(@PathVariable("orgId")Integer orgId) {
        return null;
    }

    @GetMapping("{id}")
    public Object getRecordDetails(@PathVariable("id")Integer id) {
        return null;
    }
}