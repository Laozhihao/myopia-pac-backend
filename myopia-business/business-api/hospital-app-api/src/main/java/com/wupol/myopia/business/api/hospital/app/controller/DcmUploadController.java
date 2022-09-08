package com.wupol.myopia.business.api.hospital.app.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.api.hospital.app.domain.dto.DeviceRequestDTO;
import com.wupol.myopia.business.api.hospital.app.service.DeviceUploadService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 眼底检查
 *
 * @author Simple4H
 */
@CrossOrigin
@RequestMapping("/hospital/app/device")
@RestController
@Slf4j
public class DcmUploadController {

    @Resource
    private ResourceFileService resourceFileService;

    @Resource
    private DeviceUploadService deviceUploadService;


    /**
     * 眼底检查数据上传
     *
     * @param requestDTO 请求DTO
     *
     * @return ReturnInformation
     */
    @PostMapping(value = "/uploadFundus", consumes = {"multipart/form-data"})
    public String addV2(DeviceRequestDTO requestDTO) {
        return deviceUploadService.fundusUpload(requestDTO);
    }

    @GetMapping("resource")
    public ApiResult<String> getResource(Integer id) {
        return ApiResult.success(resourceFileService.getResourcePath(id));
    }
}
