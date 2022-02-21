package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.core.screening.organization.service.OverviewService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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




}
