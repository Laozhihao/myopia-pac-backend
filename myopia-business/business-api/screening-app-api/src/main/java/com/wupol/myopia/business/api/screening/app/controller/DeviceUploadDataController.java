package com.wupol.myopia.business.api.screening.app.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.screening.app.domain.dto.DeviceUploadDto;
import com.wupol.myopia.business.api.screening.app.service.DeviceUploadDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

/**
 * 筛查端App接口
 *
 * @Author Chikong
 * @Date 2021-01-21
 */
@CrossOrigin
@ResponseResultBody
@Controller
@RequestMapping("/app/screening")
@Slf4j
public class DeviceUploadDataController {

    @Autowired
    private DeviceUploadDataService deviceScreeningDataService;

    /**
     * 上传数据
     * @param deviceUploadDto
     * @return
     */
    @PostMapping(value = "vs/uploadData", params = "v=1")
    public void uploadDeviceData(@Valid @RequestBody DeviceUploadDto deviceUploadDto){
        deviceScreeningDataService.uploadDeviceData(deviceUploadDto);
    }

}
