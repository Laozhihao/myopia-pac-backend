package com.wupol.myopia.business.core.parent.service;

import com.alibaba.excel.util.CollectionUtils;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.parent.domain.mapper.WorkOrderMapper;
import com.wupol.myopia.business.core.parent.domain.model.Parent;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工单业务类
 * @Author xjl
 * @Date 2022-03-04
 */
@Service
public class WorkOrderService extends BaseService<WorkOrderMapper, WorkOrder> {

    @Transactional(rollbackFor = Exception.class)
    public List<WorkOrder> findByCreateUserId(Integer createUserId) {
        List<WorkOrder> list = baseMapper.findByCreateUserId(createUserId);
        if (CollectionUtils.isEmpty(list)){
            for (WorkOrder workOrder : list){
                if (workOrder.getStatus() != 1 && workOrder.getViewStatus() == WorkOrder.USER_VIEW_STATUS_UNREAD){
                    workOrder.setViewStatus(WorkOrder.USER_VIEW_STATUS_READ);
                    baseMapper.updateById(workOrder);
                }
            }
        }
        return list;
    }

    public void addWorkOrder (WorkOrder workOrder,Parent parent){
        workOrder.setStatus(1);
        if (parent != null && StringUtils.isNotBlank(parent.getWxNickname())){
            workOrder.setWxNickname(parent.getWxNickname());
        }
        baseMapper.insert(workOrder);
    }

    public int workOrderState(Integer createUserId){
      List<WorkOrder> list =  baseMapper.findByCreateUserId(createUserId);
      if (CollectionUtils.isEmpty(list)){
          for (WorkOrder workOrder: list){
              if (workOrder.getStatus() != 1 && workOrder.getViewStatus() == WorkOrder.USER_VIEW_STATUS_UNREAD){
                  return WorkOrder.USER_VIEW_STATUS_UNREAD;
              }
          }
      }
      return WorkOrder.USER_VIEW_STATUS_READ;
    }

}
