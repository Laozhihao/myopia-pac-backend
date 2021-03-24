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
import com.wupol.myopia.business.management.util.TwoTuple;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
@Log4j2
public class DistrictService extends BaseService<DistrictMapper, District> {
    /**
     * 省级行政区域的父节点code
     */
    private static final long PROVINCE_PARENT_CODE = 100000000L;
    /**
     * 最小行政区域代码编号
     */
    private static final long SMALLEST_PROVINCE_CODE = 110000000L;

    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    /**
     * 根据code查地址
     */
    public District getByCode(Long code) throws BusinessException {
        return baseMapper.selectOne(new QueryWrapper<District>().eq("code", code));
    }

    /**
     * 根据code查地址
     *
     * @param codes code
     * @return List<District>
     */
    public List<District> getByCodes(List<Long> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return new ArrayList<>();
        }
        return baseMapper.getByCodes(codes);
    }

    /**
     * 根据地址名查code，查不到时直接返回emptyList
     */
    public List<Long> getCodeByName(String provinceName, String cityName, String areaName, String townName) throws BusinessException {
        List<District> districtList = getWholeCountryDistrictTreePriorityCache();
        TwoTuple<Long, List<District>> provinceCodeWithChild = findInChild(provinceName, districtList);
        TwoTuple<Long, List<District>> cityCodeWithChild = findInChild(cityName, provinceCodeWithChild.getSecond());
        TwoTuple<Long, List<District>> areaCodeWithChild = findInChild(areaName, cityCodeWithChild.getSecond());
        TwoTuple<Long, List<District>> townCodeWithChild = findInChild(townName, areaCodeWithChild.getSecond());

        if (Objects.isNull(provinceCodeWithChild.getFirst()) || Objects.isNull(cityCodeWithChild.getFirst())
                || Objects.isNull(areaCodeWithChild.getFirst()) || Objects.isNull(townCodeWithChild.getFirst())) {
            log.warn("根据地址名查无code, provinceName: {}, cityName: {}, areaName: {}, townName: {}", provinceName, cityName, areaName, townName);
            return Collections.emptyList();
        }
        return Arrays.asList(provinceCodeWithChild.getFirst(), cityCodeWithChild.getFirst(), areaCodeWithChild.getFirst(), townCodeWithChild.getFirst());
    }

    private TwoTuple<Long, List<District>> findInChild(String name, List<District> districtList) {
        if (CollectionUtils.isEmpty(districtList)) {
            return new TwoTuple<>(null, Collections.emptyList());
        }
        for (District district : districtList) {
            if (district.getName().equals(name)) {
                return new TwoTuple<>(district.getCode(), district.getChild());
            }
        }
        return new TwoTuple<>(null, Collections.emptyList());
    }

    /**
     * 通过用户身份，过滤查询的行政区域ID
     * - 如果是平台管理员，则将行政区域ID作为条件
     * - 如果非平台管理员，则返回当前用户的行政区域ID
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
        List<District> list = JSONObject.parseObject(districtDetail, new TypeReference<List<District>>() {
        });
        if (CollectionUtils.isEmpty(list)) {
            return name.toString();
        }
        for (District district : list) {
            name.append(district.getName());
        }
        return name.toString();
    }

    /**
     * 通过districtId获取层级全名（如：XX省XX市）
     *
     * @param districtId 区域ID
     * @return 名字
     */
    public String getDistrictNameByDistrictId(Integer districtId) {
        StringBuilder name = new StringBuilder();
        List<District> list = getDistrictPositionDetailById(districtId);
        if (CollectionUtils.isEmpty(list)) {
            return name.toString();
        }
        for (District district : list) {
            name.append(district.getName());
        }
        return name.toString();
    }

    /**
     * 通过 指定行政区域的层级位置 - 层级链(从省开始到当前层级)  获取层级全名（如：XX省XX市）
     *
     * @param list 区域ID
     * @return 名字
     */
    public String getDistrictNameByDistrictPositionDetail(List<District> list) {
        StringBuilder name = new StringBuilder();
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
    public List<District> getCurrentUserDistrictTree(CurrentUser currentUser) throws IOException {
        // 平台管理员，可看到全国的
        if (currentUser.isPlatformAdminUser()) {
            return getWholeCountryDistrictTreePriorityCache();
        }
        // 非平台管理员，获取以其所属行政区域为根节点的行政区域树
        District parentDistrict = getNotPlatformAdminUserDistrict(currentUser);
        return getSpecificDistrictTreePriorityCache(parentDistrict.getCode());
    }

    /**
     * 获取以当前登录用户所属行政区域为根节点的行政区域树所有ID
     *
     * @param currentUser 当前登录用户
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<Integer> getCurrentUserDistrictTreeAllIds(CurrentUser currentUser) throws IOException {
        List<District> districtTrees = getCurrentUserDistrictTree(currentUser);
        List<Integer> districtIds = new ArrayList<>();
        getAllIds(districtIds, districtTrees);
        return districtIds;
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
     * @param districtId 根节点行政区域
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getSpecificDistrictTree(Integer districtId) throws IOException {
        // 获取以指定行政区域为根节点的行政区域树
        District district = getById(districtId);
        return getSpecificDistrictTreePriorityCache(district.getCode());
    }

    /**
     * 从缓存获取以指定行政区域为根节点的行政区域树的所有行政区域ID
     *
     * @param districtId 根节点行政区域
     * @return java.util.List<Integer>
     **/
    public List<Integer> getSpecificDistrictTreeAllDistrictIds(Integer districtId) throws IOException {
        List<Integer> childDistrictIds = new ArrayList<>();
        // 获取以指定行政区域为根节点的行政区域树
        try {
            List<District> childDistrictList = getSpecificDistrictTree(districtId);
            getAllIds(childDistrictIds, childDistrictList);
        } catch (Exception e) {
            log.error("获取区域层级失败", e);
        }
        return childDistrictIds;
    }

    /**
     * 从缓存获取以指定行政区域为根节点的行政区域树
     *
     * @param rootCode 指定的行政区域代码编号
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getSpecificDistrictTreePriorityCache(long rootCode) throws IOException {
        // 从缓存获取
        String key = String.format(CacheKey.DISTRICT_TREE, rootCode);
        Object cacheList = redisUtil.get(key);
        if (!Objects.isNull(cacheList)) {
            return JSONObject.parseObject(JSONObject.toJSONString(cacheList), new TypeReference<List<District>>() {});
        }
        // 缓存没有，则从rootCode所属的省份中遍历查找
        District district = getProvinceDistrictTreePriorityCache(rootCode);
        List<District> districtList = Collections.singletonList(district.getCode() == rootCode ? district : getSubTreeFromDistrictTree(district.getChild(), rootCode));
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
        District provinceDistrictTree = getDistrictTree(Long.valueOf(provincePrefix) * 10000000);
        Assert.notNull(provinceDistrictTree, "无该省份数据：" + provincePrefix);
        redisUtil.hset(CacheKey.DISTRICT_ALL_PROVINCE_TREE, provincePrefix, provinceDistrictTree);
        return provinceDistrictTree;
    }

    /**
     * 获取指定行政区域所属省份的所有行政区域树
     * @param districtId 行政区域Id
     * @return
     */
    private District getProvinceDistrictTreePriorityCacheById(Integer districtId) {
        District district = getById(districtId);
        Assert.notNull(district, "无效行政区域ID：" + districtId);
        return getProvinceDistrictTreePriorityCache(district.getCode());
    }

    /**
     * 获取指定行政区域所在省份的层级ID
     * @param districtId 行政区域Id
     * @return
     */
    public Integer getProvinceId(Integer districtId) {
        District district = getProvinceDistrictTreePriorityCacheById(districtId);
        return district.getId();
    }

    /**
     * 获取指定行政区域所属省份的所有行政区域的id列表
     *
     * @param districtId 行政区域ID
     * @return List
     **/
    public List<Integer> getProvinceAllDistrictIds(Integer districtId) {
        District provinceDistrictTreePriorityCache = getProvinceDistrictTreePriorityCacheById(districtId);
        List<Integer> districtIds = new ArrayList<>();
        districtIds.add(provinceDistrictTreePriorityCache.getId());
        getAllIds(districtIds, provinceDistrictTreePriorityCache.getChild());
        return districtIds;
    }

    /**
     * 获取指定行政区域所属省份的所有行政区域的id列表
     *
     * @param districtId 行政区域ID
     * @return List
     **/
    public List<Integer> getAllDistrictIds(Integer districtId) {
        District districtTree = getDistrictTree(districtId);
        Assert.notNull(districtTree, "无效行政区域ID：" + districtId);
        List<Integer> districtIds = new ArrayList<>();
        getAllIds(districtIds, districtTree.getChild());
        districtIds.add(districtTree.getId());
        return districtIds;
    }


    /**
     * 获取层级所有子孙层级的ID
     *
     * @param districtIds
     * @param childs
     */
    public void getAllIds(List<Integer> districtIds, List<District> childs) {
        if (CollectionUtils.isEmpty(childs)) {
            return;
        }
        districtIds.addAll(childs.stream().filter(Objects::nonNull).map(District::getId).collect(Collectors.toList()));
        childs.forEach(district -> getAllIds(districtIds, district.getChild()));
    }

    /**
     * 获取指定的行政区及其子区域组成的区域树
     *
     * @param districts 行政区域集合
     * @param rootCode  指定的行政区域代码编号
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public District getSubTreeFromDistrictTree(List<District> districts, long rootCode) throws IOException {
        String rootCodeStr = String.valueOf(rootCode);
        // 如果不包含“000”，则说明是街道、乡、镇，无下级行政区域。如：110119202-香营乡、110119200-大庄科乡、110119110-井庄镇
        if (!rootCodeStr.contains("000")) {
            return findOne(new District().setCode(rootCode));
        }
        District subTree = null;
        for (District district : districts) {
            Long code = district.getCode();
            if (rootCode == code) {
                return district;
            }
            String prefix = StrUtil.subBefore(String.valueOf(code), "000", false);
            if (prefix.length() % 2 == 0 && rootCodeStr.startsWith(prefix)) {
                subTree = getSubTreeFromDistrictTree(district.getChild(), rootCode);
                break;
            }
        }
        if (Objects.nonNull(subTree)) {
            return subTree;
        }
        // 处理不符合“行政区编号与其上级行政区的编号有关联”规则的行政区域
        District district = getByCode(rootCode);
        Assert.notNull(district, "无效行政区域代码编号");
        return getDistrictTree(rootCode);
    }

    /**
     * 获取全国区域树（优先从缓存读取）
     *
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getWholeCountryDistrictTreePriorityCache() {
        Object cacheList = redisUtil.get(CacheKey.DISTRICT_ALL_TREE);
        if (!Objects.isNull(cacheList)) {
            return JSONObject.parseObject(JSONObject.toJSONString(cacheList), new TypeReference<List<District>>() {
            });
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
    public List<District> getChildDistrictByParentIdPriorityCache(Long parentCode) throws IOException {
        Assert.notNull(parentCode, "行政区域代码编号不能为空");
        String key = String.format(CacheKey.DISTRICT_CHILD_TREE, parentCode);
        Object cacheList = redisUtil.get(key);
        if (!Objects.isNull(cacheList)) {
            return JSONObject.parseObject(JSONObject.toJSONString(cacheList), new TypeReference<List<District>>() {
            });
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
     * 根据层级ID获取指定行政区域的层级位置 - 层级链(从省开始到当前层级)
     *
     * @param districtId 行政区域
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getDistrictPositionDetailById(Integer districtId) {
        District district = getById(districtId);
        if (Objects.isNull(district)) {
            return Collections.emptyList();
        }
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
     * @param districtList    行政区域集
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
    private District getDistrictTree(Long districtCode) {
        return baseMapper.selectDistrictTree(districtCode);
    }


    /**
     * 根据行政区域Id，获取以其作为根节点的行政区区域树
     *
     * @param districtId 行政区域Id
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public District getDistrictTree(Integer districtId) {
        District district = getById(districtId);
        return getDistrictTree(district.getCode());
    }

    /**
     * 根据行政区域Id，获取以其作为根节点的行政区区域树的所有ID
     *
     * @param districtId 行政区域Id
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<Integer> getDistrictTreeAllIds(Integer districtId) {
        District districtTree = getDistrictTree(districtId);
        List<Integer> districtIds = new ArrayList<>();
        districtIds.add(districtTree.getId());
        getAllIds(districtIds, districtTree.getChild());
        return districtIds;
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

    /**
     * 从当前节点出发，向上获取区域名称
     *
     * @param code 当前节点
     * @return 名字
     */
    public String getTopDistrictName(Long code) {
        String key = String.format(CacheKey.DISTRICT_TOP_CN_NAME, code);

        // 先从缓存中取
        String name = (String) redisUtil.get(key);
        if (StringUtils.isNotBlank(name)) {
            return name;
        }

        // 为空，从数据库查询
        String resultName = getName("", code);
        redisUtil.set(key, resultName);
        return resultName;
    }

    /**
     * 获取行政区域
     *
     * @param code code
     * @return 名称
     */
    public String getDistrictName(Long code) {
        String key = String.format(CacheKey.DISTRICT_CN_NAME, code);

        // 先从缓存中取
        String name = (String) redisUtil.get(key);
        if (StringUtils.isNotBlank(name)) {
            return name;
        }
        // 为空，从数据库查询
        District district = baseMapper.selectOne(new QueryWrapper<District>()
                .eq("code", code));
        if (null == district) {
            return "";
        }
        String resultName = district.getName();
        redisUtil.set(key, resultName);
        return resultName;
    }

    /**
     * 循环遍历
     *
     * @param name 名字
     * @param code code
     * @return 名字
     */
    private String getName(String name, Long code) {
        District district = baseMapper
                .selectOne(new QueryWrapper<District>()
                        .eq("code", code));
        if (null == district) {
            return name;
        }
        return getName(district.getName() + name, district.getParentCode());
    }


    /**
     * 通过code拼接详细地址
     *
     * @param provinceCode 省代码
     * @param cityCode     市代码
     * @param areaCode     区代码
     * @param townCode     镇代码
     * @param address      地址
     * @return 全名称
     */
    public String getAddressDetails(Long provinceCode, Long cityCode, Long areaCode, Long townCode, String address) {
        if (null != townCode) {
            return getTopDistrictName(townCode) + "  " + StringUtils.defaultString(address);
        } else if (null != areaCode) {
            return getTopDistrictName(areaCode) + "  " + StringUtils.defaultString(address);
        } else if (null != cityCode) {
            return getTopDistrictName(cityCode) + "  " + StringUtils.defaultString(address);
        } else if (null != provinceCode) {
            return getTopDistrictName(provinceCode) + "  " + StringUtils.defaultString(address);
        }
        return "";
    }

    /**
     * 通过行政id获取行政名称
     *
     * @param ids 行政id
     * @return Map<Integer, String>
     */
    public Map<Integer, String> getByIds(List<Integer> ids) {
        List<District> districts = baseMapper.selectList(new QueryWrapper<District>().in("id", ids));
        return districts.stream()
                .collect(Collectors.toMap(District::getId, District::getName));
    }

    /**
     * 通过名字获取code
     *
     * @param name 行政名字
     * @return code
     */
    public Long getCodeByName(String name) {
        String key = String.format(CacheKey.DISTRICT_CODE, name);

        // 先从缓存中取
        Long code = getLongCode(key, Long.class);
        if (null != code) {
            return code;
        }
        // 为空，从数据库查询
        District district = baseMapper.selectOne(new QueryWrapper<District>()
                .eq("name", name));
        if (null == district) {
            return null;
        }
        Long resultCode = district.getCode();
        redisUtil.set(key, resultCode);
        return resultCode;
    }

    private <T> T getLongCode(String key, Class<T> clazz) {
        Object valueObj = redisUtil.get(key);
        if (clazz.isInstance(valueObj)) {
            return (T) valueObj;
        } else if (clazz == Long.class && valueObj instanceof Integer) {
            Integer obj = (Integer) valueObj;
            return (T) Long.valueOf(obj.longValue());
        }
        return null;
    }

    /**
     * 获取当前登录用户所属省级的行政区树
     *
     * @param currentUser 当前登录用户
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getCurrentUserProvinceTree(CurrentUser currentUser) {
        if (currentUser.isPlatformAdminUser()) {
            return getWholeCountryDistrictTreePriorityCache();
        }
        District district = getNotPlatformAdminUserDistrict(currentUser);
        return Collections.singletonList(getProvinceDistrictTreePriorityCache(district.getCode()));
    }

    /**
     * 通过地区id找到所有下属district
     *
     * @param districtId
     * @return
     */
    public List<District> getChildDistrictByParentIdPriorityCache(Integer districtId) throws IOException {
        District district = getById(districtId);
        return this.getChildDistrictByParentIdPriorityCache(district.getCode());
    }

    /**
     * 获取地区树
     *
     * @param districtTree
     * @param districtIds
     * @return
     */
    public List<District> getDistrictTree(List<District> districtTree, Set<Integer> districtIds) {
        return districtTree.stream().filter(district -> {
                    boolean isContain = districtIds.contains(district.getId());
                    if (isContain) {
                        districtIds.remove(district.getId());
                        this.getDistrictTree(district.getChild(), districtIds);
                    }
                    return isContain;
                }
        ).collect(Collectors.toList());
    }

    /**
     * 获取当前用户地区树 与 districts 的交集
     *
     * @param user
     * @param districtIds
     * @return
     */
    public List<District> getValidDistrictTree(CurrentUser user, Set<Integer> districtIds) throws IOException {
        List<District> districts = new ArrayList<>();
        if (user == null) {
            return districts;
        }
/*

        if (CollectionUtils.isEmpty(districtIds) && !user.isPlatformAdminUser()) {
            District currentDistrict = getNotPlatformAdminUserDistrict(user);
            districts.add(currentDistrict);
            return districts;
        } else if (CollectionUtils.isEmpty(districtIds)) {
            return new ArrayList<>();
        }
*/

        List<District> districtTree = getCurrentUserDistrictTree(user);
        districts = filterDistrictTree(districtTree, districtIds);
        if (user.isPlatformAdminUser()) {
            return districts;
        }
        if (CollectionUtils.isEmpty(districts)) {
            District currentDistrict = getNotPlatformAdminUserDistrict(user);
            districts.add(currentDistrict);
        }
        return districts;
    }

    /**
     * 过滤该地区树没在districts
     *
     * @param districtTree
     * @param districts
     * @return
     */
    private List<District> filterDistrictTree(List<District> districtTree, Set<Integer> districts) {
        if (CollectionUtils.isEmpty(districtTree) || CollectionUtils.isEmpty(districts)) {
            return new ArrayList<>();
        }
        return districtTree.stream().map(district ->
                filterDistrict(district, districts)
        ).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 过滤地区
     *
     * @param district
     * @param districts
     * @return
     */
    private District filterDistrict(District district, Set<Integer> districts) {
        if (district == null || CollectionUtils.isEmpty(districts)) {
            return null;
        }
        boolean isContain = districts.contains(district.getId());
        if (isContain) {
            districts.remove(district.getId());
        }
        district.setChild(filterDistrictTree(district.getChild(), districts));
        if (!isContain && CollectionUtils.isEmpty(district.getChild())) {
            return null;
        }
        return district;
    }

    /**
     * 获取下级的所有地区
     *
     * @return
     * @throws IOException
     */
    public Set<Integer> getChildDistrictIdsByDistrictId(Integer districtId) throws IOException {
        List<District> districts = getChildDistrictByParentIdPriorityCache(districtId);
        Set<Integer> districtIds = districts.stream().map(District::getId).collect(Collectors.toSet());
        return districtIds;
    }

    /**
     * 获取当前code的孩子节点
     *
     * @param code code
     * @return List<District>
     */
    public List<District> getNextDistrictByCode(Long code) {
        return baseMapper.selectChildNodeByParentCode(code);
    }

    /**
     * 获取某个省，所有城市下，所有地区id
     *
     * @param districtId
     * @return
     */
    public Map<District, Set<Integer>> getCityAllDistrictIds(Integer districtId) throws IOException {
        List<District> cityDistrictList = getChildDistrictByParentIdPriorityCache(districtId);
        Map<District, Set<Integer>> districtSetMap = cityDistrictList.stream().collect(Collectors.toMap(Function.identity(), cityDistrict -> {
         return new HashSet<>(getAllDistrictIds(cityDistrict.getId()));
        }));
        return districtSetMap;
    }
}
