package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
@Log4j2
public class ScreeningOrganizationService extends BaseService<ScreeningOrganizationMapper, ScreeningOrganization> {

    @Resource
    private GovDeptService govDeptService;

    @Resource
    private ScreeningOrganizationMapper screeningOrganizationMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 保存筛查机构
     *
     * @param screeningOrganization 筛查机构
     * @return Integer 插入个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveScreeningOrganization(ScreeningOrganization screeningOrganization) {

        Integer townCode = screeningOrganization.getTownCode();
        Integer createUserId = screeningOrganization.getCreateUserId();

        if (null == townCode) {
            throw new BusinessException("数据异常");
        }
        RLock rLock = redissonClient.getLock(Const.LOCK_ORG_REDIS + townCode);

        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {
                screeningOrganization.setOrgNo(generateOrgNoByRedis(townCode));
                baseMapper.insert(screeningOrganization);
            }
        } catch (InterruptedException e) {
            log.error("用户id:{}获取锁异常", createUserId);
            throw new BusinessException("系统繁忙，请稍后再试");
        } finally {
            if (rLock.isLocked()) {
                rLock.unlock();
            }
        }
        log.warn("用户id:{}新增机构获取不到锁，区域代码:{}", createUserId, townCode);
        throw new BusinessException("请重试");
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
     * 更新机构状态
     *
     * @param request 入参
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateStatus(StatusRequest request) {
        ScreeningOrganization org = new ScreeningOrganization();
        org.setId(request.getId());
        org.setStatus(request.getStatus());
        return baseMapper.updateById(org);
    }

    /**
     * 生成编号
     *
     * @param code 镇代码
     * @return 代码
     */
    private String generateOrgNo(Integer code) {
        ScreeningOrganization org = screeningOrganizationMapper.getLastOrgByNo(code);
        if (null == org) {
            return StringUtils.join(code, "201");
        }
        return String.valueOf(Long.parseLong(org.getOrgNo()) + 1);
    }

    /**
     * 生成编号通过Redis
     *
     * @param code 镇代码
     * @return 编号
     */
    private String generateOrgNoByRedis(Integer code) {
        // 查询redis是否存在
        String key = Const.GENERATE_ORG_SN + code;
        Integer queueCount = (Integer) redisUtil.get(key);
        if (null == queueCount) {
            ScreeningOrganization org = screeningOrganizationMapper.getLastOrgByNo(code);
            // 数据库不存在
            if (null == org) {
                // 数据库不存在，初始化
                redisUtil.set(key, 201);
                return StringUtils.join(code, "201");
            }
            // 获取当前数据库中最新的编号并且加一
            String resultCode = String.valueOf(Long.parseLong(org.getOrgNo()) + 1);
            // 获取后三位,并缓存到redis中
            redisUtil.set(key, Integer.valueOf(StringUtils.right(resultCode, 3)));
            return resultCode;
        }
        // 自增一,并且返回
        return StringUtils.join(code, redisUtil.incr(key, 1));
    }
}