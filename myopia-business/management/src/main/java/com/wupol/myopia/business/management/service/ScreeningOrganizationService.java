package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.ScreeningOrganizationListRequest;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import org.apache.commons.lang3.StringUtils;
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
    private HospitalService hospitalService;

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
     * @param request   筛查机构列表请求体
     * @param govDeptId 机构id
     * @return IPage<ScreeningOrganization> {@link IPage}
     */
    public IPage<ScreeningOrganization> getScreeningOrganizationList(ScreeningOrganizationListRequest request, Integer govDeptId) {
        Page<ScreeningOrganization> page = new Page<>(request.getPage(), request.getLimit());
        QueryWrapper<ScreeningOrganization> wrapper = new QueryWrapper<>();

        wrapper.in("gov_dept_id", hospitalService.getAllByDeptId(govDeptId));
        wrapper.ne("status", Const.STATUS_IS_DELETED);

        if (null != request.getOrgNo()) {
            wrapper.like("org_no", request.getOrgNo());
        }

        if (StringUtils.isNotBlank(request.getName())) {
            wrapper.like("name", request.getName());
        }

        if (null != request.getType()) {
            wrapper.eq("type", request.getType());
        }

        if (null != request.getCode()) {
            wrapper.like("city_code", request.getCode())
                    .or()
                    .like("area_code", request.getCode());
        }
        return baseMapper.selectPage(page, wrapper);
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
