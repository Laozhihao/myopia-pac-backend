package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.SchoolDto;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.management.domain.mapper.SchoolMapper;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.SchoolAdmin;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
@Log4j2
public class SchoolService extends BaseService<SchoolMapper, School> {

    @Value(value = "${oem.province.code}")
    private Long provinceCode;

    @Resource
    private SchoolAdminService schoolAdminService;

    @Resource
    private SchoolMapper schoolMapper;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private DistrictService districtService;

    @Resource
    private OauthService oauthService;

    /**
     * 新增学校
     *
     * @param school 学校实体
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO saveSchool(School school) {

        Integer createUserId = school.getCreateUserId();
        Long townCode = school.getTownCode();
        if (null == townCode) {
            throw new BusinessException("数据异常");
        }

        // 初始化省代码
        school.setProvinceCode(provinceCode);
        // 设置行政区域名
        school.setDistrictName(districtService.getDistrictNameById(school.getDistrictId()));

        RLock rLock = redissonClient.getLock(Const.LOCK_SCHOOL_REDIS + townCode);
        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {
                baseMapper.insert(school);
                return generateAccountAndPassword(school);
            }
        } catch (InterruptedException e) {
            log.error("用户id:{}获取锁异常,e:{}", createUserId, e);
            throw new BusinessException("系统繁忙，请稍后再试");
        }
        log.warn("用户id:{}新增学校获取不到锁，区域代码:{}", createUserId, townCode);
        throw new BusinessException("请重试");
    }

    /**
     * 更新学校
     *
     * @param school 学校实体类
     * @return 学校实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public School updateSchool(School school) {
        // 设置行政区域名
        school.setDistrictName(districtService.getDistrictNameById(school.getDistrictId()));
        baseMapper.updateById(school);
        return baseMapper.selectById(school.getId());
    }

    /**
     * 删除学校
     *
     * @param id 学校id
     * @return 删除数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedSchool(Integer id) {
        School school = new School();
        school.setId(id);
        school.setStatus(Const.STATUS_IS_DELETED);
        return baseMapper.updateById(school);
    }

    /**
     * 更新状态
     *
     * @param request 入参
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateStatus(StatusRequest request) {

        SchoolAdmin staff = schoolAdminService.getAdminBySchoolId(request.getId());
        // 更新OAuth2
        UserDTO userDTO = new UserDTO()
                .setId(staff.getUserId())
                .setStatus(request.getStatus());
        oauthService.modifyUser(userDTO);
        School school = new School().setId(request.getId()).setStatus(request.getStatus());
        return schoolMapper.updateById(school);
    }

    /**
     * 获取学校列表
     *
     * @param pageRequest 分页
     * @param schoolQuery 请求体
     * @param currentUser 当前用户
     * @return IPage<SchoolDto> {@link IPage}
     */
    public IPage<SchoolDto> getSchoolList(PageRequest pageRequest, SchoolQuery schoolQuery,
                                          CurrentUser currentUser) {

        Integer orgId = currentUser.getOrgId();
        String createUser = schoolQuery.getCreateUser();
        List<Integer> userIds = new ArrayList<>();

        Integer districtId = districtService.getDistrictId(currentUser, schoolQuery.getDistrictId());

        // 创建人ID处理
        if (StringUtils.isNotBlank(createUser)) {
            UserDTOQuery query = new UserDTOQuery();
            query.setRealName(createUser);
            Page<UserDTO> userListPage = oauthService.getUserListPage(query);
            List<UserDTO> records = userListPage.getRecords();
            if (!CollectionUtils.isEmpty(userListPage.getRecords())) {
                userIds = records.stream().map(UserDTO::getId).collect(Collectors.toList());
            }
        }

        // 查询
        IPage<SchoolDto> schoolDtoIPage = schoolMapper.getSchoolListByCondition(pageRequest.toPage(),
                schoolQuery.getName(), schoolQuery.getSchoolNo(),
                schoolQuery.getType(), districtId, userIds);

        List<SchoolDto> schools = schoolDtoIPage.getRecords();

        // 为空直接返回
        if (CollectionUtils.isEmpty(schools)) {
            return schoolDtoIPage;
        }

        // 封装DTO
        schools.forEach(s -> {
            s.setScreeningTime(0);
            // 判断是否能更新
            if (s.getGovDeptId().equals(orgId)) {
                s.setCanUpdate(Boolean.TRUE);
            }
        });
        return schoolDtoIPage;
    }

    /**
     * 重置密码
     *
     * @param id 医院id
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(Integer id) {
        School school = schoolMapper.selectById(id);
        if (null == school) {
            throw new BusinessException("数据异常");
        }
        SchoolAdmin admin = schoolAdminService.getAdminBySchoolId(id);
        return resetOAuthPassword(school, admin.getUserId());
    }


    /**
     * 生成账号密码
     *
     * @return UsernameAndPasswordDto 账号密码
     */
    private UsernameAndPasswordDTO generateAccountAndPassword(School school) {
        String password = PasswordGenerator.getSchoolAdminPwd();
        String username = school.getName();

        UserDTO userDTO = new UserDTO()
                .setOrgId(school.getId())
                .setUsername(username)
                .setPassword(password)
                .setCreateUserId(school.getCreateUserId())
                .setSystemCode(SystemCode.SCHOOL_CLIENT.getCode());

        UserDTO user = oauthService.addAdminUser(userDTO);
        schoolAdminService.insertStaff(school.getId(), school.getCreateUserId(), school.getGovDeptId(), user.getId());
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 重置密码
     *
     * @param school 学校
     * @param userId OAuth2 的userId
     * @return 账号密码
     */
    private UsernameAndPasswordDTO resetOAuthPassword(School school, Integer userId) {
        String password = PasswordGenerator.getSchoolAdminPwd();
        String username = school.getName();

        UserDTO userDTO = new UserDTO()
                .setId(userId)
                .setUsername(username)
                .setPassword(password);
        oauthService.modifyUser(userDTO);
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 获取导出数据
     */
    public List<School> getExportData(SchoolQuery query) {
        return baseMapper.getExportData(query);
    }
}