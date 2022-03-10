package com.wupol.myopia.business.core.parent.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工单Mapper接口
 *
 * @Author xjl
 * @Date 2022-03-04
 */
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {


    List<WorkOrder> findByUserId(@Param("userId") Integer userId);
}
