package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningNoticeVO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        IPage<ScreeningNoticeDTO> screeningNoticeIPage = screeningNoticeDeptOrgService.selectPageByQuery(pageRequest.toPage(), query);
        // 政府部门名称
        List<Integer> allGovDeptIds = screeningNoticeIPage.getRecords().stream().filter(vo -> ScreeningNotice.TYPE_GOV_DEPT.equals(vo.getType())).map(ScreeningNoticeDTO::getAcceptOrgId).distinct().collect(Collectors.toList());
        Map<Integer, String> govDeptIdNameMap = CollectionUtils.isEmpty(allGovDeptIds) ? Collections.emptyMap() : govDeptService.getByIds(allGovDeptIds).stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        // 筛查机构名称
        List<Integer> allScreeningOrgIds = screeningNoticeIPage.getRecords().stream().filter(vo -> ScreeningNotice.TYPE_ORG.equals(vo.getType())).map(ScreeningNoticeDTO::getAcceptOrgId).distinct().collect(Collectors.toList());
        Map<Integer, ScreeningOrganization> screeningOrgMap = CollectionUtils.isEmpty(allScreeningOrgIds) ? Collections.emptyMap() : screeningOrganizationService.getByIds(allScreeningOrgIds).stream().collect(Collectors.toMap(ScreeningOrganization::getId, Function.identity()));
        // 创建用户和操作用户名称
        List<Integer> userIds = screeningNoticeIPage.getRecords().stream().map(ScreeningNotice::getCreateUserId).distinct().collect(Collectors.toList());
        List<Integer> operatorUserIds = screeningNoticeIPage.getRecords().stream().map(ScreeningNotice::getOperatorId).distinct().collect(Collectors.toList());
        userIds.addAll(operatorUserIds);
        Map<Integer, String> userIdNameMap = oauthServiceClient.getUserBatchByIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getRealName, (x, y) -> y));

        // 设置地址、所属部门、创建人信息
        return screeningNoticeIPage.convert(dto -> {
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
            if (ScreeningNotice.TYPE_ORG.equals(vo.getType())) {
                ScreeningOrganization screeningOrganization = screeningOrgMap.get(vo.getAcceptOrgId());
                vo.setScreeningOrgName(Optional.ofNullable(screeningOrganization).map(ScreeningOrganization::getName).orElse(StringUtils.EMPTY));
                vo.setCanCreatePlan(Optional.ofNullable(screeningOrganization).map(x -> StringUtils.isNotBlank(x.getScreeningTypeConfig()) && x.getScreeningTypeConfig().contains(String.valueOf(dto.getScreeningType()))).orElse(Boolean.FALSE));
            }
            // 平台管理员：看到所有通知的发布人、政府部门：仅看到自己创建的通知的发布人、筛查机构：看不到发布人
            if (currentUser.isPlatformAdminUser() || (currentUser.isGovDeptUser() && ScreeningNotice.TYPE_GOV_DEPT_SELF_RELEASE.equals(vo.getType()))) {
                vo.setReleaserName(userIdNameMap.getOrDefault(vo.getOperatorId(), StringUtils.EMPTY));
            }
            return vo;
        });
    }

}
