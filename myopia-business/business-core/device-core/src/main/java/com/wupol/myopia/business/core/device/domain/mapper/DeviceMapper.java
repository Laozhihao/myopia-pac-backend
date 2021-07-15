package com.wupol.myopia.business.core.device.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.query.DeviceQuery;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper接口
 *
 * @Author jacob
 * @Date 2021-06-28
 */
public interface DeviceMapper extends BaseMapper<Device> {

    /**
     * 分页查询（仅支持查询条件为模糊查询）
     *
     * @param page 分页器
     * @param query 查询条件
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.wupol.myopia.business.core.device.domain.model.Device>
     **/
    IPage<Device> getPageByLikeQuery(@Param("page") Page<?> page, @Param("query") DeviceQuery query);
}
