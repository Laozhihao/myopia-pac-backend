package com.wupol.myopia.business.aggregation.export.excel.imports;

import cn.hutool.core.util.IdcardUtil;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.PasswordAndUsernameGenerator;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.aggregation.export.excel.domain.StaffImportEnum;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationStaffDTO;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 筛查人员导入
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class ScreeningOrgStaffExcelImportService {

    @Resource
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;

    @Resource
    private OauthServiceClient oauthServiceClient;

    /**
     * 导入机构人员
     *
     * @param currentUser    当前登录用户
     * @param multipartFile  导入文件
     * @param screeningOrgId 筛查机构id
     * @throws BusinessException io异常
     */
    public void importScreeningOrganizationStaff(CurrentUser currentUser, MultipartFile multipartFile, Integer screeningOrgId) {
        if (null == screeningOrgId) {
            throw new BusinessException("机构ID不能为空");
        }

        List<Map<Integer, String>> listMap = FileUtils.readExcel(multipartFile);

        preCheckStaff(screeningOrgId, listMap);
        // excel格式：序号	姓名	性别	身份证号	手机号码	说明
        List<UserDTO> userList = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            if (StringUtils.isBlank(item.get(StaffImportEnum.NAME.getIndex()))) {
                break;
            }
            checkStaffInfo(item);
            UserDTO userDTO = new UserDTO();
            userDTO.setRealName(item.get(StaffImportEnum.NAME.getIndex()))
                    .setGender(GenderEnum.getType(item.get(StaffImportEnum.GENDER.getIndex())))
                    .setIdCard(item.get(StaffImportEnum.ID_CARD.getIndex()))
                    .setPhone(item.get(StaffImportEnum.PHONE.getIndex()))
                    .setCreateUserId(currentUser.getId())
                    .setIsLeader(0)
                    .setPassword(PasswordAndUsernameGenerator.getScreeningUserPwd(item.get(StaffImportEnum.PHONE.getIndex()), item.get(StaffImportEnum.ID_CARD.getIndex())))
                    .setUsername(item.get(StaffImportEnum.PHONE.getIndex()))
                    .setOrgId(screeningOrgId).setSystemCode(SystemCode.SCREENING_CLIENT.getCode());
            if (null != item.get(StaffImportEnum.REMARK.getIndex())) {
                userDTO.setRemark(item.get(StaffImportEnum.REMARK.getIndex()));
            }
            userList.add(userDTO);
        }
        List<ScreeningOrganizationStaffDTO> importList = userList.stream().map(item -> {
            ScreeningOrganizationStaffDTO staff = new ScreeningOrganizationStaffDTO();
            staff.setIdCard(item.getIdCard())
                    .setScreeningOrgId(item.getOrgId())
                    .setCreateUserId(item.getCreateUserId())
                    .setRemark(item.getRemark())
                    .setGovDeptId(currentUser.getOrgId());
            return staff;
        }).collect(Collectors.toList());

        // 批量新增OAuth2
        List<User> users = oauthServiceClient.addScreeningUserBatch(userList);
        Map<String, Integer> userMaps = users.stream().collect(Collectors.toMap(User::getIdCard, User::getId));
        // 设置userId
        importList.forEach(i -> i.setUserId(userMaps.get(i.getIdCard())));
        screeningOrganizationStaffService.saveBatch(importList);
    }

    /**
     * 筛查人员前置校验
     *
     * @param screeningOrgId 筛查机构ID
     * @param listMap        筛查人员
     */
    private void preCheckStaff(Integer screeningOrgId, List<Map<Integer, String>> listMap) {
        // 收集身份证号码
        List<String> idCards = listMap.stream().map(s -> s.get(StaffImportEnum.ID_CARD.getIndex())).collect(Collectors.toList());
        if (idCards.stream().distinct().count() < idCards.size()) {
            throw new BusinessException("身份证号码重复");
        }
        List<User> checkIdCards = oauthServiceClient.getUserBatchByIdCards(idCards, SystemCode.SCREENING_CLIENT.getCode(), screeningOrgId);
        Assert.isTrue(CollectionUtils.isEmpty(checkIdCards), "身份证号码已经被使用，请确认！");

        // 收集手机号码
        List<String> phones = listMap.stream().map(s -> s.get(3)).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        Assert.isTrue(phones.size() == phones.stream().distinct().count(), "手机号码重复");
        List<User> checkPhones = oauthServiceClient.getUserBatchByPhones(phones, SystemCode.SCREENING_CLIENT.getCode());
        Assert.isTrue(CollectionUtils.isEmpty(checkPhones), "手机号码已经被使用，请确认！");
    }

    /**
     * 检查筛查人员信息
     *
     * @param item 筛查人员
     */
    private void checkStaffInfo(Map<Integer, String> item) {
        Assert.isTrue(StringUtils.isNotBlank(item.get(1)) && !GenderEnum.getType(item.get(1)).equals(GenderEnum.UNKNOWN.type), "性别异常");
        Assert.isTrue(StringUtils.isNotBlank(item.get(2)) && IdcardUtil.isValidCard(item.get(2)), "身份证异常");
        Assert.isTrue(StringUtils.isNotBlank(item.get(3)) && Pattern.matches(RegularUtils.REGULAR_MOBILE, item.get(3)), "手机号码异常");
    }
}
