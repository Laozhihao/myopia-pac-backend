package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.hospital.service.OrgCooperationHospitalBizService;
import com.wupol.myopia.business.api.management.domain.dto.DeviceDTO;
import com.wupol.myopia.business.api.management.domain.vo.DeviceVO;
import com.wupol.myopia.business.common.utils.constant.DoctorConclusion;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import com.wupol.myopia.business.core.device.domain.query.DeviceQuery;
import com.wupol.myopia.business.core.device.domain.vo.DeviceReportTemplateVO;
import com.wupol.myopia.business.core.device.service.DeviceScreeningDataService;
import com.wupol.myopia.business.core.device.service.DeviceService;
import com.wupol.myopia.business.core.device.service.ScreeningOrgBindDeviceReportService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private DistrictService districtService;

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
     * @param patientAge 患者月龄
     * @param leftPa     左眼等效球镜
     * @param rightPa    右眼等效球镜
     * @param leftCyl    左眼柱镜
     * @param rightCyl   右眼柱镜
     * @return left-医生结论 rigjt医生建议
     */
    private TwoTuple<String, String> getDoctorAdvice(Integer patientAge, BigDecimal leftPa, BigDecimal rightPa, BigDecimal leftCyl, BigDecimal rightCyl) {
        if (Objects.isNull(leftPa) && Objects.isNull(rightPa) && Objects.isNull(leftCyl) && Objects.isNull(rightCyl)) {
            return new TwoTuple<>();
        }
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
     * @param patientAge 患者月龄
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
     * @param patientAge 患者月龄
     * @param pa         等效球镜
     * @return 是否远视
     */
    private boolean checkSingleEyeIsFarsightedness(Integer patientAge, BigDecimal pa) {
        if (Objects.isNull(patientAge) || Objects.isNull(pa)) {
            return true;
        }
        // 月龄转换成年龄
        double age = patientAge / 12d;

        if (isLeftBetween(age, 0, 4)) {
            return pa.compareTo(new BigDecimal("3.5")) > 0;
        }
        if (isLeftBetween(age, 4, 6)) {
            return pa.compareTo(new BigDecimal("2.5")) > 0;
        }
        if (isLeftBetween(age, 6, 8)) {
            return pa.compareTo(new BigDecimal("2.0")) > 0;
        }
        if (isLeftBetween(age, 8, 9)) {
            return pa.compareTo(new BigDecimal("1.5")) > 0;
        }
        if (isLeftBetween(age, 9, 10)) {
            return pa.compareTo(new BigDecimal("1.25")) > 0;
        }
        if (isLeftBetween(age, 10, 11)) {
            return pa.compareTo(new BigDecimal("1.0")) > 0;
        }
        if (isLeftBetween(age, 11, 12)) {
            return pa.compareTo(new BigDecimal("0.75")) > 0;
        }
        return pa.compareTo(new BigDecimal("0.50")) > 0;
    }

    /**
     * 是否在两个值的中间（左闭由开）
     *
     * @param target 目标
     * @param var1   值1
     * @param var2   值2
     * @return boolean
     */
    private boolean isLeftBetween(double target, double var1, double var2) {
        return target >= var1 && target < var2;
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
     * @param patientAge 患者月龄
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
     * @param patientAge 患者月龄
     * @param pa         等效球镜
     * @return 是否远视储备不足
     */
    private boolean checkSingleEyeIsInsufficientFarsightedReserves(Integer patientAge, BigDecimal pa) {
        if (Objects.isNull(patientAge) || Objects.isNull(pa)) {
            return true;
        }
        // 月龄转换成年龄
        double age = patientAge / 12d;
        if (isLeftBetween(age, 0, 9)) {
            return pa.compareTo(new BigDecimal("0")) >= 0 && pa.compareTo(new BigDecimal("1")) <= 0;
        }
        return false;
    }

    /**
     * 获取设备列表（分页）
     *
     * @param deviceDTO   查询条件
     * @param pageRequest 分页参数
     * @return {@link com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.wupol.myopia.business.api.management.domain.vo.DeviceVO> }
     **/
    public Page<DeviceVO> getDeviceListByPage(DeviceDTO deviceDTO, PageRequest pageRequest) {
        Assert.notNull(pageRequest, "分页参数为空");
        // 获取指定名称的筛查机构集
        if (Objects.nonNull(deviceDTO) && StringUtils.hasText(deviceDTO.getBindingScreeningOrgName())) {
            List<ScreeningOrganization> screeningOrgList = screeningOrganizationService.getByNameLike(deviceDTO.getBindingScreeningOrgName());
            List<Integer> ids = screeningOrgList.stream().map(ScreeningOrganization::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)) {
                return new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
            }
            deviceDTO.setScreeningOrgIds(ids);
        }
        // 分页查询
        IPage<Device> devicePage = deviceService.getPageByLikeQuery(pageRequest, Objects.nonNull(deviceDTO) ? deviceDTO.toDeviceQuery() : new DeviceQuery());
        List<DeviceVO> deviceList = JSON.parseArray(JSON.toJSONString(devicePage.getRecords()), DeviceVO.class);
        if (CollectionUtils.isEmpty(deviceList)) {
            return new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        }
        // 填充筛查机构的名称和行政区域名称
        return new Page<DeviceVO>(devicePage.getCurrent(), devicePage.getSize(), devicePage.getTotal()).setRecords(fillScreeningOrgNameAndDistrictName(deviceList));
    }

    /**
     * 填充筛查机构的名称和行政区域名称
     *
     * @param deviceList 设备列表
     * @return {@link java.util.List<com.wupol.myopia.business.api.management.domain.vo.DeviceVO>}
     **/
    private List<DeviceVO> fillScreeningOrgNameAndDistrictName(List<DeviceVO> deviceList) {
        if (CollectionUtils.isEmpty(deviceList)) {
            return deviceList;
        }
        List<Integer> screeningOrgIdList = deviceList.stream().map(Device::getBindingScreeningOrgId).distinct().collect(Collectors.toList());
        List<ScreeningOrganization> screeningOrgList = screeningOrganizationService.getByIds(screeningOrgIdList);
        Map<Integer, ScreeningOrganization> screeningOrgNameMap = screeningOrgList.stream().collect(Collectors.toMap(ScreeningOrganization::getId, Function.identity()));
        return deviceList.stream().map(deviceVO -> {
            ScreeningOrganization screeningOrg = screeningOrgNameMap.get(deviceVO.getBindingScreeningOrgId());
            if (Objects.isNull(screeningOrg)) {
                return deviceVO;
            }
            deviceVO.setBindingScreeningOrgName(screeningOrg.getName());
            deviceVO.setBindingScreeningOrgDistrictName(districtService.getDistrictNameByDistrictId(screeningOrg.getDistrictId()));
            return deviceVO;
        }).collect(Collectors.toList());
    }
}
