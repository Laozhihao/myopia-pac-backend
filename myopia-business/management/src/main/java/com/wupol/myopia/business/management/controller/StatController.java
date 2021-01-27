package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/stat")
public class StatController {
    @GetMapping("warningList")
    public ApiResult getWarningList() {
        return ApiResult.success();
    }
}
