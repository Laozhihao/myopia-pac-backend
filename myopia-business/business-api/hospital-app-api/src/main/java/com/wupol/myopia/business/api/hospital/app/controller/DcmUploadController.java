package com.wupol.myopia.business.api.hospital.app.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.hospital.app.domain.dto.FundusImageDTO;
import com.wupol.myopia.business.api.hospital.app.service.FundusImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 眼底检查
 *
 * @author Simple4H
 */
@Validated
@CrossOrigin
@RequestMapping("/hospital/app/device")
@RestController
@Slf4j
public class DcmUploadController {

    @Resource
    private FundusImageService fundusImageService;

    /**
     * 获取眼底影像
     *
     * @param patientId 患者Id
     *
     * @return ReturnInformation
     */
    @GetMapping(value = "/getPatientFundusFile")
    public ApiResult<List<FundusImageDTO>> getPatientFundusFile(@NotNull(message = "患者Id不能为空") Integer patientId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return ApiResult.success(fundusImageService.getPatientFundusFile(patientId, user.getOrgId()));
    }

    /**
     * 删除患者当天最后一批影像
     *
     * @param patientId 患者Id
     *
     * @return ReturnInformation
     */
    @DeleteMapping(value = "/deleted/{patientId}")
    public ApiResult<Boolean> deletedPatientImage(@PathVariable("patientId") Integer patientId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return ApiResult.success(fundusImageService.deletedPatientTodayLastBatchImage(patientId, user.getOrgId()));
    }
}
