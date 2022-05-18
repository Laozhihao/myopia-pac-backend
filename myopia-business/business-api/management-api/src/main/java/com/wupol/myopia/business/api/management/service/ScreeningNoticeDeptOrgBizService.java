package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningNoticeVO;
import com.wupol.myopia.business.api.management.domain.vo.UserVO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
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

    /**
     * 分页查询
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningNoticeVO> getPage(ScreeningNoticeQueryDTO query, PageRequest pageRequest, CurrentUser user) {
        Page<ScreeningNotice> page = (Page<ScreeningNotice>) pageRequest.toPage();
        if (StringUtils.isNotBlank(query.getCreatorNameLike())){
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(query.getCreatorNameLike());
            List<User> list = oauthServiceClient.getUserListByName(userDTO);
            List<Integer> userIdList = list.stream().map(item -> item.getId()).collect(Collectors.toList());
            query.setCreateUserIds(userIdList);
        }
        IPage<ScreeningNoticeDTO> screeningNoticeIPage = screeningNoticeDeptOrgService.selectPageByQuery(page, query);
        List<Integer> allGovDeptIds = screeningNoticeIPage.getRecords().stream().filter(vo -> ScreeningNotice.TYPE_GOV_DEPT.equals(vo.getType())).map(ScreeningNoticeDTO::getAcceptOrgId).distinct().collect(Collectors.toList());
        Map<Integer, String> govDeptIdNameMap = CollectionUtils.isEmpty(allGovDeptIds) ? Collections.emptyMap() : govDeptService.getByIds(allGovDeptIds).stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        List<Integer> userIds = screeningNoticeIPage.getRecords().stream().map(ScreeningNotice::getCreateUserId).distinct().collect(Collectors.toList());
        Map<Integer, String> userIdNameMap = oauthServiceClient.getUserBatchByIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getRealName));

        // 设置地址信息
        return screeningNoticeIPage.convert(dto -> {
            ScreeningNoticeVO vo = new ScreeningNoticeVO(dto);
            List<District> districtPositionDetailById = districtService.getDistrictPositionDetailById(vo.getDistrictId());
            vo.setDistrictDetail(districtPositionDetailById).setDistrictName(districtService.getDistrictNameByDistrictPositionDetail(districtPositionDetailById)).setCreatorName(userIdNameMap.getOrDefault(vo.getCreateUserId(), ""));
            if (ScreeningNotice.TYPE_GOV_DEPT.equals(vo.getType())) {
                vo.setGovDeptName(govDeptIdNameMap.getOrDefault(vo.getAcceptOrgId(), ""));
            }
            //判断是否为当前用户创建的通知
            if (user.getId().equals(vo.getCreateUserId())){
                vo.setIsSelfRelease(ScreeningNotice.IS_SELF_RELEASE);
            }else{
                vo.setIsSelfRelease(ScreeningNotice.IS_NOT_SELF_RELEASE);
            }
            return vo;
        });
    }

}
