package com.wupol.myopia.business.management.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.constant.CacheKey;
import com.wupol.myopia.business.management.domain.dto.*;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationStaffMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationStaffQuery;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningOrganizationStaffVo;
import com.wupol.myopia.business.management.util.TwoTuple;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private OauthService oauthService;

    /**
     * 获取机构人员列表
     *
     * @param request     请求入参
     * @param currentUser 当前登录用户
     * @return Page<UserExtDTO> {@link Page}
     */
    public Page<UserExtDTO> getOrganizationStaffList(OrganizationStaffRequest request, CurrentUser currentUser) {
        UserDTOQuery userQuery = new UserDTOQuery();

        // 非平台管理员需要机构ID进行过滤
//        if (!currentUser.isPlatformAdminUser()) {
//            userQuery.setOrgId(request.getScreeningOrgId());
//        }
        userQuery.setOrgId(request.getScreeningOrgId());

        // 搜索条件
        userQuery.setCurrent(request.getCurrent())
                .setSize(request.getSize())
                .setRealName(request.getName())
                .setIdCard(request.getIdCard())
                .setPhone(request.getPhone())
                .setSystemCode(SystemCode.SCREENING_CLIENT.getCode());
        // 获取筛查人员
        Page<UserExtDTO> page = JSONObject
                .parseObject(JSONObject.toJSONString(oauthService.getUserListPage(userQuery)),
                        new TypeReference<Page<UserExtDTO>>() {
                        });
        List<UserExtDTO> resultLists = page.getRecords();
        // 封装DTO，回填多端管理的ID
        if (!CollectionUtils.isEmpty(resultLists)) {
            List<Integer> userIds = resultLists.stream().map(UserExtDTO::getId).collect(Collectors.toList());
            Map<Integer, ScreeningOrganizationStaff> staffSnMaps = getStaffsByUserIds(userIds)
                    .stream().collect(Collectors
                            .toMap(ScreeningOrganizationStaff::getUserId, Function.identity()));
            resultLists.forEach(s -> s.setStaffId(staffSnMaps.get(s.getId()).getId()));
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

        // 检查身份证号码是否重复
        List<UserDTO> checkIdCards = oauthService
                .getUserBatchByIdCards(Lists.newArrayList(staffQuery.getIdCard()),
                        SystemCode.SCREENING_CLIENT.getCode(), staffQuery.getScreeningOrgId());
        if (!CollectionUtils.isEmpty(checkIdCards)) {
            throw new BusinessException("身份证已经被使用！");
        }

        // 检查手机号码是否重复
        List<UserDTO> checkPhones = oauthService
                .getUserBatchByPhones(Lists.newArrayList(staffQuery.getPhone()),
                        SystemCode.SCREENING_CLIENT.getCode());
        if (!CollectionUtils.isEmpty(checkPhones)) {
            throw new BusinessException("手机号码已经被使用");
        }

        RLock rLock = redissonClient.getLock(String.format(CacheKey.LOCK_ORG_STAFF_REDIS, phone));
        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {

                // 生成账号密码
                TwoTuple<UsernameAndPasswordDTO, Integer> tuple = generateAccountAndPassword(staffQuery);
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
        Integer id = staff.getId();
        ScreeningOrganizationStaff checkStaff = baseMapper.selectById(id);
        if (null == checkStaff || null == checkStaff.getUserId()) {
            log.error("更新筛查人员失败id:{},数据异常", id);
            throw new BusinessException("数据异常");
        }
        ScreeningOrganizationStaff admin = baseMapper.selectById(staff.getId());

        // 检查身份证号码是否重复
        List<UserDTO> checkIdCards = oauthService
                .getUserBatchByIdCards(Lists.newArrayList(staff.getIdCard()),
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
        List<UserDTO> checkPhones = oauthService
                .getUserBatchByPhones(Lists.newArrayList(staff.getPhone()),
                        SystemCode.SCREENING_CLIENT.getCode());
        if (!CollectionUtils.isEmpty(checkPhones)) {
            if (checkPhones.size() > 1) {
                throw new BusinessException("手机号码重复");
            }
            if (!checkPhones.get(0).getId().equals(admin.getUserId())) {
                throw new BusinessException("手机号码重复");
            }
        }
        UserDTO userDTO = new UserDTO()
                .setId(checkStaff.getUserId())
                .setRealName(staff.getRealName())
                .setGender(staff.getGender())
                .setPhone(staff.getPhone())
                .setIdCard(staff.getIdCard())
                .setUsername(staff.getPhone())
                .setRemark(staff.getRemark());
        oauthService.modifyUser(userDTO);
        resetPassword(new StaffResetPasswordRequest(staff.getId(), staff.getPhone(), staff.getIdCard()));
        return staff;
    }

    /**
     * 更新状态
     *
     * @param request 入参
     * @return UserDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateStatus(StatusRequest request) {
        ScreeningOrganizationStaff staff = baseMapper.selectById(request.getId());
        // 更新OAuth2
        UserDTO userDTO = new UserDTO()
                .setId(staff.getUserId())
                .setStatus(request.getStatus());
        return oauthService.modifyUser(userDTO);
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
        oauthService.resetPwd(staff.getUserId(), password);
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

        UserDTO user = oauthService.addAdminUser(userDTO);
        tuple.setSecond(user.getId());
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
        return super.saveBatch(list.stream().map(item -> (ScreeningOrganizationStaff) item).collect(Collectors.toList()));
    }

    /**
     * 通过组织Id获取员工
     *
     * @param orgId 组织id
     * @return List<ScreeningOrganizationStaff>
     */
    public List<ScreeningOrganizationStaff> getByOrgId(Integer orgId) {
        return baseMapper
                .selectList(new QueryWrapper<ScreeningOrganizationStaff>()
                        .eq("screening_org_id", orgId));
    }

    /**
     * 通过组织Id获取员工
     *
     * @param orgIds 组织id
     * @return List<ScreeningOrganizationStaff>
     */
    public List<ScreeningOrganizationStaff> getStaffListsByOrgIds(List<Integer> orgIds) {
        return baseMapper
                .selectList(new QueryWrapper<ScreeningOrganizationStaff>()
                        .in("screening_org_id", orgIds));
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
        return baseMapper
                .selectList(new QueryWrapper<ScreeningOrganizationStaff>()
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

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<ScreeningOrganizationStaff> getByPage(Page<?> page, ScreeningOrganizationStaffQuery query) {
        return baseMapper.getByPage(page, query);
    }
}
