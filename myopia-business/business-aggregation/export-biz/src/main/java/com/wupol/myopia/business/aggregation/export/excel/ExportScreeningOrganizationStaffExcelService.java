package com.wupol.myopia.business.aggregation.export.excel;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
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
        return String.format(ExcelNoticeKeyContentConstant.STAFF_EXCEL_NOTICE_KEY_CONTENT, orgName);
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        // 设置文件名
        StringBuilder builder = new StringBuilder().append(ExcelFileNameConstant.STAFF_FILE_NAME);
        String orgName = screeningOrganizationService.getById(exportCondition.getScreeningOrgId()).getName();
        builder.append(orgName);
        return builder.toString();
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        // 不需要校验
    }

    @Override
    public String getRedisKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_EXCEL_ORG_STAFF,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getScreeningOrgId());
    }
}
