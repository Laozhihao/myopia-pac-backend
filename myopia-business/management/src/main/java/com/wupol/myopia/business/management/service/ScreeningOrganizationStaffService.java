package com.wupol.myopia.business.management.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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
import com.wupol.myopia.business.management.domain.dto.UserExtDTO;
import com.wupol.myopia.business.management.domain.dto.UsernameAndPasswordDTO;
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
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
public class ScreeningOrganizationStaffService extends BaseService<ScreeningOrganizationStaffMapper, ScreeningOrganizationStaff> {

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    @Qualifier("com.wupol.myopia.business.management.client.OauthServiceClient")
    @Autowired
    private OauthServiceClient oauthServiceClient;

    /**
     * 获取机构人员列表
     *
     * @param request 请求入参
     * @return Page<UserExtDTO> {@link Page}
     */
    public Page<UserExtDTO> getOrganizationStaffList(OrganizationStaffRequest request) {

        ApiResult apiResult = oauthServiceClient.getUserListPage(
                new UserDTO()
                        .setCurrent(request.getCurrent())
                        .setSize(request.getSize())
//                        .setOrgId(request.getScreeningOrgId())
                        .setRealName(request.getName())
                        .setIdCard(request.getIdCard())
                        .setPhone(request.getMobile()));
        if (apiResult.isSuccess()) {
            Page<UserExtDTO> page = JSONObject.parseObject(JSONObject.toJSONString(apiResult.getData()), new TypeReference<Page<UserExtDTO>>() {
            });
            List<UserExtDTO> resultLists = page.getRecords();

            if (!CollectionUtils.isEmpty(resultLists)) {
                List<Integer> userIds = resultLists.stream().map(UserExtDTO::getId).collect(Collectors.toList());
                Map<Integer, String> staffSnMaps = baseMapper
                        .selectList(new QueryWrapper<ScreeningOrganizationStaff>()
                                .in("user_id", userIds))
                        .stream()
                        .collect(Collectors.toMap(ScreeningOrganizationStaff::getUserId,
                                ScreeningOrganizationStaff::getStaffNo));

                resultLists.forEach(s -> {
                    s.setSn(staffSnMaps.get(s.getId()));
                });
                return page;
            }
        }
        return null;
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
    public synchronized UsernameAndPasswordDTO saveOrganizationStaff(ScreeningOrganizationStaffQuery staffQuery) {

        // 生成账号密码
        TwoTuple<UsernameAndPasswordDTO, Integer> tuple = generateAccountAndPassword(staffQuery);
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
     * @param staff 员工实体类
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateOrganizationStaff(ScreeningOrganizationStaffQuery staff) {
        UserDTO userDTO = new UserDTO()
                .setId(staff.getUserId())
                .setRealName(staff.getName())
                .setGender(staff.getGender())
                .setPhone(staff.getPhone())
                .setIdCard(staff.getIdCard())
                .setRemark(staff.getRemark());
        ApiResult<UserDTO> apiResult = oauthServiceClient.addUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("OAuth2 异常");
        }
        return baseMapper.updateById(staff);
    }

    /**
     * 生成账号密码
     *
     * @return TwoTuple<UsernameAndPasswordDto, Integer> 账号密码,Id
     */
    private TwoTuple<UsernameAndPasswordDTO, Integer> generateAccountAndPassword(ScreeningOrganizationStaffQuery staff) {
        TwoTuple<UsernameAndPasswordDTO, Integer> tuple = new TwoTuple<>();

        String password = PasswordGenerator.getScreeningUserPwd(staff.getPhone(), staff.getIdCard());
        String username = staff.getPhone();
        tuple.setFirst(new UsernameAndPasswordDTO(username, password));

        UserDTO userDTO = new UserDTO()
                .setOrgId(staff.getId())
                .setUsername(username)
                .setPassword(password)
                .setCreateUserId(staff.getCreateUserId())
                .setSystemCode(SystemCode.SCREENING_CLIENT.getCode())
                .setRealName(staff.getName())
                .setGender(staff.getGender())
                .setPhone(staff.getPhone())
                .setIdCard(staff.getIdCard())
                .setRemark(staff.getRemark());

        ApiResult<UserDTO> apiResult = oauthServiceClient.addAdminUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("创建管理员信息异常");
        }
        tuple.setSecond(apiResult.getData().getId());
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
