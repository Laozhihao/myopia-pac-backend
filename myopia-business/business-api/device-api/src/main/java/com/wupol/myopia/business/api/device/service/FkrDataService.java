package com.wupol.myopia.business.api.device.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.api.device.domain.dto.FkrRequestDTO;
import com.wupol.myopia.business.api.device.util.ParsePlanStudentUtils;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * Fkr数据上传
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
    public void uploadData(FkrRequestDTO requestDTO) {
        log.info("str:{}", JSON.toJSONString(requestDTO));
        String deviceSN = requestDTO.getDeviceSN();
        Integer planStudentId = ParsePlanStudentUtils.parsePlanStudentId(requestDTO.getUid());

        Device device = deviceUploadDataService.getDevice(deviceSN);
        Integer orgId = deviceUploadDataService.getOrganizationIdThrowException(device);
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = deviceUploadDataService.getScreeningPlanSchoolStudent(orgId, planStudentId);
        // 保存原始数据
        deviceUploadDataService.saveDeviceData(device, JSON.toJSONString(requestDTO), planStudentId, orgId, System.currentTimeMillis());
        visionScreeningBizService.saveOrUpdateStudentScreenData(getComputerOptometryDTO(requestDTO, screeningPlanSchoolStudent));
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
        computerOptometryDTO.setLK1(new BigDecimal(requestDTO.getLeftK1()));
        computerOptometryDTO.setLK2(new BigDecimal(requestDTO.getLeftK2()));
        computerOptometryDTO.setRK1(new BigDecimal(requestDTO.getRightK1()));
        computerOptometryDTO.setRK2(new BigDecimal(requestDTO.getRightK2()));
        computerOptometryDTO.setIsCooperative(0);
        computerOptometryDTO.setSchoolId(String.valueOf(screeningPlanSchoolStudent.getSchoolId()));
        computerOptometryDTO.setDeptId(screeningPlanSchoolStudent.getScreeningOrgId());
        computerOptometryDTO.setCreateUserId(-1);
        computerOptometryDTO.setPlanStudentId(String.valueOf(screeningPlanSchoolStudent.getId()));
        computerOptometryDTO.setIsState(0);
        return computerOptometryDTO;
    }
}
