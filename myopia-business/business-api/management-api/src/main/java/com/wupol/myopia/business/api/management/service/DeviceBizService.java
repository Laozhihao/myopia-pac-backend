package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.hospital.service.OrgCooperationHospitalBizService;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.service.DeviceScreeningDataService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 设备管理
 *
 * @author Simple4H
 */
@Service
public class DeviceBizService {

    @Resource
    private DeviceScreeningDataService deviceScreeningDataService;

    @Resource
    private OrgCooperationHospitalBizService orgCooperationHospitalBizService;

    /**
     * 获取打印需要的信息
     *
     * @param ids ids
     * @return List<DeviceReportPrintResponseDTO>
     */
    public List<DeviceReportPrintResponseDTO> getPrintReportInfo(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException("id不能为空");
        }
        List<DeviceReportPrintResponseDTO> responseDTOS = deviceScreeningDataService.getPrintReportInfo(ids);

        responseDTOS.forEach(r -> {
            r.setSuggestHospitalDO(orgCooperationHospitalBizService.packageSuggestHospital(r.getScreeningOrgId()));
            r.setDoctorAdvice("");
        });
        return responseDTOS;
    }
}
