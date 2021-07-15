package com.wupol.myopia.business.api.device.controller;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.business.api.device.domain.dto.DeviceUploadDTO;
import com.wupol.myopia.business.api.device.domain.result.DeviceUploadResult;
import com.wupol.myopia.business.api.device.service.DeviceUploadDataService;
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

    /**
     * 上传数据
     * @param deviceUploadDto
     * @return
     */
    @PostMapping(value = "vs/uploadData", params = "v=1")
    public DeviceUploadResult uploadDeviceData(@Valid @RequestBody DeviceUploadDTO deviceUploadDto){
        try {
            deviceUploadDataService.uploadDeviceData(deviceUploadDto);
        } catch (Exception e) {
            log.error("设备上传数据失败,错误msg={},数据 = {}",e.getMessage(),JSON.toJSONString(deviceUploadDto));
            //todo 暂时不把message 返回,1是现在的设备端也处理不了,2是现在处理有点麻烦,后面考虑统一处理
            return DeviceUploadResult.FAILURE;
        }
        return DeviceUploadResult.SUCCESS;
    }

}
