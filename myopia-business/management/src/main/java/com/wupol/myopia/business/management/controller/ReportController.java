package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * @Author HaoHao
 * @Date 2021/3/16
 **/
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/report")
@Log4j2
public class ReportController {

    /**
     * 导出区域筛查报告
     *
     * @param notificationId 筛查通知ID
     * @param districtId 行政区域ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/district/export")
    public ApiResult exportDistrictReport(@NotNull(message = "筛查通知ID不能为空") Integer notificationId, @NotNull(message = "行政区域ID不能为空") Integer districtId) {

        return ApiResult.success();
    }

    /**
     * 导出学校筛查报告
     *
     * @param notificationId 筛查通知ID
     * @param schoolId 行政区域ID
     * @return com.wupol.myopia.base.domain.ApiResult
     **/
    @GetMapping("/school/export")
    public ApiResult exportSchoolReport(@NotNull(message = "筛查通知ID不能为空") Integer notificationId, @NotNull(message = "学校ID不能为空") Integer schoolId) {
        return ApiResult.success();
    }


}
