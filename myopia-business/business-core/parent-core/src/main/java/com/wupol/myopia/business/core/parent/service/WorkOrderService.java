package com.wupol.myopia.business.core.parent.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.parent.domain.mapper.WorkOrderMapper;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工单业务类
 * @Author xjl
 * @Date 2022-03-04
 */
@Service
public class WorkOrderService extends BaseService<WorkOrderMapper, WorkOrder> {

    public List<WorkOrder> findByUserId(Integer userId) {
        return baseMapper.findByUserId(userId);
    }


}
