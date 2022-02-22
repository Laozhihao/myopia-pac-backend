package com.wupol.myopia.business.core.screening.organization.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OverviewDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.business.core.screening.organization.domain.query.OverviewQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 总览机构信息表Mapper接口
 *
 * @Author wulizhou
 * @Date 2022-02-17
 */
public interface OverviewMapper extends BaseMapper<Overview> {

    List<Overview> getByNameNeId(@Param("name") String name, @Param("id") Integer id);

    IPage<OverviewDTO> getOverviewListByCondition(@Param("page") Page<?> page, @Param("govDeptId") List<Integer> govDeptId,
                                                  @Param("query") OverviewQuery query);

}
