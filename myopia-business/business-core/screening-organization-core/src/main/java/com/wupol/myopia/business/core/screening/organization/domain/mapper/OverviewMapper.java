package com.wupol.myopia.business.core.screening.organization.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OverviewDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.business.core.screening.organization.domain.query.OverviewQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 总览机构信息表Mapper接口
 *
 * @Author wulizhou
 * @Date 2022-02-17
 */
public interface OverviewMapper extends BaseMapper<Overview> {

    /**
     * 获取指定名称且ID不为#{id}的数据
     * @param name
     * @param id
     * @return
     */
    List<Overview> getByNameNeId(@Param("name") String name, @Param("id") Integer id);

    /**
     * 获取总览机构列表
     * @param page
     * @param query
     * @return
     */
    IPage<OverviewDTO> getOverviewListByCondition(@Param("page") Page<?> page, @Param("query") OverviewQuery query);

    /**
     * 获取状态未更新的总览机构，即：<br/>
     * 已过期未设置过停用状态，已开始未设置启用状态
     * @param date
     * @return
     */
    List<Overview> getByCooperationTimeAndStatus(@Param("date") Date date);

}
