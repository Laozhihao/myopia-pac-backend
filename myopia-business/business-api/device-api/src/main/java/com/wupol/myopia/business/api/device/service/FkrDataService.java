package com.wupol.myopia.business.api.device.service;

import com.alibaba.fastjson.JSONObject;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

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
    public void uploadData(String data,String clientId) {
        String[] split = StringUtils.split(data, "#!>");
        log.info("str:{}", JSONObject.toJSONString(split));
        String deviceSN = split[1].substring(7);
        Integer planStudentId = Integer.valueOf(split[3].substring(7));
        ThreeTuple<BigDecimal, BigDecimal, BigDecimal> leftData = getData(split[4]);
        ThreeTuple<BigDecimal, BigDecimal, BigDecimal> rightData = getData(split[5]);

        Device device = deviceUploadDataService.getDevice(deviceSN);
        ScreeningOrganization screeningOrganization = deviceUploadDataService.getScreeningOrganization(device);
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = deviceUploadDataService.getScreeningPlanSchoolStudent(screeningOrganization, planStudentId);
        // 保存原始数据
        deviceUploadDataService.saveDeviceData(device, JSONObject.toJSONString(data), planStudentId, screeningOrganization.getId(),System.currentTimeMillis());
        visionScreeningBizService.saveOrUpdateStudentScreenData(getComputerOptometryDTO(leftData, rightData, screeningPlanSchoolStudent),clientId);
    }

    /**
     * 获取数据
     *
     * @param str 数据
     * @return first-球镜 second-柱镜 third-轴位
     */
    private ThreeTuple<BigDecimal, BigDecimal, BigDecimal> getData(String str) {
        return new ThreeTuple<>(new BigDecimal(str.substring(24, 30)), new BigDecimal(str.substring(30, 36)), new BigDecimal(str.substring(36, 39)));
    }

    /**
     * 构建ComputerOptometryDTO
     *
     * @param leftData                   左眼数据
     * @param rightData                  右眼数据
     * @param screeningPlanSchoolStudent 筛查学生
     * @return ComputerOptometryDTO
     */
    private ComputerOptometryDTO getComputerOptometryDTO(ThreeTuple<BigDecimal, BigDecimal, BigDecimal> leftData,
                                                         ThreeTuple<BigDecimal, BigDecimal, BigDecimal> rightData,
                                                         ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
        computerOptometryDTO.setLSph(leftData.getFirst());
        computerOptometryDTO.setLCyl(leftData.getSecond());
        computerOptometryDTO.setLAxial(leftData.getThird());
        computerOptometryDTO.setRSph(rightData.getFirst());
        computerOptometryDTO.setRCyl(rightData.getSecond());
        computerOptometryDTO.setRAxial(rightData.getThird());
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
