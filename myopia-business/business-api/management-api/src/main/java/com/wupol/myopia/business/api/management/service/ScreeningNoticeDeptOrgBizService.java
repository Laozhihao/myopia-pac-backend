package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningNoticeVO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ScreeningNoticeDeptOrgBizService {

    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private ScreeningTaskService screeningTaskService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;

    /**
     * 分页查询
     *
     * @param query         查询条件
     * @param pageRequest   分页条件
     * @param currentUser   当前用户
     * @return
     */
    public IPage<ScreeningNoticeVO> getPage(ScreeningNoticeQueryDTO query, PageRequest pageRequest, CurrentUser currentUser) {
        if (StringUtils.isNotBlank(query.getCreatorNameLike())){
            UserDTO userDTO = new UserDTO();
            userDTO.setRealName(query.getCreatorNameLike());
            List<User> list = oauthServiceClient.getUserListByName(userDTO);
            List<Integer> userIdList = list.stream().map(User::getId).collect(Collectors.toList());
            query.setCreateUserIds(userIdList);
        }
        IPage<ScreeningNoticeDTO> screeningNoticePage = screeningNoticeDeptOrgService.selectPageByQuery(pageRequest.toPage(), query);

        IPage<ScreeningNoticeVO> screeningNoticeVOPage = new Page<>(screeningNoticePage.getCurrent(),screeningNoticePage.getSize(),screeningNoticePage.getTotal());

        List<ScreeningNoticeDTO> records = screeningNoticePage.getRecords();
        if (CollUtil.isEmpty(records)){
            return screeningNoticeVOPage;
        }

        // 政府部门名称
        Map<Integer, String> govDeptIdNameMap = getGovDeptIdNameMap(records);

        // 筛查机构和学校
        TwoTuple<Map<Integer, ScreeningOrganization>, Map<Integer, School>> screeningOrgAndSchool = getScreeningOrgAndSchoolMap(records);

        // 创建用户和操作用户名称
        Map<Integer, String> userIdNameMap = getUserIdNameMap(records);

        // 设置地址、所属部门、创建人信息
        List<ScreeningNoticeVO> screeningNoticeVOList = records.stream().map(dto -> buildScreeningNoticeVO(currentUser, govDeptIdNameMap, screeningOrgAndSchool, userIdNameMap, dto)).collect(Collectors.toList());

        screeningNoticeVOPage.setRecords(screeningNoticeVOList);
        return screeningNoticeVOPage;
    }


    /**
     * 构建筛查通知列表结果
     * @param currentUser
     * @param govDeptIdNameMap
     * @param screeningOrgAndSchool
     * @param userIdNameMap
     * @param dto
     */
    private ScreeningNoticeVO buildScreeningNoticeVO(CurrentUser currentUser, Map<Integer, String> govDeptIdNameMap,
                                                     TwoTuple<Map<Integer, ScreeningOrganization>, Map<Integer, School>> screeningOrgAndSchool,
                                                     Map<Integer, String> userIdNameMap, ScreeningNoticeDTO dto) {
        ScreeningNoticeVO vo = new ScreeningNoticeVO(dto);
        List<District> districtPositionDetailById = districtService.getDistrictPositionDetailById(vo.getDistrictId());
        vo.setDistrictDetail(districtPositionDetailById)
                .setDistrictName(districtService.getDistrictNameByDistrictPositionDetail(districtPositionDetailById))
                .setCreatorName(userIdNameMap.getOrDefault(vo.getCreateUserId(), StringUtils.EMPTY));
        // 转换type，方便前端展示处理（常见病版本中，“发布筛查通知”和“筛查通知”菜单合并）
        if (ScreeningNotice.TYPE_GOV_DEPT.equals(vo.getType()) && vo.getGovDeptId().equals(vo.getAcceptOrgId())) {
            vo.setType(ScreeningNotice.TYPE_GOV_DEPT_SELF_RELEASE);
        }
        // 政府部门名称
        if (ScreeningNotice.TYPE_GOV_DEPT.equals(vo.getType()) || ScreeningNotice.TYPE_GOV_DEPT_SELF_RELEASE.equals(vo.getType())) {
            vo.setGovDeptName(govDeptIdNameMap.getOrDefault(vo.getAcceptOrgId(), StringUtils.EMPTY));
        }

        // 筛查机构名称
        if (Objects.equals(ScreeningNotice.TYPE_ORG,vo.getType())) {
            ScreeningOrganization screeningOrganization = screeningOrgAndSchool.getFirst().get(vo.getAcceptOrgId());
            vo.setScreeningOrgName(getScreeningOrgName(screeningOrganization,ScreeningOrganization::getName));
            vo.setCanCreatePlan(getCanCreatePlan(screeningOrganization,ScreeningOrganization::getScreeningTypeConfig,dto.getScreeningType()));
        }

        //学校
        if (Objects.equals(ScreeningNotice.TYPE_SCHOOL,vo.getType())) {
            School school = screeningOrgAndSchool.getSecond().get(vo.getAcceptOrgId());
            vo.setScreeningOrgName(getScreeningOrgName(school,School::getName));
            vo.setCanCreatePlan(getCanCreatePlan(school,School::getScreeningTypeConfig,dto.getScreeningType()));
        }


        // 平台管理员：看到所有通知的发布人、政府部门：仅看到自己创建的通知的发布人、筛查机构：看不到发布人
        if (currentUser.isPlatformAdminUser() || (currentUser.isGovDeptUser() && ScreeningNotice.TYPE_GOV_DEPT_SELF_RELEASE.equals(vo.getType()))) {
            vo.setReleaserName(userIdNameMap.getOrDefault(vo.getOperatorId(), StringUtils.EMPTY));
        }
        return vo;
    }


    /**
     * 获取筛查机构名
     * @param entity
     * @param function
     */
    private <T>String getScreeningOrgName(T entity ,Function<T,String> function){
        return Optional.ofNullable(entity).map(function).orElse(StringUtils.EMPTY);
    }

    /**
     * 判断是否能创建计划
     * @param entity
     * @param function
     * @param screeningType
     */
    private <T>Boolean getCanCreatePlan(T entity,Function<T,String> function,Integer screeningType){
        return Optional.ofNullable(entity)
                .map(function)
                .filter(StringUtils::isNotBlank)
                .filter(x -> x.contains(String.valueOf(screeningType))).isPresent();
    }


    /**
     * 获取用户信息
     * @param screeningNoticeDTOList
     */
    private Map<Integer, String>  getUserIdNameMap(List<ScreeningNoticeDTO> screeningNoticeDTOList) {
        Set<Integer> userIds = screeningNoticeDTOList.stream().map(ScreeningNotice::getCreateUserId).collect(Collectors.toSet());
        Set<Integer> operatorUserIds = screeningNoticeDTOList.stream().map(ScreeningNotice::getOperatorId).collect(Collectors.toSet());
        userIds.addAll(operatorUserIds);
        if (CollUtil.isEmpty(userIds)){
            return Maps.newHashMap();
        }
        List<User> userList = oauthServiceClient.getUserBatchByIds(Lists.newArrayList(userIds));
        if (CollUtil.isEmpty(userList)){
            return Maps.newHashMap();
        }
        return userList.stream().collect(Collectors.toMap(User::getId, User::getRealName, (x, y) -> y));
    }

    /**
     * 政府部门信息
     * @param screeningNoticeDTOList
     */
    private Map<Integer, String> getGovDeptIdNameMap(List<ScreeningNoticeDTO> screeningNoticeDTOList){
        Set<Integer> allGovDeptIds = screeningNoticeDTOList.stream()
                .filter(vo -> ScreeningNotice.TYPE_GOV_DEPT.equals(vo.getType()))
                .map(ScreeningNoticeDTO::getAcceptOrgId)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(allGovDeptIds)){
            return Maps.newHashMap();
        }
        List<GovDept> govDeptList = govDeptService.getByIds(Lists.newArrayList(allGovDeptIds));
        if (CollUtil.isEmpty(govDeptList)){
            return Maps.newHashMap();
        }
        return govDeptList.stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
    }

    /**
     * 获取筛查机构和学校信息
     * @param screeningNoticeDTOList
     */
    private TwoTuple<Map<Integer, ScreeningOrganization>,Map<Integer, School>> getScreeningOrgAndSchoolMap(List<ScreeningNoticeDTO> screeningNoticeDTOList){
        TwoTuple<Map<Integer, ScreeningOrganization>,Map<Integer, School>> tuple = TwoTuple.of(Maps.newHashMap(),Maps.newHashMap());

        // 机构
        Set<Integer> allScreeningOrgIds = screeningNoticeDTOList.stream()
                .filter(vo -> ScreeningNotice.TYPE_ORG.equals(vo.getType()))
                .map(ScreeningNoticeDTO::getAcceptOrgId)
                .collect(Collectors.toSet());
        List<ScreeningOrganization> screeningOrganizationList = null;
        if (CollUtil.isNotEmpty(allScreeningOrgIds)){
            screeningOrganizationList = screeningOrganizationService.getByIds(allScreeningOrgIds);
        }
        if (!CollectionUtils.isEmpty(screeningOrganizationList)){
            Map<Integer, ScreeningOrganization> screeningOrgMap = screeningOrganizationList.stream().collect(Collectors.toMap(ScreeningOrganization::getId, Function.identity()));
            tuple.setFirst(screeningOrgMap);
        }

        // 学校
        Set<Integer> allScreeningSchoolIds = screeningNoticeDTOList.stream()
                .filter(vo -> ScreeningNotice.TYPE_SCHOOL.equals(vo.getType()))
                .map(ScreeningNoticeDTO::getAcceptOrgId)
                .collect(Collectors.toSet());
        List<School> schoolList = null;
        if (CollUtil.isNotEmpty(allScreeningSchoolIds)){
            schoolList = schoolService.getByIds(Lists.newArrayList(allScreeningSchoolIds));
        }
        if (!CollectionUtils.isEmpty(schoolList)){
            Map<Integer, School> screeningSchoolMap = schoolList.stream().collect(Collectors.toMap(School::getId, Function.identity()));
            tuple.setSecond(screeningSchoolMap);
        }

        return tuple;
    }

    public List<ScreeningNoticeDTO> getCanLinkNotice(Integer screeningOrgId) {
        List<ScreeningNoticeDTO> notices = screeningNoticeDeptOrgService.getCanLinkNotice(screeningOrgId);

//        // 通过TaskId查询最原始的通知
//        List<ScreeningTask> tasks = screeningTaskService.listByIds(notices.stream().map(ScreeningNotice::getScreeningTaskId).collect(Collectors.toList()));
//        Map<Integer, Integer> taskMap = tasks.stream().collect(Collectors.toMap(ScreeningTask::getId, ScreeningTask::getScreeningNoticeId));
//
//        List<ScreeningNotice> sourceNotices = screeningNoticeService.getByIds(tasks.stream().map(ScreeningTask::getScreeningNoticeId).collect(Collectors.toList()));
//        Map<Integer, Integer> sourceNoticeMap = sourceNotices.stream().collect(Collectors.toMap(ScreeningNotice::getId, ScreeningNotice::getGovDeptId));
//
//        // 获取行政部门
//        List<GovDept> govDeptList = govDeptService.getByIds(Lists.newArrayList(sourceNotices.stream().map(ScreeningNotice::getGovDeptId).collect(Collectors.toList())));
//        Map<Integer, String> govDeptMap = govDeptList.stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
//        notices.forEach(notice-> notice.setGovDeptName(govDeptMap.get(sourceNoticeMap.get(taskMap.get(notice.getScreeningTaskId())))));
        return notices;
    }
}
