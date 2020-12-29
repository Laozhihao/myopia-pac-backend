package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.UsernameAndPasswordDto;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
public class ScreeningOrganizationService extends BaseService<ScreeningOrganizationMapper, ScreeningOrganization> {

    @Resource
    private GovDeptService govDeptService;

    @Resource
    private ScreeningOrganizationMapper screeningOrganizationMapper;

    @Qualifier("com.wupol.myopia.business.management.client.OauthServiceClient")
    @Autowired
    private OauthServiceClient oauthServiceClient;

    /**
     * 保存筛查机构
     *
     * @param screeningOrganization 筛查机构
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized UsernameAndPasswordDto saveScreeningOrganization(ScreeningOrganization screeningOrganization) {
        if (null == screeningOrganization.getTownCode()) {
            throw new BusinessException("数据异常");
        }
        screeningOrganization.setOrgNo(generateOrgNo(screeningOrganization.getTownCode()));
        baseMapper.insert(screeningOrganization);
        return generateAccountAndPassword(screeningOrganization);
    }

    /**
     * 更新筛查机构
     *
     * @param screeningOrganization 筛查机构实体咧
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateScreeningOrganization(ScreeningOrganization screeningOrganization) {
        return baseMapper.updateById(screeningOrganization);
    }

    /**
     * 删除筛查机构
     *
     * @param id 筛查机构ID
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedById(Integer id) {
        ScreeningOrganization screeningOrganization = new ScreeningOrganization();
        screeningOrganization.setId(id);
        screeningOrganization.setStatus(Const.STATUS_IS_DELETED);
        return baseMapper.updateById(screeningOrganization);
    }

    /**
     * 获取筛查机构列表
     *
     * @param pageRequest 分页
     * @param query       筛查机构列表请求体
     * @param govDeptId   机构id
     * @return IPage<ScreeningOrganization> {@link IPage}
     */
    public IPage<ScreeningOrganization> getScreeningOrganizationList(PageRequest pageRequest, ScreeningOrganizationQuery query, Integer govDeptId) {
        return screeningOrganizationMapper.getScreeningOrganizationListByCondition(
                pageRequest.toPage(), govDeptService.getAllSubordinate(govDeptId),
                query.getName(), query.getType(), query.getOrgNo(), query.getCode());
    }


    /**
     * 生成账号密码
     *
     * @return UsernameAndPasswordDto 账号密码
     */
    private UsernameAndPasswordDto generateAccountAndPassword(ScreeningOrganization screeningOrganization) {
        String password = PasswordGenerator.getScreeningOrgAdminPwd(screeningOrganization.getOrgNo());
        String username = screeningOrganization.getName();

//        UserDTO userDTO = new UserDTO();
//        userDTO.setOrgId(screeningOrganization.getGovDeptId());
//        userDTO.setUsername(username);
//        userDTO.setPassword(password);
//        userDTO.setCreateUserId(screeningOrganization.getCreateUserId());
//        userDTO.setSystemCode(SystemCode.SCREENING_CLIENT.getCode());
//
//        ApiResult apiResult = oauthServiceClient.addUser(userDTO);
//        if (!apiResult.isSuccess()) {
//            throw new BusinessException("创建管理员信息异常");
//        }
//        UserDTO data = (UserDTO) apiResult.getData();

        return new UsernameAndPasswordDto(username, password);
    }

    private String generateOrgNo(Integer code) {
        ScreeningOrganization screeningOrganization = screeningOrganizationMapper.getLastOrgByNo(code);
        if (null == screeningOrganization) {
            return StringUtils.join(code, "201");
        }
        return String.valueOf(Long.parseLong(screeningOrganization.getOrgNo()) + 1);
    }
}
