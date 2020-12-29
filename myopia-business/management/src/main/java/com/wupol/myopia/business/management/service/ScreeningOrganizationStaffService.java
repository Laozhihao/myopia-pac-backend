package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.domain.dto.OrganizationStaffRequest;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.UsernameAndPasswordDto;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationStaffMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationStaffQuery;
import com.wupol.myopia.business.management.util.TwoTuple;
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
public class ScreeningOrganizationStaffService extends BaseService<ScreeningOrganizationStaffMapper, ScreeningOrganizationStaff> {

    @Resource
    private GovDeptService govDeptService;

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    @Qualifier("com.wupol.myopia.business.management.client.OauthServiceClient")
    @Autowired
    private OauthServiceClient oauthServiceClient;

    /**
     * 获取机构人员列表
     *
     * @param request   请求入参
     * @param govDeptId 部门id
     * @return Page<ScreeningOrganizationStaff> {@link Page}
     */
    public Page<ScreeningOrganizationStaff> getOrganizationStaffList(OrganizationStaffRequest request, Integer govDeptId) {

        Page<ScreeningOrganizationStaff> page = new Page<>(request.getCurrent(), request.getSize());
        QueryWrapper<ScreeningOrganizationStaff> wrapper = new QueryWrapper<>();

        likeQueryAppend(wrapper, "screening_org_id", request.getScreeningOrgId());
        InQueryAppend(wrapper, "gov_dept_id", govDeptService.getAllSubordinate(govDeptId));

        if (StringUtils.isNotBlank(request.getName())) {

        }
        if (null != request.getIdCard()) {

        }
        if (null != request.getIdCard()) {

        }
        if (StringUtils.isNotBlank(request.getMobile())) {

        }
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 删除用户
     *
     * @param id           id
     * @param createUserId 创建人
     * @return 删除个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedOrganizationStaff(Integer id, Integer createUserId) {
        // TODO: 删除用户
        return 1;
    }

    /**
     * 新增员工
     *
     * @param staffQuery 员工实体类
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized UsernameAndPasswordDto saveOrganizationStaff(ScreeningOrganizationStaffQuery staffQuery) {

        // 生成账号密码
        TwoTuple<UsernameAndPasswordDto, Integer> tuple = generateAccountAndPassword(staffQuery);
        // 通过screeningOrgId获取机构
        ScreeningOrganization organization = screeningOrganizationService.getById(staffQuery.getScreeningOrgId());
        staffQuery.setStaffNo(generateOrgNo(organization.getOrgNo(), staffQuery.getIdCard()));
        staffQuery.setUserId(tuple.getSecond());

        baseMapper.insert(staffQuery);
        return tuple.getFirst();
    }

    /**
     * 更新员工
     *
     * @param screeningOrganizationStaff 员工实体类
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateOrganizationStaff(ScreeningOrganizationStaff screeningOrganizationStaff) {
        return baseMapper.updateById(screeningOrganizationStaff);
    }

    /**
     * 生成账号密码
     *
     * @return TwoTuple<UsernameAndPasswordDto, Integer> 账号密码,Id
     */
    private TwoTuple<UsernameAndPasswordDto, Integer> generateAccountAndPassword(ScreeningOrganizationStaffQuery staff) {
        TwoTuple<UsernameAndPasswordDto, Integer> tuple = new TwoTuple<>();

        String password = PasswordGenerator.getScreeningUserPwd(staff.getPhone(), staff.getIdCard());
        String username = staff.getPhone();
        tuple.setFirst(new UsernameAndPasswordDto(username, password));

        UserDTO userDTO = new UserDTO();
        userDTO.setOrgId(staff.getGovDeptId());
        userDTO.setUsername(username);
        userDTO.setPassword(password);
        userDTO.setCreateUserId(staff.getCreateUserId());
        userDTO.setSystemCode(SystemCode.SCREENING_CLIENT.getCode());

        ApiResult apiResult = oauthServiceClient.addUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("创建管理员信息异常");
        }
        UserDTO data = (UserDTO) apiResult.getData();
        tuple.setSecond(data.getId());
        return tuple;
    }

    /**
     * 生成人员编号
     *
     * @param orgNo  筛查机构编号
     * @param idCard 身份证
     * @return String 编号
     */
    private String generateOrgNo(String orgNo, String idCard) {
        return StringUtils.join(orgNo, StringUtils.right(idCard, 6));
    }
}
