package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.CacheKey;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.mapper.DistrictMapper;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.mapper.DistrictMapper;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class DistrictService extends BaseService<DistrictMapper, District> {

    @Value(value = "${oem.province.code}")
    private Long oemProvinceCode;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private RedisUtil redisUtil;

    /** 根据地址名查code */
    public List<Long> getCodeByName(String provinceName, String cityName, String areaName, String townName) throws BusinessException{
        Long provinceCode = null, cityCode = null, areaCode = null, townCode = null;
        List<District> districtList = getAllDistrict();
        for (District item : districtList) {
            if (item.getName().equals(provinceName)) {
                provinceCode = item.getCode();
            } else if (item.getName().equals(cityName)) {
                cityCode = item.getCode();
            } else if (item.getName().equals(areaName)) {
                areaCode = item.getCode();
            } else if (item.getName().equals(townName)) {
                townCode = item.getCode();
            }
        }
        if (Objects.isNull(provinceCode) || Objects.isNull(cityCode) || Objects.isNull(areaCode) || Objects.isNull(townCode)) {
            throw new BusinessException("未匹配到地址");
        }
        return Arrays.asList(provinceCode, cityCode, areaCode, townCode);
    }

    /** 根据code获取对应的地址 */
    public String getAddressPrefix(Long provinceCode, Long cityCode, Long areaCode, Long townCode) throws ValidationException {
        String province = null, city = null, area = null, town = null;
        List<District> districtList = baseMapper.findByCodeList(provinceCode, cityCode, areaCode, townCode);
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
        Long code;
        if (govDept.getDistrictId() == -1) {
            code = oemProvinceCode;
        } else {
            District district = getById(govDept.getDistrictId());
            code = district.getCode();
        }
        // 前端级联控件需要数组返回
        return Arrays.asList(getDistrictWithChildByCode(code));
    }

    /**
     * 获取指定行政区域和它的下属区域,带缓存
     * @param code 行政区域code
     * @return
     */
    public District getDistrictWithChildByCode(Long code) {
        if (code < 110000000) {
            throw new BusinessException("非法的行政区域编号: "+code);
        }
        String key = String.format(CacheKey.DISTRICT_CODE, code);
        if (redisUtil.hasKey(key)) {
            return (District) redisUtil.get(key);
        }
        // 该省的数据
        List<District> provinceList = getDistrictByProvincePrefixCode(code);
        District district = null;
        for (District item : provinceList) {
            if (item.getCode().equals(code)) {
                district = item;
                break;
            }
        }
        if (Objects.isNull(district)) {
            throw new BusinessException("非法的行政区域编号: "+code);
        }
        // 获取下属的行政区域数据
        district.setChild(getDistrictByParentCode(provinceList, district.getCode()));
        redisUtil.set(key, district);
        return district;
    }

    /**
     * 获取指定行政区域和它的下属区域,带缓存
     * @param parentCode 行政区域code
     * @return
     */
    private List<District> getDistrictByParentCode(List<District> provinceList, Long parentCode) {
        String key = String.format(CacheKey.DISTRICT_PARENT_CODE, parentCode);
        if (redisUtil.hasKey(key)) {
            return (List<District>) redisUtil.get(key);
        }
        List<District> list = new ArrayList<>();
        for (District item : provinceList) {
            if (parentCode.equals(item.getParentCode())) {
                item.setChild(getDistrictByParentCode(provinceList, item.getCode()));
                list.add(item);
            }
        }
        redisUtil.set(key, list);
        return list;
    }

    /** 通过比较前两位来取该省的数据 */
    private List<District> getDistrictByProvincePrefixCode(Long code) {
        String key = String.format(CacheKey.DISTRICT_PROVINCE_CODE, code);
        if (redisUtil.hasKey(key)) {
            return (List<District>) redisUtil.get(key);
        }
        // 通过比较前两位来取该省的数据
        int suffix = 10000000;
        List<District> list = getAllDistrict().stream().filter(item->
                (int)(item.getCode() / suffix) == (int)(code / suffix)
        ).collect(Collectors.toList());
        redisUtil.set(key, list);
        return list;
    }

    /** 获取所有地行政区域,带缓存 */
    public List<District> getAllDistrict() {
        String key = CacheKey.DISTRICT;
        if (redisUtil.hasKey(key)) {
            return (List<District>) redisUtil.get(key);
        }
        List<District> list = baseMapper.selectList(new QueryWrapper<>());
        redisUtil.set(key, list);
        return list;
    }

}
