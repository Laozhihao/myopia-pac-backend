package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.device.constant.OrgTypeEnum;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataAndOrgDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataQueryDTO;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import com.wupol.myopia.business.core.device.service.DeviceScreeningDataService;
import com.wupol.myopia.business.core.device.service.DeviceService;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/6/29 17:42
 */
@Service
public class DeviceScreeningDataBizService {

    @Autowired
    private DeviceScreeningDataService deviceScreeningDataService;

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DeviceService deviceService;

    /**
     * 分页查询
     *
     * @param query
     * @param pageRequest
     *
     * @return
     */
    public IPage<DeviceScreeningDataAndOrgDTO> getPage(DeviceScreeningDataQueryDTO query, PageRequest pageRequest) {
        Page<DeviceScreeningData> page = pageRequest.getPage();
        // 如果筛查条件有机构名称，转化为id
        if (StringUtils.isNotBlank(query.getScreeningOrgNameSearch())) {
            List<ScreeningOrganization> byNameLike = screeningOrganizationService.getByNameLike(query.getScreeningOrgNameSearch(), Boolean.FALSE);
            List<Integer> orgIds = byNameLike.stream().map(ScreeningOrganization::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(orgIds)) {
                return new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
            }
            query.setScreeningOrgIds(orgIds);
        }

        IPage<DeviceScreeningDataAndOrgDTO> datas = deviceScreeningDataService.selectPageByQuery(page, query);
        List<DeviceScreeningDataAndOrgDTO> records = datas.getRecords();

        if (CollectionUtils.isEmpty(records)) {
            return new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        }

        // 获取学校
        Map<Integer, String> schoolMap = schoolService.getSchoolMap(records, DeviceScreeningDataAndOrgDTO::getScreeningOrgId);
        // 获取医院
        Map<Integer, Hospital> hospitalMap = hospitalService.getHospitalMap(records, DeviceScreeningDataAndOrgDTO::getScreeningOrgId);
        // 获取机构
        Map<Integer, ScreeningOrganization> screeningOrganizationMap = screeningOrganizationService.getScreeningOrganizationMap(records, DeviceScreeningDataAndOrgDTO::getScreeningOrgId);
        // 设备
        Map<Integer, Device> deviceMap = deviceService.getDeviceMap(records, DeviceScreeningDataAndOrgDTO::getDeviceId);

        // 查询机构名称
        records.forEach(x -> {

            Device device = deviceMap.get(x.getDeviceId());
            if (Objects.nonNull(device)) {
                x.setScreeningOrgName(getOrgName(device, schoolMap, hospitalMap, screeningOrganizationMap));
            }

            if (Objects.nonNull(x.getLeftAxsi())) {
                x.setLeftAxsi(BigDecimal.valueOf(x.getLeftAxsi()).setScale(0, RoundingMode.DOWN).doubleValue());
            }
            if (Objects.nonNull(x.getRightAxsi())) {
                x.setRightAxsi(BigDecimal.valueOf(x.getRightAxsi()).setScale(0, RoundingMode.DOWN).doubleValue());
            }
        });
        return datas;
    }

    /**
     * 获取机构名称
     *
     * @param device                   设备
     * @param schoolMap                学校
     * @param hospitalMap              医院
     * @param screeningOrganizationMap 机构
     *
     * @return 机构名称
     */
    private String getOrgName(Device device, Map<Integer, String> schoolMap, Map<Integer, Hospital> hospitalMap, Map<Integer, ScreeningOrganization> screeningOrganizationMap) {
        if (Objects.equals(device.getOrgType(), OrgTypeEnum.SCREENING.getCode())) {
            return screeningOrganizationMap.getOrDefault(device.getBindingScreeningOrgId(), new ScreeningOrganization()).getName();
        }
        if (Objects.equals(device.getOrgType(), OrgTypeEnum.HOSPITAL.getCode())) {
            return hospitalMap.getOrDefault(device.getBindingScreeningOrgId(), new Hospital()).getName();
        }
        if (Objects.equals(device.getOrgType(), OrgTypeEnum.SCHOOL.getCode())) {
            return schoolMap.get(device.getBindingScreeningOrgId());

        }
        return StringUtils.EMPTY;
    }

}
