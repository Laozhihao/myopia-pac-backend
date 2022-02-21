package com.wupol.myopia.business.core.screening.organization.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.OverviewScreeningOrganization;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 总览机构筛查机构关联表Mapper接口
 *
 * @Author wulizhou
 * @Date 2022-02-17
 */
public interface OverviewScreeningOrganizationMapper extends BaseMapper<OverviewScreeningOrganization> {

    /**
     * 批量插入总览机构筛查机构绑定信息
     * @param overviewId
     * @param screeningOrganizationIds
     * @return
     */
    int batchSave(@Param("overviewId") Integer overviewId, @Param("screeningOrganizationIds") List<Integer> screeningOrganizationIds);

}
