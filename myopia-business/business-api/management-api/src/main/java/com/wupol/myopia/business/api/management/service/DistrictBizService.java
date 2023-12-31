package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.OverviewService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author HaoHao
 * @Date 2021/4/26
 **/
@Service
public class DistrictBizService {

    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private OverviewService overviewService;

    /**
     * 获取非平台管理员用户的行政区域
     *
     * @param currentUser 当前登录用户
     * @return com.wupol.myopia.business.management.domain.model.District
     **/
    public District getNotPlatformAdminUserDistrict(CurrentUser currentUser) {
        if (currentUser.isGovDeptUser()) {
            GovDept govDept = govDeptService.getById(currentUser.getOrgId());
            return districtService.getById(govDept.getDistrictId());
        }
        if (currentUser.isScreeningUser() || (currentUser.isHospitalUser() && (Objects.nonNull(currentUser.getScreeningOrgId())))) {
            ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(currentUser.getScreeningOrgId());
            return districtService.getById(screeningOrganization.getDistrictId());
        }
        // 总览机构
        if (currentUser.isOverviewUser()) {
            Overview overview = overviewService.getById(currentUser.getOrgId());
            return districtService.getById(overview.getDistrictId());
        }
        throw new BusinessException("无效用户类型");
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
        // 平台管理员|总览机构行政区域的筛选条件
        if (currentUser.isPlatformAdminUser() || currentUser.isOverviewUser()) {
            return districtId;
        }
        // 非平台管理员只能看到自己同级行政区域
        return getNotPlatformAdminUserDistrict(currentUser).getId();
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
        return districtService.getDistrictPositionDetail(district.getCode());
    }

    /**
     * 获取当前登录用户所属省级的行政区树
     *
     * @param currentUser 当前登录用户
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getCurrentUserProvinceTree(CurrentUser currentUser) {
        if (currentUser.isPlatformAdminUser()) {
            return districtService.getWholeCountryDistrictTreePriorityCache();
        }
        District district = getNotPlatformAdminUserDistrict(currentUser);
        return Collections.singletonList(districtService.getProvinceDistrictTreePriorityCache(district.getCode()));
    }


    /**
     * 获取当前用户地区树 与 districts 的交集
     *
     * @param user        当前用户
     * @param districtIds 待求交集的行政区域ID集
     * @return
     */
    public List<District> getValidDistrictTree(CurrentUser user, Set<Integer> districtIds) {
        List<District> districts = new ArrayList<>();
        if (user == null) {
            return districts;
        }

        List<District> districtTree = getCurrentUserProvinceTree(user);
        return districtService.filterDistrictTree(districtTree, districtIds);

    }

    /**
     * 获取当前用户地区树 与 districts 的交集
     *
     * @param user        当前用户
     * @param districtIds 待求交集的行政区域ID集
     * @return List<District>
     */
    public List<District> getChildDistrictValidDistrictTree(CurrentUser user, Set<Integer> districtIds) {
        List<District> districts = new ArrayList<>();
        if (user == null) {
            return districts;
        }
        // 当前用户的行政区域
        District district = getNotPlatformAdminUserDistrict(user);
        // 获取下属的行政区域
        List<District> childDistrictTree = districtService.getSpecificDistrictTreePriorityCache(district.getCode());
        // 过滤该地区树没在districts
        return districtService.filterDistrictTree(childDistrictTree, districtIds);
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
            return districtService.getWholeCountryDistrictTreePriorityCache();
        }
        // 非平台管理员，获取以其所属行政区域为根节点的行政区域树
        District parentDistrict = getNotPlatformAdminUserDistrict(currentUser);
        return districtService.getSpecificDistrictTreePriorityCache(parentDistrict.getCode());
    }


    /**
     * 获取以当前登录用户所属行政区域为根节点的行政区域树所有ID
     *
     * @param currentUser 当前登录用户
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<Integer> getCurrentUserDistrictTreeAllIds(CurrentUser currentUser) {
        List<District> districtTrees = getCurrentUserDistrictTree(currentUser);
        List<Integer> districtIds = new ArrayList<>();
        districtService.getAllIds(districtIds, districtTrees);
        return districtIds;
    }

}
