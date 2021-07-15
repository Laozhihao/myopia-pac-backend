package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataAndOrgDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataQueryDTO;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import com.wupol.myopia.business.core.device.service.DeviceScreeningDataService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
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

    /**
     * 分页查询
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<DeviceScreeningDataAndOrgDTO> getPage(DeviceScreeningDataQueryDTO query, PageRequest pageRequest) {
        Page<DeviceScreeningData> page = (Page<DeviceScreeningData>) pageRequest.toPage();
        // 如果筛查条件有机构名称，转化为id
        if (StringUtils.isNotBlank(query.getScreeningOrgNameSearch())) {
            List<ScreeningOrganization> byNameLike = screeningOrganizationService.getByNameLike(query.getScreeningOrgNameSearch());
            List<Integer> orgIds = byNameLike.stream().map(ScreeningOrganization::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(orgIds)) {
                return new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
            }
            query.setScreeningOrgIds(orgIds);
        }
        IPage<DeviceScreeningDataAndOrgDTO> datas = deviceScreeningDataService.selectPageByQuery(page, query);
        // 查询机构名称
        datas.getRecords().forEach(x -> {
            x.setScreeningOrgName(screeningOrganizationService.getNameById(x.getScreeningOrgId()));
            if (Objects.nonNull(x.getLeftAxsi())) {
                x.setLeftAxsi(x.getLeftAxsi().setScale(0, BigDecimal.ROUND_DOWN));
            }
            if (Objects.nonNull(x.getRightAxsi())) {
                x.setRightAxsi(x.getRightAxsi().setScale(0, BigDecimal.ROUND_DOWN));
            }
        });
        return datas;
    }

}
