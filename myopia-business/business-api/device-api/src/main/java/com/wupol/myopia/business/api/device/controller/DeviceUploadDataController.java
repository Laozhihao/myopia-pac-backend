package com.wupol.myopia.business.api.device.controller;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.screening.domain.dto.DeviceDataRequestDTO;
import com.wupol.myopia.business.api.device.domain.dto.DeviceUploadDTO;
import com.wupol.myopia.business.api.device.domain.result.DeviceUploadResult;
import com.wupol.myopia.business.api.device.service.DeviceUploadDataService;
import com.wupol.myopia.business.api.device.service.IDeviceDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * 设备上传接口
 *
 * @Author Jacob
 * @Date 2021-07-05
 */
@CrossOrigin
@RequestMapping("/api")
@RestController
@Slf4j
public class DeviceUploadDataController {

    @Autowired
    private DeviceUploadDataService deviceUploadDataService;

    @Autowired
    private IDeviceDataService iDeviceDataService;

    /**
     * 上传数据
     *
     * @param deviceUploadDto
     * @return
     */
    @PostMapping(value = "vs/uploadData", params = "v=1")
    public DeviceUploadResult uploadDeviceData(@Valid @RequestBody DeviceUploadDTO deviceUploadDto) {
        try {
            deviceUploadDataService.uploadDeviceData(deviceUploadDto);
        } catch (Exception e) {
            log.error("设备上传数据失败,数据 = {}", JSON.toJSONString(deviceUploadDto), e);
            if (e instanceof BusinessException) {
                return DeviceUploadResult.FAILURE(e.getMessage());
            }
            return DeviceUploadResult.FAILURE;
        }
        return DeviceUploadResult.SUCCESS;
    }


    @PostMapping("/device/uploadData")
    public ApiResult uploadLightBoxData(@RequestBody @Valid DeviceDataRequestDTO requestDTO) {
        iDeviceDataService.uploadDate(requestDTO);
        return ApiResult.success();
    }

}
