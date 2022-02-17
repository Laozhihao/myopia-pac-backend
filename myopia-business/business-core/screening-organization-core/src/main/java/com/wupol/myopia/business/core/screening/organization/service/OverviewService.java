package com.wupol.myopia.business.core.screening.organization.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.organization.domain.mapper.OverviewMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import org.springframework.stereotype.Service;

/**
 * @Author wulizhou
 * @Date 2022-02-17
 */
@Service
public class OverviewService extends BaseService<OverviewMapper, Overview> {


    /**
     * 检验总览机构合作信息是否合法
     * @param overview
     */
    public void checkOverviewCooperation(Overview overview)  {
        if (!overview.checkCooperation()) {
            throw new BusinessException("合作信息非法，请确认");
        }
    }

}
