package com.wupol.myopia.business.core.parent.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderQueryDTO;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import org.apache.ibatis.annotations.Param;


/**
 * 工单Mapper接口
 *
 * @Author xjl
 * @Date 2022-03-04
 */
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {

    /**
     * 查询工单分页结果
     * @param page
     * @param workOrderQueryDTO
     * @return 分页结果
     */
    IPage<WorkOrderDTO> getByPage(@Param("page") Page<?> page, @Param("workOrderQueryDTO") WorkOrderQueryDTO workOrderQueryDTO);
}
