package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.mapper.ScreeningNoticeMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningNoticeDeptOrg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class ScreeningNoticeService extends BaseService<ScreeningNoticeMapper, ScreeningNotice> {
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private DistrictService districtService;

    /**
     * 发布通知
     * @param id
     * @return
     */
    public Boolean release(Integer id) {
        //1. 更新状态&发布时间
        if (baseMapper.release(id) > 0) {
            List<ScreeningNoticeDeptOrg> screeningNoticeDeptOrgs =
                    districtService.getCurrentUserDistrictTree(CurrentUserUtil.getCurrentUser())
                            .stream()
                            .map(district
                                    -> new ScreeningNoticeDeptOrg()
                                               .setScreeningNoticeId(id)
                                               .setDistrictId(district.getId()))
                            .collect(Collectors.toList());
            //2. 为下属部门创建通知
            return screeningNoticeDeptOrgService.saveBatch(screeningNoticeDeptOrgs);
        }
        return false;
    }
}
