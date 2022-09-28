package com.wupol.myopia.business.api.device.controller;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.screening.domain.dto.DeviceDataRequestDTO;
import com.wupol.myopia.business.api.device.config.DeviceDataFactory;
import com.wupol.myopia.business.api.device.domain.dto.*;
import com.wupol.myopia.business.api.device.domain.result.DeviceUploadResult;
import com.wupol.myopia.business.api.device.service.DeviceUploadDataService;
import com.wupol.myopia.business.api.device.service.DeviceUploadService;
import com.wupol.myopia.business.api.device.service.FkrDataService;
import com.wupol.myopia.business.api.device.service.IDeviceDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    private FkrDataService fkrDataService;

    @Resource
    private DeviceUploadService deviceUploadService;

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
        } catch (BusinessException e) {
            log.error("设备上传数据失败,数据 = {}", JSON.toJSONString(deviceUploadDto), e);
            return DeviceUploadResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("设备上传数据失败,数据 = {}", JSON.toJSONString(deviceUploadDto), e);
            return DeviceUploadResult.FAILURE;
        }
        return DeviceUploadResult.SUCCESS;
    }

    /**
     * 南京灯箱视力表
     *
     * @param requestDTO
     * @return com.wupol.myopia.base.domain.ApiResult<java.lang.String>
     **/
    @PostMapping("/device/uploadData")
    public ApiResult<String> uploadLightBoxData(@RequestBody @Valid DeviceDataRequestDTO requestDTO) {
        IDeviceDataService deviceDataService = DeviceDataFactory.getDeviceDataService(requestDTO.getBusinessType());
        deviceDataService.uploadDate(requestDTO);
        return ApiResult.success();
    }

    /**
     * 体脂秤数据上传
     *
     * @param requestDTO 入参
     * @return ScalesResponseDTO
     */
    @PostMapping("/device/bmi")
    public Object uploadBMI(@RequestBody @Valid ScalesRequestDTO requestDTO) {
        log.info("Data:{}", JSON.toJSONString(requestDTO));
        return deviceUploadDataService.bodyFatScaleUpload(requestDTO);
    }

    /**
     * 获取学生信息
     *
     * @param request 请求入参
     * @return ApiResult<UserInfoResponseDTO>
     */
    @GetMapping("getUserInfo")
    public ApiResult<UserInfoResponseDTO> getInfo(@Valid UserInfoRequestDTO request) {
        return ApiResult.success(deviceUploadDataService.getUserInfo(request));
    }

    /**
     * FKR710 数据上传
     *
     * @param requestDTO 数据
     *
     * @return 结果
     */
    @PostMapping("fkr710/upload")
    public ApiResult frkUpload(@RequestBody FkrRequestDTO requestDTO) {
        fkrDataService.uploadData(requestDTO);
        return ApiResult.success();
    }

    /**
     * 眼底检查数据上传
     *
     * @param requestDTO 请求DTO
     *
     * @return ReturnInformation
     */
    @PostMapping(value = "/device/uploadFundus", consumes = {"multipart/form-data"})
    public String uploadFundus(DeviceRequestDTO requestDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return deviceUploadService.fundusUpload(requestDTO,user.getOrgId());
    }

}
