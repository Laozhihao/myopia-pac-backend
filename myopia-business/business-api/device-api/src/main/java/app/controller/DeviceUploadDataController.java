package app.controller;

import app.service.DeviceUploadDataService;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.screening.app.domain.dto.DeviceUploadDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;


/**
 * 设备上传接口
 *
 * @Author Jacob
 * @Date 2021-07-05
 */
@CrossOrigin
@ResponseResultBody
@Controller
@RequestMapping("/api")
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
    public void uploadDeviceData(@Valid @RequestBody DeviceUploadDTO deviceUploadDto){
        deviceUploadDataService.uploadDeviceData(deviceUploadDto);
    }

}
