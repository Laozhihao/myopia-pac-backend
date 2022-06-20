package com.wupol.myopia.business.api.device.service;

import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.api.device.domain.dto.FkrRequestDTO;
import com.wupol.myopia.business.api.device.util.ParsePlanStudentUtils;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * TODO:
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class FkrDataService {

    @Resource
    private DeviceUploadDataService deviceUploadDataService;

    @Resource
    private VisionScreeningBizService visionScreeningBizService;

    @Transactional(rollbackFor = Exception.class)
    public void uploadData(FkrRequestDTO requestDTO,String clientId) {
        log.info("str:{}", JSONObject.toJSONString(requestDTO));
        String deviceSN = requestDTO.getDeviceSN();
        Integer planStudentId = ParsePlanStudentUtils.parsePlanStudentId(requestDTO.getUid());

        Device device = deviceUploadDataService.getDevice(deviceSN);
        ScreeningOrganization screeningOrganization = deviceUploadDataService.getScreeningOrganization(device);
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = deviceUploadDataService.getScreeningPlanSchoolStudent(screeningOrganization, planStudentId);
        // 保存原始数据
        deviceUploadDataService.saveDeviceData(device, JSONObject.toJSONString(requestDTO), planStudentId, screeningOrganization.getId(), System.currentTimeMillis());
        visionScreeningBizService.saveOrUpdateStudentScreenData(getComputerOptometryDTO(requestDTO, screeningPlanSchoolStudent),clientId);
    }

    /**
     * 构建ComputerOptometryDTO
     *
     * @param requestDTO                 数据
     * @param screeningPlanSchoolStudent 筛查学生
     *
     * @return ComputerOptometryDTO
     */
    private ComputerOptometryDTO getComputerOptometryDTO(FkrRequestDTO requestDTO, ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
        computerOptometryDTO.setLSph(new BigDecimal(requestDTO.getLeftSph()));
        computerOptometryDTO.setLCyl(new BigDecimal(requestDTO.getLeftCyl()));
        computerOptometryDTO.setLAxial(new BigDecimal(requestDTO.getLeftAxial()));
        computerOptometryDTO.setRSph(new BigDecimal(requestDTO.getRightSph()));
        computerOptometryDTO.setRCyl(new BigDecimal(requestDTO.getRightCyl()));
        computerOptometryDTO.setRAxial(new BigDecimal(requestDTO.getRightAxial()));
//        computerOptometryDTO.setDiagnosis();
        computerOptometryDTO.setIsCooperative(0);
        computerOptometryDTO.setSchoolId(String.valueOf(screeningPlanSchoolStudent.getSchoolId()));
        computerOptometryDTO.setDeptId(screeningPlanSchoolStudent.getScreeningOrgId());
        computerOptometryDTO.setCreateUserId(-1);
        computerOptometryDTO.setPlanStudentId(String.valueOf(screeningPlanSchoolStudent.getId()));
        computerOptometryDTO.setIsState(0);
        return computerOptometryDTO;
    }
}
