package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.mapper.DistrictMapper;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.mapper.DistrictMapper;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.xml.bind.ValidationException;
import java.util.Arrays;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class DistrictService extends BaseService<DistrictMapper, District> {

    @Autowired
    private GovDeptService govDeptService;

    /** 获取地址的前缀,省市区镇 */
    public String getAddressPrefix(Long provinceCode, Long cityCode, Long areaCode, Long townCode) throws ValidationException {
        String province = null, city = null, area = null, town = null;
        List<District> districtList = baseMapper.findByCodeList(provinceCode, cityCode, areaCode, townCode);
//        List<District> districtList = baseMapper.selectBatchIds(Arrays.asList(""));
        for (District item : districtList) {
            if (item.getCode().equals(provinceCode)) {
                province = item.getName();
            } else if (item.getCode().equals(cityCode)) {
                city = item.getName();
            } else if (item.getCode().equals(areaCode)) {
                area = item.getName();
            } else if (item.getCode().equals(townCode)) {
                town = item.getName();
            }
        }
        if (StringUtils.isBlank(province) || StringUtils.isBlank(city) || StringUtils.isBlank(area) || StringUtils.isBlank(town)) {
            throw new ValidationException(String.format("未匹配到地址: province=%s, city=%s, area=%s, town=%s",
                    provinceCode, cityCode, areaCode, townCode));
        }
        return province + city + area + town;
    }

    /**
     * 获取行政区树
     *
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getCurrentUserDistrictTree() {
        Integer orgId = CurrentUserUtil.getCurrentUser().getOrgId();
        GovDept govDept = govDeptService.getById(orgId);
        District district = getById(govDept.getDistrictId());
        return baseMapper.selectDistrictTree(district.getCode());
    }
}
