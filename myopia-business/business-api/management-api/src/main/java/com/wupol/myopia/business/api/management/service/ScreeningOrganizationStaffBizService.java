package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 筛查人员
 *
 * @author Simple4H
 */
@Service
public class ScreeningOrganizationStaffBizService {

    @Autowired
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;

    /**
     * 更新机构人员的id
     *
     * @param currentUser  当前用户
     * @param resourceFile 资源文件
     */
    public void updateOrganizationStaffSignId(CurrentUser currentUser, ResourceFile resourceFile) {
        ScreeningOrganizationStaff screeningOrganizationStaff = new ScreeningOrganizationStaff();
        screeningOrganizationStaff.setScreeningOrgId(currentUser.getOrgId()).setUserId(currentUser.getId());
        List<ScreeningOrganizationStaff> screeningOrganizationStaffs = screeningOrganizationStaffService.getByEntity(screeningOrganizationStaff);
        if (CollectionUtils.isNotEmpty(screeningOrganizationStaffs)) {
            screeningOrganizationStaff = screeningOrganizationStaffs.stream().findFirst().get();
        }
        if (screeningOrganizationStaff.getId() != null) {
            screeningOrganizationStaff.setSignFileId(resourceFile.getId());
            screeningOrganizationStaffService.updateById(screeningOrganizationStaff);
        }
    }
}
