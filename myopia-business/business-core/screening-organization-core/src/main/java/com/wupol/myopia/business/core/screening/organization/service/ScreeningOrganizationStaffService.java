package com.wupol.myopia.business.core.screening.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.organization.domain.dto.*;
import com.wupol.myopia.business.core.screening.organization.domain.mapper.ScreeningOrganizationStaffMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 筛查人员
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class ScreeningOrganizationStaffService extends BaseService<ScreeningOrganizationStaffMapper, ScreeningOrganizationStaff> {

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;
    @Resource
    private OauthServiceClient oauthServiceClient;

    /**
     * 获取机构人员列表
     *
     * @param request 请求入参
     * @return Page<UserExtDTO> {@link Page}
     */
    public IPage<ScreeningOrgStaffUserDTO> getOrganizationStaffList(OrganizationStaffRequestDTO request) {
        UserDTO userQuery = new UserDTO();

        // 搜索条件
        userQuery.setCurrent(request.getCurrent())
                .setSize(request.getSize())
                .setOrgId(request.getScreeningOrgId())
                .setRealName(request.getName())
                .setIdCard(request.getIdCard())
                .setPhone(request.getPhone())
                .setSystemCode(SystemCode.SCREENING_CLIENT.getCode());
        // 获取筛查人员
        Page<User> page = oauthServiceClient.getUserListPage(userQuery);
        List<User> resultLists = page.getRecords();
        if (CollectionUtils.isEmpty(resultLists)) {
            return new Page<>(request.getCurrent(), request.getSize());
        }
        // 封装DTO，回填多端管理的ID
        List<Integer> userIds = resultLists.stream().map(User::getId).collect(Collectors.toList());
        Map<Integer, ScreeningOrganizationStaff> staffSnMaps = getStaffsByUserIds(userIds)
                .stream().collect(Collectors.toMap(ScreeningOrganizationStaff::getUserId, Function.identity()));
        return page.convert(user -> {
            ScreeningOrgStaffUserDTO screeningOrgStaffUserDTO = new ScreeningOrgStaffUserDTO(user);
            screeningOrgStaffUserDTO.setStaffId(staffSnMaps.get(user.getId()).getId());
            return screeningOrgStaffUserDTO;
        });
    }

    /**
     * 新增员工
     *
     * @param staffQuery 员工实体类
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO saveOrganizationStaff(ScreeningOrganizationStaffQueryDTO staffQuery) {

        // 检查身份证号码是否重复
        List<User> checkIdCards = oauthServiceClient.getUserBatchByIdCards(Lists.newArrayList(staffQuery.getIdCard()),
                        SystemCode.SCREENING_CLIENT.getCode(), staffQuery.getScreeningOrgId());
        if (!CollectionUtils.isEmpty(checkIdCards)) {
            throw new BusinessException("身份证已经被使用！");
        }

        // 检查手机号码是否重复
        List<User> checkPhones = oauthServiceClient.getUserBatchByPhones(Lists.newArrayList(staffQuery.getPhone()),
                        SystemCode.SCREENING_CLIENT.getCode());
        if (!CollectionUtils.isEmpty(checkPhones)) {
            throw new BusinessException("手机号码已经被使用");
        }

        // 生成账号密码
        TwoTuple<UsernameAndPasswordDTO, Integer> tuple = generateAccountAndPassword(staffQuery);
        staffQuery.setUserId(tuple.getSecond());
        save(staffQuery);
        return tuple.getFirst();
    }

    /**
     * 更新员工
     *
     * @param staff 员工实体类
     * @return 员工实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public ScreeningOrganizationStaffQueryDTO updateOrganizationStaff(ScreeningOrganizationStaffQueryDTO staff) {
        Integer id = staff.getId();
        ScreeningOrganizationStaff checkStaff = baseMapper.selectById(id);
        if (null == checkStaff || null == checkStaff.getUserId()) {
            log.error("更新筛查人员失败id:{},数据异常", id);
            throw new BusinessException("数据异常");
        }
        ScreeningOrganizationStaff admin = baseMapper.selectById(staff.getId());

        // 检查身份证号码是否重复
        List<User> checkIdCards = oauthServiceClient.getUserBatchByIdCards(Lists.newArrayList(staff.getIdCard()),
                        SystemCode.SCREENING_CLIENT.getCode(), checkStaff.getScreeningOrgId());
        if (!CollectionUtils.isEmpty(checkIdCards)) {
            if (checkIdCards.size() > 1) {
                throw new BusinessException("身份证号码重复");
            }
            if (!checkIdCards.get(0).getId().equals(admin.getUserId())) {
                throw new BusinessException("身份证号码重复");
            }
        }

        // 检查手机号码是否重复
        List<User> checkPhones = oauthServiceClient.getUserBatchByPhones(Lists.newArrayList(staff.getPhone()),
                        SystemCode.SCREENING_CLIENT.getCode());
        if (!CollectionUtils.isEmpty(checkPhones)) {
            if (checkPhones.size() > 1) {
                throw new BusinessException("手机号码重复");
            }
            if (!checkPhones.get(0).getId().equals(admin.getUserId())) {
                throw new BusinessException("手机号码重复");
            }
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setId(checkStaff.getUserId())
                .setRealName(staff.getRealName())
                .setGender(staff.getGender())
                .setPhone(staff.getPhone())
                .setIdCard(staff.getIdCard())
                .setUsername(staff.getPhone())
                .setRemark(staff.getRemark());
        oauthServiceClient.modifyUser(userDTO);
        resetPassword(new StaffResetPasswordRequestDTO(staff.getId(), staff.getPhone(), staff.getIdCard()));
        return staff;
    }

    /**
     * 更新状态
     *
     * @param request 入参
     * @return UserDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public User updateStatus(StatusRequest request) {
        ScreeningOrganizationStaff staff = baseMapper.selectById(request.getId());
        // 更新OAuth2
        UserDTO userDTO = new UserDTO();
        userDTO.setId(staff.getUserId())
                .setStatus(request.getStatus());
        return oauthServiceClient.modifyUser(userDTO);
    }

    /**
     * 重置密码
     *
     * @param request 入参
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(StaffResetPasswordRequestDTO request) {
        ScreeningOrganizationStaff staff = baseMapper.selectById(request.getStaffId());
        String password = PasswordGenerator.getScreeningUserPwd(request.getPhone(), request.getIdCard());
        String username = request.getPhone();
        oauthServiceClient.resetPwd(staff.getUserId(), password);
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 根据用户id列表查询
     */
    public List<ScreeningOrganizationStaff> getByIds(List<Integer> ids) {
        return baseMapper.selectBatchIds(ids);
    }

    /**
     * 生成账号密码
     *
     * @return TwoTuple<UsernameAndPasswordDto, Integer> 账号密码,Id
     */
    private TwoTuple<UsernameAndPasswordDTO, Integer> generateAccountAndPassword(ScreeningOrganizationStaffQueryDTO staff) {
        TwoTuple<UsernameAndPasswordDTO, Integer> tuple = new TwoTuple<>();

        String password = PasswordGenerator.getScreeningUserPwd(staff.getPhone(), staff.getIdCard());
        String username = staff.getPhone();
        tuple.setFirst(new UsernameAndPasswordDTO(username, password));

        UserDTO userDTO = new UserDTO();
        userDTO.setOrgId(staff.getScreeningOrgId())
                .setUsername(username)
                .setPassword(password)
                .setCreateUserId(staff.getCreateUserId())
                .setSystemCode(SystemCode.SCREENING_CLIENT.getCode())
                .setRealName(staff.getRealName())
                .setGender(staff.getGender())
                .setPhone(staff.getPhone())
                .setIdCard(staff.getIdCard())
                .setRemark(staff.getRemark());

        User user = oauthServiceClient.addMultiSystemUser(userDTO);
        tuple.setSecond(user.getId());
        return tuple;
    }

    /**
     * 批量新增, 自动生成编号
     */
    public void saveBatch(List<ScreeningOrganizationStaffDTO> list) {
        if (CollectionUtils.isEmpty(list)) return;
        // 通过screeningOrgId获取机构
        ScreeningOrganization organization = screeningOrganizationService.getById(list.get(0).getScreeningOrgId());
        if (null == organization) {
            throw new BusinessException("数据异常,找不到筛查机构的数据,id为:" + list.get(0).getScreeningOrgId());
        }
        super.saveBatch(list.stream().map(item -> (ScreeningOrganizationStaff) item).collect(Collectors.toList()));
    }

    /**
     * 通过组织Id获取员工
     *
     * @param orgId 组织id
     * @return List<ScreeningOrganizationStaff>
     */
    public List<ScreeningOrganizationStaff> getByOrgId(Integer orgId) {
        return baseMapper.getByOrgId(orgId);
    }

    /**
     * 批量通过组织Id获取员工
     *
     * @param orgIds 组织id
     * @return List<ScreeningOrganizationStaff>
     */
    public List<ScreeningOrganizationStaff> getStaffListsByOrgIds(List<Integer> orgIds) {
        return baseMapper.getByOrgIds(orgIds);
    }

    /**
     * 批量通过组织Id获取筛查人员信息
     *
     * @param orgIds orgIds
     * @return Map<Integer, List < ScreeningOrganizationStaff>>
     */
    public Map<Integer, List<ScreeningOrganizationStaff>> getOrgStaffMapByIds(List<Integer> orgIds) {
        return getStaffListsByOrgIds(orgIds).stream()
                .collect(Collectors.groupingBy(ScreeningOrganizationStaff::getScreeningOrgId));

    }

    /**
     * 获取员工通过userIds
     *
     * @param userIds 用户id
     * @return 员工
     */
    public List<ScreeningOrganizationStaff> getStaffsByUserIds(List<Integer> userIds) {
        return baseMapper.getByUserIds(userIds);
    }

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<ScreeningOrganizationStaff> getByPage(Page<?> page, ScreeningOrganizationStaffQueryDTO query) {
        return baseMapper.getByPage(page, query);
    }


    public List<ScreeningOrganizationStaff> getByEntity(ScreeningOrganizationStaff screeningOrganizationStaff) {
        LambdaQueryWrapper<ScreeningOrganizationStaff> screeningOrganizationStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
        screeningOrganizationStaffLambdaQueryWrapper.setEntity(screeningOrganizationStaff);
        return baseMapper.selectList(screeningOrganizationStaffLambdaQueryWrapper);
    }

}