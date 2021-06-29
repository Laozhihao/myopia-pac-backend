package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.hospital.service.OrgCooperationHospitalBizService;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.service.DeviceScreeningDataService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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

    /**
     * 判断是否近视
     *
     * @param leftPa  左眼等效球镜
     * @param rightPa 右眼等效球镜
     * @return 是否近视
     */
    private boolean checkIsMyopia(BigDecimal leftPa, BigDecimal rightPa) {
        return checkSingleEyeIsMyopia(leftPa) || checkSingleEyeIsMyopia(rightPa);
    }

    /**
     * 单眼是否近视
     *
     * @param pa 等效球镜
     * @return 是否近视
     */
    private boolean checkSingleEyeIsMyopia(BigDecimal pa) {
        if (Objects.isNull(pa)) {
            return false;
        }
        return pa.compareTo(new BigDecimal("-0.5")) < 0;
    }

    /**
     * 判断是否远视
     *
     * @param patientAge 患者年龄
     * @param leftPa     左眼等效球镜
     * @param rightPa    右眼等效球镜
     * @return 是否远视
     */
    private boolean checkIsFarsightedness(Integer patientAge, BigDecimal leftPa, BigDecimal rightPa) {

    }

    /**
     * 单眼是否远视
     *
     * @param patientAge 患者年龄
     * @param pa         等效球镜
     * @return 是否远视
     */
    private boolean checkSingleEyeIsFarsightedness(Integer patientAge, BigDecimal pa) {
        switch (patientAge) {
            case 0:
                return pa.compareTo(new BigDecimal("3.5")) > 0;
            case 1:
                return pa.compareTo(new BigDecimal("3.5")) > 0;
            case 2:
                return pa.compareTo(new BigDecimal("3.5")) > 0;
            case 3:
                return pa.compareTo(new BigDecimal("3.5")) > 0;
            case 4:
                return pa.compareTo(new BigDecimal("2.5")) > 0;
            case 5:
                return pa.compareTo(new BigDecimal("2.5")) > 0;
            case 6:
                return pa.compareTo(new BigDecimal("2.0")) > 0;
            case 7:
                return pa.compareTo(new BigDecimal("2.0")) > 0;
            case 8:
                return pa.compareTo(new BigDecimal("1.5")) > 0;
            case 9:
                return pa.compareTo(new BigDecimal("1.25")) > 0;
            case 10:
                return pa.compareTo(new BigDecimal("1.0")) > 0;
            case 11:
                return pa.compareTo(new BigDecimal("0.75")) > 0;
            default:
                return pa.compareTo(new BigDecimal("0.50")) > 0;
        }
    }
}
