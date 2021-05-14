package com.wupol.myopia.business.aggregation.export.excel;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.business.aggregation.export.pdf.constant.FileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationStaffExportDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 导出机构下的筛查人员
 *
 * @author Simple4H
 */
@Service("screeningOrganizationStaffExcelService")
public class ExportScreeningOrganizationStaffExcelService extends BaseExportExcelFileService {

    @Resource
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    @Resource
    private OauthServiceClient oauthServiceClient;

    @Override
    public List getExcelData(ExportCondition exportCondition) {

        Integer screeningOrgId = exportCondition.getScreeningOrgId();
        String orgName = screeningOrganizationService.getById(screeningOrgId).getName();

        List<ScreeningOrganizationStaff> staffLists = screeningOrganizationStaffService.getByOrgId(screeningOrgId);
        UserDTO userQuery = new UserDTO();
        userQuery.setSize(staffLists.size())
                .setCurrent(1)
                .setOrgId(screeningOrgId)
                .setSystemCode(SystemCode.SCREENING_CLIENT.getCode());
        Page<User> userPage = oauthServiceClient.getUserListPage(userQuery);
        List<User> userList = JSON.parseArray(JSON.toJSONString(userPage.getRecords()), User.class);

        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }

        // 构建数据
        return userList.stream()
                .map(item -> new ScreeningOrganizationStaffExportDTO()
                        .setName(item.getRealName())
                        .setGender(GenderEnum.getName(item.getGender()))
                        .setPhone(item.getPhone())
                        .setIdCard(item.getIdCard())
                        .setOrganization(orgName)).collect(Collectors.toList());
    }

    @Override
    public Class getHeadClass() {
        return ScreeningOrganizationStaffExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        String orgName = screeningOrganizationService.getById(exportCondition.getScreeningOrgId()).getName();
        return String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS,
                orgName + FileNameConstant.STAFF_EXCEL_FILE_NAME,
                new Date());
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("筛查机构人员");
        String orgName = screeningOrganizationService.getById(exportCondition.getScreeningOrgId()).getName();
        builder.append("-").append(orgName);
        return builder.toString();
    }
}
