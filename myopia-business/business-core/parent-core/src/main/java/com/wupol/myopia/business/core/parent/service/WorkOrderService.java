package com.wupol.myopia.business.core.parent.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.parent.domain.mapper.WorkOrderMapper;
import com.wupol.myopia.business.core.parent.domain.model.Parent;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 工单业务类
 * @Author xjl
 * @Date 2022-03-04
 */
@Service
public class WorkOrderService extends BaseService<WorkOrderMapper, WorkOrder> {

    public List<WorkOrder> findByUserId(Integer userId) {
        List<WorkOrder> list = baseMapper.findByUserId(userId);
        if (list.size()>0){
            for (WorkOrder workOrder : list){
                if (workOrder.getViewStatus() == WorkOrder.USER_VIEW_STATUS_UNREAD){
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

    public int workOrderState(Integer userId){
      List<WorkOrder> list =  baseMapper.findByUserId(userId);
      if (list.size()>0){
          for (WorkOrder workOrder: list){
              if (workOrder.getViewStatus() == WorkOrder.USER_VIEW_STATUS_UNREAD){
                  return WorkOrder.USER_VIEW_STATUS_UNREAD;
              }
          }
      }
      return WorkOrder.USER_VIEW_STATUS_READ;
    }

}
