package com.wupol.myopia.business.core.screening.organization.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordAndUsernameGenerator;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.dos.ScreeningOrgCountDO;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
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
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    @Resource
    private ResourceFileService resourceFileService;

    /**
     * 获取机构人员列表
     *
     * @param request 请求入参
     * @return Page<UserExtDTO> {@link Page}
     */
    public IPage<ScreeningOrgStaffUserDTO> getOrganizationStaffList(PageRequest pageRequest,OrganizationStaffRequestDTO request,CurrentUser users) {
        UserDTO userQuery = new UserDTO();

        // 搜索条件
        userQuery.setOrgId(request.getScreeningOrgId())
                .setRealName(request.getName())
                .setIdCard(request.getIdCard())
                .setPhone(request.getPhone())
                .setSystemCode(SystemCode.SCREENING_CLIENT.getCode());
        // 获取筛查人员
        List<User> list = oauthServiceClient.getUserList(userQuery);
        List<User> resultLists = JSON.parseArray(JSON.toJSONString(list), User.class);
        if (CollectionUtils.isEmpty(resultLists)) {
            return new Page<>(request.getCurrent(), request.getSize());
        }
        // 封装DTO，回填多端管理的ID
        List<Integer> userIds = resultLists.stream().map(User::getId).collect(Collectors.toList());
        ScreeningOrganizationStaffQueryDTO staffQueryDTO = new ScreeningOrganizationStaffQueryDTO();
        staffQueryDTO.setUserIds(userIds);
        staffQueryDTO.setType(users.isPlatformAdminUser() ? null : ScreeningOrganizationStaff.GENERAL_SCREENING_PERSONNEL);
        IPage<ScreeningOrganizationStaff> page = getByPage(pageRequest.toPage(),staffQueryDTO);
        Map<Integer, ScreeningOrganizationStaff> staffSnMaps = page.getRecords()
                .stream().collect(Collectors.toMap(ScreeningOrganizationStaff::getUserId, Function.identity()));
        List<ScreeningOrgStaffUserDTO> screeningOrgStaffUserDTOList = new ArrayList<>();
        for (User user : resultLists){
            ScreeningOrgStaffUserDTO screeningOrgStaffUserDTO = new ScreeningOrgStaffUserDTO(user);
            ScreeningOrganizationStaff staff = staffSnMaps.get(user.getId());
            if (staff != null){
                screeningOrgStaffUserDTO.setStaffId(staff.getId());
                if (Objects.nonNull(staff.getSignFileId())) {
                    screeningOrgStaffUserDTO.setSignFileUrl(resourceFileService.getResourcePath(staff.getSignFileId()));
                }
                screeningOrgStaffUserDTOList.add(screeningOrgStaffUserDTO);
            }
        }
        return new Page<ScreeningOrgStaffUserDTO>(page.getCurrent(), page.getSize(), page.getTotal()).setRecords(screeningOrgStaffUserDTOList);
    }

    /**
     * 新增员工
     *
     * @param staffQuery 员工实体类
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO saveOrganizationStaff(ScreeningOrganizationStaffQueryDTO staffQuery) {
        if (staffQuery.getType() == null || staffQuery.getType()==ScreeningOrganizationStaff.GENERAL_SCREENING_PERSONNEL){
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
                .setRemark(staff.getRemark())
                .setSystemCode(SystemCode.SCREENING_CLIENT.getCode())
                .setUserType(UserType.SCREENING_STAFF_TYPE_ORG.getType());
        oauthServiceClient.updateUser(userDTO);
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
        return oauthServiceClient.updateUser(userDTO);
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
        User user = oauthServiceClient.getUserDetailByUserId(staff.getUserId());
        String password = ScreeningOrganizationStaff.AUTO_CREATE_STAFF_DEFAULT_PASSWORD;
        String username = user.getUsername();
        if (!ScreeningOrganizationStaff.isAutoCreateScreeningStaff(staff.getType())){
            password = PasswordAndUsernameGenerator.getScreeningUserPwd(request.getPhone(), request.getIdCard());
            username = request.getPhone();
            oauthServiceClient.resetPwd(staff.getUserId(), password);
        }
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
        String password;
        String username;
        UserDTO userDTO = new UserDTO();
        //如果是创建机构时自动新增的人员
        if (ScreeningOrganizationStaff.isAutoCreateScreeningStaff(staff.getType())){
            password = ScreeningOrganizationStaff.AUTO_CREATE_STAFF_DEFAULT_PASSWORD;
            username = staff.getUserName();
        }else{
            password = PasswordAndUsernameGenerator.getScreeningUserPwd(staff.getPhone(), staff.getIdCard());
            username = staff.getPhone();
        }
        tuple.setFirst(new UsernameAndPasswordDTO(username, password));

        userDTO.setOrgId(staff.getScreeningOrgId())
                .setUsername(username)
                .setPassword(password)
                .setCreateUserId(staff.getCreateUserId())
                .setSystemCode(SystemCode.SCREENING_CLIENT.getCode())
                .setRealName(staff.getRealName())
                .setGender(staff.getGender())
                .setPhone(staff.getPhone())
                .setIdCard(staff.getIdCard())
                .setRemark(staff.getRemark())
                .setUserType(UserType.SCREENING_STAFF_TYPE_ORG.getType());
        User user = oauthServiceClient.addMultiSystemUser(userDTO);
        tuple.setSecond(user.getId());
        return tuple;
    }

    /**
     * 批量新增, 自动生成编号
     */
    public void saveBatch(List<ScreeningOrganizationStaffDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        // 通过screeningOrgId获取机构
        ScreeningOrganization organization = screeningOrganizationService.getById(list.get(0).getScreeningOrgId());
        if (null == organization) {
            throw new BusinessException("数据异常,找不到筛查机构的数据,id为:" + list.get(0).getScreeningOrgId());
        }
        super.saveBatch(list.stream().map(ScreeningOrganizationStaff.class::cast).collect(Collectors.toList()));
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
     * @return List<ScreeningOrgCountDO>
     */
    public Map<Integer, Integer> countByOrgIds(List<Integer> orgIds, Integer type) {
        return baseMapper.countByOrgIds(orgIds,type).stream().collect(Collectors.toMap(ScreeningOrgCountDO::getScreeningOrgId, ScreeningOrgCountDO::getCount));
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
     * 通过用户id获取人员信息
     * @param userId
     * @return
     */
    public ScreeningOrganizationStaff getStaffsByUserId(Integer userId){

        return baseMapper.getByUserId(userId);
    }

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<ScreeningOrganizationStaff> getByPage(Page<?> page, ScreeningOrganizationStaffQueryDTO query) {
        return baseMapper.getByPage(page, query.getUserIds(),query.getType());
    }

    /**
     * 更新机构人员的id
     *
     * @param currentUser  当前用户
     * @param resourceFile 资源文件
     */
    public void updateOrganizationStaffSignId(CurrentUser currentUser, ResourceFile resourceFile) {
        ScreeningOrganizationStaff screeningOrganizationStaff = new ScreeningOrganizationStaff();
        screeningOrganizationStaff.setScreeningOrgId(currentUser.getOrgId()).setUserId(currentUser.getId());
        List<ScreeningOrganizationStaff> screeningOrganizationStaffs = getByEntity(screeningOrganizationStaff);
        if (CollectionUtils.isNotEmpty(screeningOrganizationStaffs)) {
            screeningOrganizationStaff = screeningOrganizationStaffs.stream().findFirst().orElseThrow(() -> new BusinessException("无法找到当前用户的机构人员信息,当前用户数据 User = " + JSON.toJSONString(currentUser)));
        }
        if (screeningOrganizationStaff.getId() != null) {
            screeningOrganizationStaff.setSignFileId(resourceFile.getId());
            baseMapper.updateById(screeningOrganizationStaff);
        }
    }

    /**
     * 通过条件获取筛查人员列表
     *
     * @param screeningOrganizationStaff 筛查人员
     * @return 筛查人员列表
     */
    public List<ScreeningOrganizationStaff> getByEntity(ScreeningOrganizationStaff screeningOrganizationStaff) {
        LambdaQueryWrapper<ScreeningOrganizationStaff> screeningOrganizationStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
        screeningOrganizationStaffLambdaQueryWrapper.setEntity(screeningOrganizationStaff);
        return baseMapper.selectList(screeningOrganizationStaffLambdaQueryWrapper);
    }

    /**
     * 根据筛查机构ID统计
     *
     * @param screeningOrgId 筛查
     * @return int
     **/
    public int countByScreeningOrgId(Integer screeningOrgId) {
        Assert.notNull(screeningOrgId, "screeningOrgId不能为空");
        return count(new ScreeningOrganizationStaff().setScreeningOrgId(screeningOrgId).setType(ScreeningOrganizationStaff.GENERAL_SCREENING_PERSONNEL));
    }



    /**
     * 校验筛查机构人员数量
     *
     * @param screeningOrgId 筛查
     * @return int
     **/
    public void checkScreeningOrganizationStaffAmount(Integer screeningOrgId,List<UserDTO> listMap){

        ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(screeningOrgId);
        int totalNum = countByScreeningOrgId(screeningOrgId);
        Assert.isTrue(totalNum < screeningOrganization.getAccountNum(), "账号数量已达上限，请联系管理员!");

        if (CollectionUtils.isNotEmpty(listMap) && listMap.size()>(screeningOrganization.getAccountNum()-totalNum)){
            throw new BusinessException("您已超出限制数据（筛查人员账号数量限制："+screeningOrganization.getAccountNum()+"个），操作失败！\n" +
                    "如需增加筛查人员账号数量，请联系管理员！");
        }


    }

}