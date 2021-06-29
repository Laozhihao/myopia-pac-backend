package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.hospital.service.OrgCooperationHospitalBizService;
import com.wupol.myopia.business.common.utils.constant.DoctorConclusion;
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
            r.setDoctorAdvice(getDoctorAdvice(r.getPatientAge(), r.getLeftPa(), r.getRightPa(), r.getLeftCyl(), r.getRightCyl()));
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
     * @return 医生建议
     */
    private String getDoctorAdvice(Integer patientAge, BigDecimal leftPa, BigDecimal rightPa, BigDecimal leftCyl, BigDecimal rightCyl) {
        // 判断是否近视、散光、远视。其中一项满足则是屈光不正
        if (checkIsMyopia(leftPa, rightPa) || checkIsAstigmatism(leftCyl, rightCyl) || checkIsFarsightedness(patientAge, leftPa, rightPa)) {
            return DoctorConclusion.DEVICE_REFRACTIVE_ERROR;
        } else {
            // 屈光正常还需判断是否远视储备不足情况
            if (checkIsInsufficientFarsightedReserves(patientAge, leftPa, rightPa)) {
                return DoctorConclusion.DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_ERROR;
            } else {
                return DoctorConclusion.DEVICE_REFRACTIVE_NORMAL_INSUFFICIENT_FARSIGHTED_RESERVES_NORMAL;
            }
        }
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
            return true;
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
        return checkSingleEyeIsFarsightedness(patientAge, leftPa) || checkSingleEyeIsFarsightedness(patientAge, rightPa);
    }

    /**
     * 单眼是否远视
     *
     * @param patientAge 患者年龄
     * @param pa         等效球镜
     * @return 是否远视
     */
    private boolean checkSingleEyeIsFarsightedness(Integer patientAge, BigDecimal pa) {
        if (Objects.isNull(patientAge) || Objects.isNull(pa)) {
            return true;
        }
        switch (patientAge) {
            case 0:
            case 1:
            case 2:
            case 3:
                return pa.compareTo(new BigDecimal("3.5")) > 0;
            case 4:
            case 5:
                return pa.compareTo(new BigDecimal("2.5")) > 0;
            case 6:
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

    /**
     * 判断是否散光
     *
     * @param leftCyl  左眼柱镜
     * @param rightCyl 右眼柱镜
     * @return 是否散光
     */
    private boolean checkIsAstigmatism(BigDecimal leftCyl, BigDecimal rightCyl) {
        return checkSingleEyeIsAstigmatism(leftCyl) || checkSingleEyeIsAstigmatism(rightCyl);
    }

    /**
     * 单眼是否散光
     *
     * @param cyl 柱镜
     * @return 是否散光
     */
    private boolean checkSingleEyeIsAstigmatism(BigDecimal cyl) {
        if (Objects.isNull(cyl)) {
            return true;
        }
        return cyl.abs().compareTo(new BigDecimal("0.5")) < 0;
    }

    /**
     * 判断是否远视储备不足
     *
     * @param patientAge 患者年龄
     * @param leftPa     左眼等效球镜
     * @param rightPa    右眼等效球镜
     * @return 是否远视储备不足
     */
    private boolean checkIsInsufficientFarsightedReserves(Integer patientAge, BigDecimal leftPa, BigDecimal rightPa) {
        return checkSingleEyeIsInsufficientFarsightedReserves(patientAge, leftPa) || checkSingleEyeIsInsufficientFarsightedReserves(patientAge, rightPa);
    }

    /**
     * 单眼是否远视储备不足
     *
     * @param patientAge 患者年龄
     * @param pa         等效球镜
     * @return 是否远视储备不足
     */
    private boolean checkSingleEyeIsInsufficientFarsightedReserves(Integer patientAge, BigDecimal pa) {
        if (Objects.isNull(patientAge) || Objects.isNull(pa)) {
            return true;
        }
        if (patientAge >= 0 && patientAge <= 8) {
            return pa.compareTo(new BigDecimal("0")) >= 0 && pa.compareTo(new BigDecimal("1")) <= 0;
        }
        return true;
    }
}
