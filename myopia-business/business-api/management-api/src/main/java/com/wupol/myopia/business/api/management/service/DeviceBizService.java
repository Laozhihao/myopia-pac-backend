package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.SEUtil;
import com.wupol.myopia.business.aggregation.hospital.service.OrgCooperationHospitalBizService;
import com.wupol.myopia.business.api.management.domain.dto.DeviceDTO;
import com.wupol.myopia.business.api.management.domain.vo.DeviceVO;
import com.wupol.myopia.business.common.utils.constant.DoctorConclusion;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.interfaces.HasName;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.common.utils.util.VS550Util;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.device.constant.OrgTypeEnum;
import com.wupol.myopia.business.core.device.domain.dto.DeviceOrgListResponseDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceReportPrintResponseDTO;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import com.wupol.myopia.business.core.device.domain.query.DeviceQuery;
import com.wupol.myopia.business.core.device.domain.vo.DeviceReportTemplateVO;
import com.wupol.myopia.business.core.device.service.DeviceScreeningDataService;
import com.wupol.myopia.business.core.device.service.DeviceService;
import com.wupol.myopia.business.core.device.service.ScreeningOrgBindDeviceReportService;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.organization.constant.ScreeningOrgConfigTypeEnum;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private SchoolService schoolService;

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
        // 获取机构对应的模板）
        List<Integer> orgIds = responseDTOS.stream().map(DeviceScreeningData::getScreeningOrgId).collect(Collectors.toList());
        Map<Integer, Integer> templateMap = screeningOrgBindDeviceReportService.getByOrgIds(orgIds).stream()
                .collect(Collectors.toMap(DeviceReportTemplateVO::getScreeningOrgId, DeviceReportTemplateVO::getTemplateType));

        // 配置 0-省级配置 1-单点配置
        Map<Integer, Integer>  configTypes = screeningOrganizationService.getByIds(orgIds).stream()
                .collect(Collectors.toMap(ScreeningOrganization::getId, ScreeningOrganization::getConfigType));

        responseDTOS.forEach(r -> {
            if (Objects.nonNull(r.getLeftAxsi())) {
                //左眼轴位
                r.setLeftAxsi(BigDecimal.valueOf(r.getLeftAxsi()).setScale(0, RoundingMode.DOWN).doubleValue());
            }
            if (Objects.nonNull(r.getRightAxsi())) {
                //右眼轴位
                r.setRightAxsi(BigDecimal.valueOf(r.getRightAxsi()).setScale(0, RoundingMode.DOWN).doubleValue());
            }
            //建议医院
            r.setSuggestHospitalDTO(orgCooperationHospitalBizService.packageSuggestHospital(r.getScreeningOrgId()));

            TwoTuple<String, String> doctorAdvice = getDoctorAdvice(r.getPatientAge(), r.getLeftPa(), r.getRightPa(), r.getLeftCyl(), r.getRightCyl());
            //医生结论
            r.setDoctorConclusion(doctorAdvice.getFirst());
            //医生建议
            r.setDoctorAdvice(doctorAdvice.getSecond());

            //模板类型 1-VS666模板1（模板由前端渲染））
            r.setTemplateType(templateMap.get(r.getScreeningOrgId()));
            //右眼球镜-展示使用
            r.setRightSphDisplay(calculateResolution(configTypes.get(r.getScreeningOrgId()),r.getRightSph()));
            //左眼球镜-展示使用
            r.setLeftSphDisplay(calculateResolution(configTypes.get(r.getScreeningOrgId()),r.getLeftSph()));

            //右眼柱镜-展示使用
            r.setRightCylDisplay(calculateResolution(configTypes.get(r.getScreeningOrgId()),r.getRightCyl()));
            //左眼柱镜-展示使用
            r.setLeftCylDisplay(calculateResolution(configTypes.get(r.getScreeningOrgId()),r.getLeftCyl()));

            if ( Objects.equals(configTypes,ScreeningOrgConfigTypeEnum.CONFIG_TYPE_4.getType())){
                //右眼等效球镜
                r.setRightPa(SEUtil.getSphericalEquivalent(r.getRightSphDisplay(),r.getRightCylDisplay()));
                //左眼等效球镜
                r.setLeftPa(SEUtil.getSphericalEquivalent(r.getLeftSphDisplay(), r.getLeftCylDisplay()));

            }

        });
        return responseDTOS;
    }

    /**
     * 计算分辨率
     * @param configType vs550配置
     * @param var 传入值
     * @return VS550分辨率配置
     */
    public Double calculateResolution(Integer configType, Double var) {
        if (Objects.equals(configType, ScreeningOrgConfigTypeEnum.CONFIG_TYPE_2.getType())
                || Objects.equals(configType,ScreeningOrgConfigTypeEnum.CONFIG_TYPE_3.getType())
                || Objects.equals(configType,ScreeningOrgConfigTypeEnum.CONFIG_TYPE_4.getType())){
            /*
             * 计算逻辑一（VS550计算逻辑）：VS550配置(原始逻辑)
             * 计算逻辑三（VS550计算逻辑）:VS550配置（0.25D分辨率）
             */
            return VS550Util.getDisplayValue(var);
        }
        if (Objects.equals(configType,ScreeningOrgConfigTypeEnum.CONFIG_TYPE_5.getType())){
            /*
             * 计算逻辑二（VS550计算逻辑）:VS550配置（0.01D分辨率）
             */
            return var;
        }
        return VS550Util.getDisplayValue(var);
    }

    /**
     * 获取医生建议
     *
     * @param patientAge 患者月龄
     * @param leftPa     左眼等效球镜
     * @param rightPa    右眼等效球镜
     * @param leftCyl    左眼柱镜
     * @param rightCyl   右眼柱镜
     * @return left-医生结论 right医生建议
     */
    private TwoTuple<String, String> getDoctorAdvice(Integer patientAge, Double leftPa, Double rightPa, Double leftCyl, Double rightCyl) {
        if (ObjectsUtil.allNull(leftPa, rightPa, leftCyl, rightCyl)) {
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
     * @param patientAge 患者月龄
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
     * @param patientAge    患者月龄
     * @param paDoubleValue 等效球镜
     * @return 是否远视
     */
    private boolean checkSingleEyeIsFarsightedness(Integer patientAge, Double paDoubleValue) {
        if (Objects.isNull(patientAge) || Objects.isNull(paDoubleValue)) {
            return true;
        }
        BigDecimal pa = BigDecimal.valueOf(paDoubleValue);
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
     * @param patientAge 患者月龄
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
     * @param patientAge    患者月龄
     * @param paDoubleValue 等效球镜
     * @return 是否远视储备不足
     */
    private boolean checkSingleEyeIsInsufficientFarsightedReserves(Integer patientAge, Double paDoubleValue) {
        if (Objects.isNull(patientAge) || Objects.isNull(paDoubleValue)) {
            return true;
        }
        BigDecimal pa = BigDecimal.valueOf(paDoubleValue);
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
        // 获取指定名称的机构集
        if (Objects.nonNull(deviceDTO) && StringUtils.hasText(deviceDTO.getBindingScreeningOrgName())) {
            List<Integer> ids = getByNames(deviceDTO.getOrgType(), deviceDTO.getBindingScreeningOrgName()).stream().map(DeviceOrgListResponseDTO::getOrgId).collect(Collectors.toList());
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
     *
     * @return {@link java.util.List<com.wupol.myopia.business.api.management.domain.vo.DeviceVO>}
     **/
    private List<DeviceVO> fillScreeningOrgNameAndDistrictName(List<DeviceVO> deviceList) {
        if (CollectionUtils.isEmpty(deviceList)) {
            return deviceList;
        }
        List<Integer> orgIdList = deviceList.stream().map(Device::getBindingScreeningOrgId).distinct().collect(Collectors.toList());

        // 机构
        List<ScreeningOrganization> screeningOrgList = screeningOrganizationService.getByIds(orgIdList);
        Map<Integer, ScreeningOrganization> screeningOrgNameMap = screeningOrgList.stream().collect(Collectors.toMap(ScreeningOrganization::getId, Function.identity()));

        // 医院
        Map<Integer, Hospital> hospitalMap = hospitalService.listByIds(orgIdList).stream().collect(Collectors.toMap(Hospital::getId, Function.identity()));

        // 学校
        Map<Integer, School> schoolMap = schoolService.listByIds(orgIdList).stream().collect(Collectors.toMap(School::getId, Function.identity()));

        return deviceList.stream().map(deviceVO -> getDeviceVO(screeningOrgNameMap, hospitalMap, schoolMap, deviceVO)).collect(Collectors.toList());
    }

    /**
     * getDeviceVO
     *
     * @return DeviceVO
     */
    private DeviceVO getDeviceVO(Map<Integer, ScreeningOrganization> screeningOrgNameMap, Map<Integer, Hospital> hospitalMap, Map<Integer, School> schoolMap, DeviceVO deviceVO) {
        Integer orgType = deviceVO.getOrgType();
        if (Objects.equals(orgType, OrgTypeEnum.SCREENING.getCode())) {
            return setBindingOrgNameDistrict(screeningOrgNameMap.get(deviceVO.getBindingScreeningOrgId()), deviceVO);
        }
        if (Objects.equals(orgType, OrgTypeEnum.HOSPITAL.getCode())) {
            return setBindingOrgNameDistrict(hospitalMap.get(deviceVO.getBindingScreeningOrgId()), deviceVO);
        }
        if (Objects.equals(orgType, OrgTypeEnum.SCHOOL.getCode())) {
            return setBindingOrgNameDistrict(schoolMap.get(deviceVO.getBindingScreeningOrgId()), deviceVO);
        }
        return deviceVO;
    }

    /**
     * 通过机构/医院/学校
     *
     * @param type 类型
     * @param name 名称
     *
     * @return List<DeviceOrgListResponseDTO>
     */
    public List<DeviceOrgListResponseDTO> getByNames(Integer type, String name) {
        if (Objects.equals(type, OrgTypeEnum.SCREENING.getCode())) {
            List<ScreeningOrganization> nameList = screeningOrganizationService.getByName(name);
            return convert2Dto(nameList);
        }
        if (Objects.equals(type, OrgTypeEnum.HOSPITAL.getCode())) {
            List<HospitalResponseDTO> nameList = hospitalService.getHospitalByName(name, null);
            return convert2Dto(nameList);
        }
        if (Objects.equals(type, OrgTypeEnum.SCHOOL.getCode())) {
            List<School> nameList = schoolService.getBySchoolName(name);
            return convert2Dto(nameList);
        }
        return new ArrayList<>();
    }

    /**
     * 设置机构名字和区域
     */
    private <T extends HasName> DeviceVO setBindingOrgNameDistrict(T t, DeviceVO deviceVO) {
        if (Objects.isNull(t)) {
            return deviceVO;
        }
        deviceVO.setBindingScreeningOrgName(t.getName());
        deviceVO.setBindingScreeningOrgDistrictName(districtService.getDistrictNameByDistrictId(t.getDistrictId()));
        return deviceVO;
    }

    /**
     * 转换成DTO
     */
    private <T extends HasName> List<DeviceOrgListResponseDTO> convert2Dto(List<T> t) {
        List<Integer> districtIds = t.stream().map(HasName::getDistrictId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(districtIds)) {
            return new ArrayList<>();
        }
        Map<Integer, District> districtMap = districtService.getByIds(districtIds);
        return t.stream().map(s -> new DeviceOrgListResponseDTO(s.getId(),
                        s.getName(),
                        districtService.getTopDistrictName(districtMap.get(s.getDistrictId()).getCode())))
                .collect(Collectors.toList());
    }
}
