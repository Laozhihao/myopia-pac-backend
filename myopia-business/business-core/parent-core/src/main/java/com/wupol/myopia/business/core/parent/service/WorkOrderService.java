package com.wupol.myopia.business.core.parent.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderQueryDTO;
import com.wupol.myopia.business.core.parent.domain.mapper.WorkOrderMapper;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import org.springframework.stereotype.Service;

/**
 * 工单业务类
 * @Author xjl
 * @Date 2022-03-04
 */
@Service
public class WorkOrderService extends BaseService<WorkOrderMapper, WorkOrder> {

    /**
     * 获取工单列表
     * @param pageRequest
     * @param workOrderQueryDTO
     * @return IPage<WorkOrder> {@link IPage}
     */
    public IPage<WorkOrderDTO> getWorkOrderLists(PageRequest pageRequest, WorkOrderQueryDTO workOrderQueryDTO) {
        return this.baseMapper.getByPage(pageRequest.toPage(),workOrderQueryDTO.getName(),workOrderQueryDTO.getIdCardOrPassport(),
                workOrderQueryDTO.getSchoolIds(),workOrderQueryDTO.getStartTime(),workOrderQueryDTO.getEndTime(),workOrderQueryDTO.getStatus());
    }
}
