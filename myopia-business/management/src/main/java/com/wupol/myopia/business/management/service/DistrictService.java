package com.wupol.myopia.business.management.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.CacheKey;
import com.wupol.myopia.business.management.domain.mapper.DistrictMapper;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
@Log4j2
public class DistrictService extends BaseService<DistrictMapper, District> {
    /** 省级行政区域的父节点code */
    private static final long PROVINCE_PARENT_CODE = 100000000L;
    /** 最小行政区域代码编号 */
    private static final long SMALLEST_PROVINCE_CODE = 110000000L;

    @Value(value = "${oem.province.code}")
    private Long oemProvinceCode;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

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
            // 已成功匹配地址
            if (Objects.nonNull(provinceCode) && Objects.nonNull(cityCode) & Objects.nonNull(areaCode) && Objects.nonNull(townCode)) {
                break;
            }
        }
        if (Objects.isNull(provinceCode) || Objects.isNull(cityCode) || Objects.isNull(areaCode) || Objects.isNull(townCode)) {
            throw new BusinessException("未匹配到地址");
        }
        return Arrays.asList(provinceCode, cityCode, areaCode, townCode);
    }

    /** 根据code获取对应的地址 */
    public List<String> getSplitAddress(Long provinceCode, Long cityCode, Long areaCode, Long townCode) throws ValidationException {
        if (Objects.isNull(provinceCode) || Objects.isNull(cityCode) || Objects.isNull(areaCode) || Objects.isNull(townCode)) {
            return Collections.emptyList();
        }
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
        return Arrays.asList(province, city, area, town);
    }
    /** 根据code获取对应的地址 */
    public String getAddressPrefix(Long provinceCode, Long cityCode, Long areaCode, Long townCode) throws ValidationException {
        List<String> list = getSplitAddress(provinceCode, cityCode, areaCode, townCode);
        return list.get(0) + list.get(1) + list.get(2) + list.get(3);
    }

    /** 获取所有地行政区域,带缓存 */
    public List<District> getAllDistrict() {
        String key = CacheKey.DISTRICT_ALL_LIST;
        if (redisUtil.hasKey(key)) {
            return (List<District>) redisUtil.get(key);
        }
        List<District> list = baseMapper.selectList(new QueryWrapper<>());
        redisUtil.set(key, list);
        return list;
    }

    /** 获取所有地行政区域,带缓存 */
    public Map<Integer, String> getAllDistrictIdNameMap() {
        String key = CacheKey.DISTRICT_ID_NAME_MAP;
        if (redisUtil.hasKey(key)) {
            return (Map) redisUtil.get(key);
        }
        Map<Integer, String> districtIdNameMap = getAllDistrict().stream().collect(Collectors.toMap(District::getId, District::getName));
        redisUtil.set(key, districtIdNameMap);
        return districtIdNameMap;
    }

    /**
     * 通过用户身份，过滤查询的行政区域ID
     *  - 如果是平台管理员，则将行政区域ID作为条件
     *  - 如果非平台管理员，则返回当前用户的行政区域ID
     *
     * @param currentUser 当前用户
     * @param districtId  行政区域ID
     * @return 行政区域ID
     */
    public Integer filterQueryDistrictId(CurrentUser currentUser, Integer districtId) {
        // 平台管理员行政区域的筛选条件
        if (currentUser.isPlatformAdminUser()) {
            return districtId;
        }
        // 非平台管理员只能看到自己同级行政区域
        return getNotPlatformAdminUserDistrict(currentUser).getId();
    }

    /**
     * 通过districtDetail获取名称
     *
     * @param districtDetail 前端存的字符串
     * @return 名字
     */
    public String getDistrictName(String districtDetail) {
        StringBuilder name = new StringBuilder();
        if (StringUtils.isBlank(districtDetail)) {
            return name.toString();
        }
        List<District> list = JSONObject.parseObject(districtDetail, new TypeReference<List<District>>() {});
        if (CollectionUtils.isEmpty(list)) {
            return name.toString();
        }
        for (District district : list) {
            name.append(district.getName());
        }
        return name.toString();
    }

    /**
     * 获取以当前登录用户所属行政区域为根节点的行政区域树
     *
     * @param currentUser 当前登录用户
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getCurrentUserDistrictTree(CurrentUser currentUser) {
        // 平台管理员，可看到全国的
        if (currentUser.isPlatformAdminUser()) {
            return getWholeCountryDistrictTreePriorityCache();
        }
        // 非平台管理员，获取以其所属行政区域为根节点的行政区域树
        District parentDistrict = getNotPlatformAdminUserDistrict(currentUser);
        return getSpecificDistrictTreePriorityCache(parentDistrict.getCode());
    }

    /**
     * 获取非平台管理员用户的行政区域
     *
     * @param currentUser 当前登录用户
     * @return com.wupol.myopia.business.management.domain.model.District
     **/
    public District getNotPlatformAdminUserDistrict(CurrentUser currentUser) {
        if (currentUser.isGovDeptUser()) {
            GovDept govDept = govDeptService.getById(currentUser.getOrgId());
            return getById(govDept.getDistrictId());
        }
        if (currentUser.isScreeningUser()) {
            ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(currentUser.getOrgId());
            return getById(screeningOrganization.getDistrictId());
        }
        throw new BusinessException("无效用户类型");
    }

    /**
     * 从缓存获取以指定行政区域为根节点的行政区域树
     *
     * @param rootCode 指定的行政区域代码编号
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getSpecificDistrictTreePriorityCache(long rootCode) {
        // 从缓存获取
        String key = String.format(CacheKey.DISTRICT_TREE, rootCode);
        Object cacheList = redisUtil.get(key);
        if (!Objects.isNull(cacheList)) {
            return JSONObject.parseObject(JSONObject.toJSONString(cacheList), new TypeReference<List<District>>() {});
        }
        // 缓存没有，则从rootCode所属的省份中遍历查找
        District district = getProvinceDistrictTreePriorityCache(rootCode);
        List<District> districtList = Collections.singletonList(district.getCode() == rootCode ? district : getSpecificDistrictTree(district.getChild(), rootCode));
        // 重新加入到缓存
        if (!CollectionUtils.isEmpty(districtList)) {
            redisUtil.set(key, districtList);
        }
        return districtList;
    }

    /**
     * 获取指定行政区域所属省份的所有行政区域树
     *
     * @param districtCode 行政区域代码，如 410102000
     * @return com.wupol.myopia.business.management.domain.model.District
     **/
    public District getProvinceDistrictTreePriorityCache(long districtCode) {
        Assert.isTrue(districtCode >= SMALLEST_PROVINCE_CODE, "无效行政区域代码：" + districtCode);
        // 获取前两位，11-北京市、44-广东省、41-河南省
        String provincePrefix = String.valueOf(districtCode).substring(0, 2);
        Object cache = redisUtil.hget(CacheKey.DISTRICT_ALL_PROVINCE_TREE, provincePrefix);
        if (!Objects.isNull(cache)) {
            return JSONObject.parseObject(JSON.toJSONString(cache), District.class);
        }
        // 查库，获取对应省的行政区域树，110000000、410000000
        List<District> districtList = getDistrictTree(Long.valueOf(provincePrefix) * 10000000);
        Assert.isTrue(!CollectionUtils.isEmpty(districtList), "无该省份数据：" + provincePrefix);
        redisUtil.hset(CacheKey.DISTRICT_ALL_PROVINCE_TREE, provincePrefix, districtList.get(0));
        return districtList.get(0);
    }

    /**
     * 获取指定的行政区及其子区域组成的区域树
     *
     * @param districts 行政区域集合
     * @param rootCode 指定的行政区域代码编号
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public District getSpecificDistrictTree(List<District> districts, long rootCode) {
        String rootCodeStr = String.valueOf(rootCode);
        // 如果不包含“000”，则说明是街道、乡、镇，无下级行政区域。如：110119202-香营乡、110119200-大庄科乡、110119110-井庄镇
        if (!rootCodeStr.contains("000")) {
            return null;
        }
        for (District district : districts) {
            Long code = district.getCode();
            if (rootCode == code) {
                return district;
            }
            String prefix = StrUtil.subBefore(String.valueOf(code), "000", false);
            if (rootCodeStr.startsWith(prefix)) {
                return getSpecificDistrictTree(district.getChild(), code);
            }
        }
        return null;
    }

    /**
     * 获取全国区域树（优先从缓存读取）
     *
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getWholeCountryDistrictTreePriorityCache() {
        Object cacheList = redisUtil.get(CacheKey.DISTRICT_ALL_TREE);
        if (!Objects.isNull(cacheList)) {
            return JSONObject.parseObject(JSONObject.toJSONString(cacheList), new TypeReference<List<District>>() {});
        }
        List<District> districts = getWholeCountryDistrictTree();
        redisUtil.set(CacheKey.DISTRICT_ALL_TREE, districts);
        return districts;
    }

    /**
     * 获取全国区域树（读库）
     *
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getWholeCountryDistrictTree() {
        return baseMapper.selectChildNodeByParentCode(PROVINCE_PARENT_CODE);
    }

    /**
     * 根据指定code，获取其下级行政区域集
     *
     * @param parentCode 行政区域代码编号
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getChildDistrictByParentCodePriorityCache(Long parentCode) throws IOException {
        Assert.notNull(parentCode, "行政区域代码编号不能为空");
        String key = String.format(CacheKey.DISTRICT_CHILD_TREE, parentCode);
        Object cacheList = redisUtil.get(key);
        if (!Objects.isNull(cacheList)) {
            return JSONObject.parseObject(JSONObject.toJSONString(cacheList), new TypeReference<List<District>>() {});
        }
        List<District> districts = findByList(new District().setParentCode(parentCode));
        redisUtil.set(key, districts);
        return districts;
    }

    /**
     * 获取当前登录用户所属层级位置 - 层级链(从省开始到所属层级)
     *
     * @param currentUser 当前登录用户
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getCurrentUserDistrictPositionDetail(CurrentUser currentUser) {
        if (currentUser.isPlatformAdminUser()) {
            return Collections.emptyList();
        }
        District district = getNotPlatformAdminUserDistrict(currentUser);
        return getDistrictPositionDetail(district.getCode());
    }

    /**
     * 获取指定行政区域的层级位置 - 层级链(从省开始到当前层级)
     *
     * @param district 行政区域
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getDistrictPositionDetail(District district) {
        if (Objects.isNull(district)) {
            return Collections.emptyList();
        }
        return getDistrictPositionDetail(district.getCode());
    }

    /**
     * 获取指定行政区域的层级位置 - 层级链(从省开始到当前层级)
     *
     * @param districtCode 行政区域代码
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getDistrictPositionDetail(long districtCode) {
        Assert.isTrue(districtCode >= SMALLEST_PROVINCE_CODE, "无效行政区域代码：" + districtCode);
        List<District> districtList = new ArrayList<>();
        Object cache = redisUtil.hget(CacheKey.DISTRICT_ALL_LIST, String.valueOf(districtCode));
        if (Objects.isNull(cache)) {
            return districtList;
        }
        searchParentDistrictDetail(districtList, JSONObject.parseObject(JSON.toJSONString(cache), District.class));
        return districtList.stream().sorted(Comparator.comparing(District::getCode)).collect(Collectors.toList());
    }

    /**
     * 查找上级行政区域
     *
     * @param districtList 行政区域集
     * @param currentDistrict 当前行政区域
     * @return void
     **/
    private void searchParentDistrictDetail(List<District> districtList, District currentDistrict) {
        if (Objects.isNull(currentDistrict)) {
            return;
        }
        districtList.add(currentDistrict);
        // 为省级时，停止寻找
        if (currentDistrict.getParentCode() != PROVINCE_PARENT_CODE) {
            Object cache = redisUtil.hget(CacheKey.DISTRICT_ALL_LIST, String.valueOf(currentDistrict.getParentCode()));
            searchParentDistrictDetail(districtList, JSONObject.parseObject(JSON.toJSONString(cache), District.class));
        }
    }

    /**
     * 根据行政区域代码编号，获取以其作为根节点的行政区区域树
     *
     * @param districtCode 行政区域代码
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    private List<District> getDistrictTree(Long districtCode) {
        return baseMapper.selectDistrictTree(districtCode);
    }

    /**
     * 根据ID集合，批量获取
     *
     * @param districtIds
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getDistrictByIds(List<Integer> districtIds) {
        Assert.isTrue(!CollectionUtils.isEmpty(districtIds), "行政区ID集不能为空");
        return baseMapper.selectBatchIds(districtIds);
    }

}
