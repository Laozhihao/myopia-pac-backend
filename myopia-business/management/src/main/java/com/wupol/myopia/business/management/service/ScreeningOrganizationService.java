package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
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

    /**
     * 保存筛查机构
     *
     * @param screeningOrganization 筛查机构
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveScreeningOrganization(ScreeningOrganization screeningOrganization) {
        screeningOrganization.setOrgNo(generateOrgNo());
        generateAccountAndPassword();
        return baseMapper.insert(screeningOrganization);
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
     */
    private void generateAccountAndPassword() {

    }

    /**
     * 生成编号
     *
     * @return Long
     */
    private Long generateOrgNo() {
        return 345L;
    }
}
