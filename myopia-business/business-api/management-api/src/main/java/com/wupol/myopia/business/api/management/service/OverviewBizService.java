package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.business.core.screening.organization.service.OverviewService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author wulizhou
 * @Date 2022/2/18 12:05
 */
@Service
public class OverviewBizService {

    @Autowired
    private OverviewService overviewService;

    @Autowired
    private OauthServiceClient oauthServiceClient;

    /**
     * 保存总览机构
     *
     * @param overview 总览机构实体类
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized UsernameAndPasswordDTO saveOverview(Overview overview) {
        if (overviewService.checkOverviewName(overview.getName(), null)) {
            throw new BusinessException("总览机构名字重复，请确认");
        }
        // 设置医院状态
        overviewService.save(overview);
        // oauth系统中增加总览机构状态信息
        oauthServiceClient.addOrganization(new Organization(overview.getId(), SystemCode.MANAGEMENT_CLIENT,
                UserType.OVERVIEW, overview.getStatus()));
        return overviewService.generateAccountAndPassword(overview, StringUtils.EMPTY);
        // 生成总览机构-筛查机构，总览机构-医院关系记录

    }


}
