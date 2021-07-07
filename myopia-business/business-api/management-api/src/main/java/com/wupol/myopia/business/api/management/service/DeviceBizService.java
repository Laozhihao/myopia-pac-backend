package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.hospital.service.OrgCooperationHospitalBizService;
import com.wupol.myopia.business.common.utils.constant.DoctorConclusion;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import com.wupol.myopia.business.core.device.domain.vo.DeviceReportTemplateVO;
import com.wupol.myopia.business.core.device.service.DeviceScreeningDataService;
import com.wupol.myopia.business.core.device.service.ScreeningOrgBindDeviceReportService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Resource
    private ScreeningOrgBindDeviceReportService screeningOrgBindDeviceReportService;

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
        if (CollectionUtils.isEmpty(responseDTOS)) {
            return responseDTOS;
        }
        // 获取模板
        List<Integer> orgIds = responseDTOS.stream().map(DeviceScreeningData::getScreeningOrgId).collect(Collectors.toList());
        Map<Integer, Integer> templateMap = screeningOrgBindDeviceReportService.getByOrgIds(orgIds).stream()
                .collect(Collectors.toMap(DeviceReportTemplateVO::getScreeningOrgId, DeviceReportTemplateVO::getTemplateType));
        responseDTOS.forEach(r -> {
            r.setSuggestHospitalDTO(orgCooperationHospitalBizService.packageSuggestHospital(r.getScreeningOrgId()));
            TwoTuple<String, String> doctorAdvice = getDoctorAdvice(r.getPatientAge(), r.getLeftPa(), r.getRightPa(), r.getLeftCyl(), r.getRightCyl());
            r.setDoctorConclusion(doctorAdvice.getFirst());
            r.setDoctorAdvice(doctorAdvice.getSecond());
            r.setTemplateType(templateMap.get(r.getScreeningOrgId()));
        });
        return responseDTOS;
    }

    /**
     * 获取医生建议
     *
     * @param patientAge 患者年龄
     * @param leftPa     左眼等效球镜
     * @param rightPa    右眼等效球镜
     * @param leftCyl    左眼柱镜
     * @param rightCyl   右眼柱镜
     * @return left-医生结论 rigjt医生建议
     */
    private TwoTuple<String, String> getDoctorAdvice(Integer patientAge, Double leftPa, Double rightPa, Double leftCyl, Double rightCyl) {
        // 判断是否近视、散光、远视。其中一项满足则是屈光不正
        if (checkIsMyopia(leftPa, rightPa) || checkIsAstigmatism(leftCyl, rightCyl) || checkIsFarsightedness(patientAge, leftPa, rightPa)) {
            return new TwoTuple<>(DoctorConclusion.CONCLUSION_DEVICE_REFRACTIVE_ERROR, DoctorConclusion.DEVICE_REFRACTIVE_ERROR);
        }
        // 屈光正常还需判断是否远视储备不足情况
        if (checkIsInsufficientFarsightedReserves(patientAge, leftPa, rightPa)) {
            return new TwoTuple<>(DoctorConclusion.CONCLUSION_DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_ERROR, DoctorConclusion.DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_ERROR);
        } else {
            return new TwoTuple<>(DoctorConclusion.CONCLUSION_DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_NORMAL, DoctorConclusion.DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_NORMAL);
        }
    }

    /**
     * 判断是否近视
     *
     * @param leftPa  左眼等效球镜
     * @param rightPa 右眼等效球镜
     * @return 是否近视
     */
    private boolean checkIsMyopia(Double leftPa, Double rightPa) {
        return checkSingleEyeIsMyopia(leftPa) || checkSingleEyeIsMyopia(rightPa);
    }

    /**
     * 单眼是否近视
     *
     * @param pa 等效球镜
     * @return 是否近视
     */
    private boolean checkSingleEyeIsMyopia(Double pa) {
        if (Objects.isNull(pa)) {
            return true;
        }
        return BigDecimal.valueOf(pa).compareTo(new BigDecimal("-0.5")) < 0;
    }

    /**
     * 判断是否远视
     *
     * @param patientAge 患者年龄
     * @param leftPa     左眼等效球镜
     * @param rightPa    右眼等效球镜
     * @return 是否远视
     */
    private boolean checkIsFarsightedness(Integer patientAge, Double leftPa, Double rightPa) {
        return checkSingleEyeIsFarsightedness(patientAge, leftPa) || checkSingleEyeIsFarsightedness(patientAge, rightPa);
    }

    /**
     * 单眼是否远视
     *
     * @param patientAge 患者年龄
     * @param pa         等效球镜
     * @return 是否远视
     */
    private boolean checkSingleEyeIsFarsightedness(Integer patientAge, Double pa) {
        if (Objects.isNull(patientAge) || Objects.isNull(pa)) {
            return true;
        }
        BigDecimal paBigDecimal = BigDecimal.valueOf(pa);
        switch (patientAge) {
            case 0:
            case 1:
            case 2:
            case 3:
                return paBigDecimal.compareTo(new BigDecimal("3.5")) > 0;
            case 4:
            case 5:
                return paBigDecimal.compareTo(new BigDecimal("2.5")) > 0;
            case 6:
            case 7:
                return paBigDecimal.compareTo(new BigDecimal("2.0")) > 0;
            case 8:
                return paBigDecimal.compareTo(new BigDecimal("1.5")) > 0;
            case 9:
                return paBigDecimal.compareTo(new BigDecimal("1.25")) > 0;
            case 10:
                return paBigDecimal.compareTo(new BigDecimal("1.0")) > 0;
            case 11:
                return paBigDecimal.compareTo(new BigDecimal("0.75")) > 0;
            default:
                return paBigDecimal.compareTo(new BigDecimal("0.50")) > 0;
        }
    }

    /**
     * 判断是否散光
     *
     * @param leftCyl  左眼柱镜
     * @param rightCyl 右眼柱镜
     * @return 是否散光
     */
    private boolean checkIsAstigmatism(Double leftCyl, Double rightCyl) {
        return checkSingleEyeIsAstigmatism(leftCyl) || checkSingleEyeIsAstigmatism(rightCyl);
    }

    /**
     * 单眼是否散光
     *
     * @param cyl 柱镜
     * @return 是否散光
     */
    private boolean checkSingleEyeIsAstigmatism(Double cyl) {
        if (Objects.isNull(cyl)) {
            return true;
        }
        return BigDecimal.valueOf(cyl).abs().compareTo(new BigDecimal("0.5")) < 0;
    }

    /**
     * 判断是否远视储备不足
     *
     * @param patientAge 患者年龄
     * @param leftPa     左眼等效球镜
     * @param rightPa    右眼等效球镜
     * @return 是否远视储备不足
     */
    private boolean checkIsInsufficientFarsightedReserves(Integer patientAge, Double leftPa, Double rightPa) {
        return checkSingleEyeIsInsufficientFarsightedReserves(patientAge, leftPa) || checkSingleEyeIsInsufficientFarsightedReserves(patientAge, rightPa);
    }

    /**
     * 单眼是否远视储备不足
     *
     * @param patientAge 患者年龄
     * @param pa         等效球镜
     * @return 是否远视储备不足
     */
    private boolean checkSingleEyeIsInsufficientFarsightedReserves(Integer patientAge, Double pa) {
        if (Objects.isNull(patientAge) || Objects.isNull(pa)) {
            return true;
        }
        if (patientAge >= 0 && patientAge <= 8) {
            BigDecimal paBigDecimal = BigDecimal.valueOf(pa);
            return paBigDecimal.compareTo(BigDecimal.valueOf(0)) >= 0 && paBigDecimal.compareTo(BigDecimal.valueOf(1)) <= 0;
        }
        return true;
    }
}
