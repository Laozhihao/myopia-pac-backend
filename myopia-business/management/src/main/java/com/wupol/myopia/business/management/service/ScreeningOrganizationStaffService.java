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
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.*;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationStaffMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationStaffQuery;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningOrganizationStaffVo;
import com.wupol.myopia.business.management.util.TwoTuple;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
@Log4j2
public class ScreeningOrganizationStaffService extends BaseService<ScreeningOrganizationStaffMapper, ScreeningOrganizationStaff> {

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    @Qualifier("com.wupol.myopia.business.management.client.OauthServiceClient")
    @Autowired
    private OauthServiceClient oauthServiceClient;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 获取机构人员列表
     *
     * @param request 请求入参
     * @return Page<UserExtDTO> {@link Page}
     */
    public Page<UserExtDTO> getOrganizationStaffList(OrganizationStaffRequest request) {
        UserDTOQuery userQuery = new UserDTOQuery();
        userQuery.setCurrent(request.getCurrent())
                .setSize(request.getSize())
                .setOrgId(request.getScreeningOrgId())
                .setRealName(request.getName())
                .setIdCard(request.getIdCard())
                .setPhone(request.getPhone())
                .setSystemCode(SystemCode.SCREENING_CLIENT.getCode());
        ApiResult apiResult = oauthServiceClient.getUserListPage(userQuery);
        if (!apiResult.isSuccess()) {
            throw new BusinessException(apiResult.getMessage());
        }
        Page<UserExtDTO> page = JSONObject.parseObject(JSONObject.toJSONString(apiResult.getData()), new TypeReference<Page<UserExtDTO>>() {
        });
        List<UserExtDTO> resultLists = page.getRecords();
        if (!CollectionUtils.isEmpty(resultLists)) {
            List<Integer> userIds = resultLists.stream().map(UserExtDTO::getId).collect(Collectors.toList());
            Map<Integer, ScreeningOrganizationStaff> staffSnMaps = getStaffsByUserIds(userIds)
                    .stream()
                    .collect(Collectors.
                            toMap(ScreeningOrganizationStaff::getUserId, Function.identity()));
            resultLists.forEach(s -> {
                s.setStaffNo(staffSnMaps.get(s.getId()).getStaffNo());
                s.setStaffId(staffSnMaps.get(s.getId()).getId());
            });
            return page;
        }
        return page;
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
    public UsernameAndPasswordDTO saveOrganizationStaff(ScreeningOrganizationStaffQuery staffQuery) {

        Integer createUserId = staffQuery.getCreateUserId();
        String phone = staffQuery.getPhone();

        // 通过screeningOrgId获取机构
        ScreeningOrganization organization = screeningOrganizationService.getById(staffQuery.getScreeningOrgId());
        String staffNo = generateOrgNo(organization.getOrgNo(), staffQuery.getIdCard());
        if (countStaffNo(staffNo) > 0) {
            throw new BusinessException("编号已经存在");
        }

        RLock rLock = redissonClient.getLock(Const.LOCK_ORG_STAFF_REDIS + phone);
        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {

                // 生成账号密码
                TwoTuple<UsernameAndPasswordDTO, Integer> tuple = generateAccountAndPassword(staffQuery);
                staffQuery.setStaffNo(staffNo);
                staffQuery.setUserId(tuple.getSecond());

                save(staffQuery);
                return tuple.getFirst();
            }
        } catch (InterruptedException e) {
            log.error("用户:{}创建机构人员获取锁异常,e:{}", createUserId, e);
            throw new BusinessException("系统繁忙，请稍后再试");
        } finally {
            if (rLock.isLocked()) {
                rLock.unlock();
            }
        }
        log.warn("用户id:{}新增机构获取不到锁，新增人员手机号码:{}", createUserId, phone);
        throw new BusinessException("请重试");
    }

    /**
     * 更新员工
     *
     * @param staff 员工实体类
     * @return 员工实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public ScreeningOrganizationStaffQuery updateOrganizationStaff(ScreeningOrganizationStaffQuery staff) {

        UserDTO userDTO = new UserDTO()
                .setId(staff.getUserId())
                .setRealName(staff.getRealName())
                .setGender(staff.getGender())
                .setPhone(staff.getPhone())
                .setIdCard(staff.getIdCard())
                .setRemark(staff.getRemark());
        ApiResult<UserDTO> apiResult = oauthServiceClient.modifyUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("OAuth2 异常");
        }
        baseMapper.updateById(staff);
        ScreeningOrganizationStaff resultStaff = baseMapper.selectById(staff.getId());
        staff.setStaffNo(resultStaff.getStaffNo());
        return staff;
    }

    /**
     * 更新状态
     *
     * @param request 入参
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateStatus(StatusRequest request) {

        ScreeningOrganizationStaff staff = baseMapper.selectById(request.getId());
        // 更新OAuth2
        UserDTO userDTO = new UserDTO()
                .setId(staff.getUserId())
                .setStatus(request.getStatus());
        ApiResult<UserDTO> apiResult = oauthServiceClient.modifyUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("OAuth2 异常");
        }
        return 1;
    }

    /**
     * 重置密码
     *
     * @param request 入参
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(StaffResetPasswordRequest request) {
        ScreeningOrganizationStaff staff = baseMapper.selectById(request.getStaffId());
        String password = PasswordGenerator.getScreeningUserPwd(request.getPhone(), request.getIdCard());
        String username = request.getPhone();
        UserDTO userDTO = new UserDTO()
                .setId(staff.getId())
                .setUsername(username)
                .setPassword(password);
        ApiResult<UserDTO> apiResult = oauthServiceClient.modifyUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("OAuth2 异常");
        }
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 根据用户id列表查询
     */
    public List<ScreeningOrganizationStaff> getByIds(List<Integer> ids) {
        return baseMapper.getByIds(ids);
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
                .setOrgId(staff.getScreeningOrgId())
                .setUsername(username)
                .setPassword(password)
                .setCreateUserId(staff.getCreateUserId())
                .setSystemCode(SystemCode.SCREENING_CLIENT.getCode())
                .setRealName(staff.getRealName())
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
     * 批量新增, 自动生成编号
     */
    public Boolean saveBatch(List<ScreeningOrganizationStaffVo> list) {
        if (CollectionUtils.isEmpty(list)) return false;
        // 通过screeningOrgId获取机构
        ScreeningOrganization organization = screeningOrganizationService.getById(list.get(0).getScreeningOrgId());
        if (null == organization) {
            throw new BusinessException("数据异常,找不到筛查机构的数据,id为:" + list.get(0).getScreeningOrgId());
        }
        for (ScreeningOrganizationStaffVo item : list) {
            item.setStaffNo(generateOrgNo(organization.getOrgNo(), item.getIdCard()));
        }
        return super.saveBatch(list.stream().map(item -> (ScreeningOrganizationStaff) item).collect(Collectors.toList()));
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

    /**
     * 通过组织Id获取员工
     *
     * @param orgIds 组织id
     * @return List<ScreeningOrganizationStaff>
     */
    public List<ScreeningOrganizationStaff> getStaffListsByOrgIds(List<Integer> orgIds) {
        return baseMapper.selectList(new QueryWrapper<ScreeningOrganizationStaff>().in("screening_org_id", orgIds));
    }

    /**
     * 获取员工通过userIds
     *
     * @param userIds 用户id
     * @return 员工
     */
    public List<ScreeningOrganizationStaff> getStaffsByUserIds(List<Integer> userIds) {
        return baseMapper.selectList(new QueryWrapper<ScreeningOrganizationStaff>()
                .in("user_id", userIds));
    }

    /**
     * 统计员工编号
     *
     * @param staffNo 编号
     * @return 数量
     */
    public Integer countStaffNo(String staffNo) {
        return baseMapper.selectCount(new QueryWrapper<ScreeningOrganizationStaff>()
                .eq("staff_no", staffNo));
    }
}
