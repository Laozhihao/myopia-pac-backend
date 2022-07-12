package com.wupol.myopia.business.core.parent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.alibaba.excel.util.CollectionUtils;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.constant.WorkOrderStatusEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderQueryDTO;
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

    /**
     * 获取工单列表分页结果
     * @param pageRequest
     * @param workOrderQueryDTO
     * @return IPage<WorkOrder> {@link IPage}
     */
    public IPage<WorkOrderDTO> getWorkOrderPage(PageRequest pageRequest, WorkOrderQueryDTO workOrderQueryDTO) {
        return this.baseMapper.getByPage(pageRequest.toPage(),workOrderQueryDTO.getName(),workOrderQueryDTO.getIdCardOrPassport(),
                workOrderQueryDTO.getSchoolIds(),workOrderQueryDTO.getStartTime(),workOrderQueryDTO.getEndTime(),workOrderQueryDTO.getStatus());
    }
    /**
     * 工单列表
     * @param createUserId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public List<WorkOrder> findByCreateUserId(Integer createUserId) {
        List<WorkOrder> list = baseMapper.findByCreateUserId(createUserId);
        if (!CollectionUtils.isEmpty(list)){
            //当查看工单列表的时候修改已读状态
            for (WorkOrder workOrder : list){
                if (WorkOrderStatusEnum.PROCESSED.code.equals(workOrder.getStatus()) && workOrder.getViewStatus() == WorkOrder.USER_VIEW_STATUS_UNREAD){
                    workOrder.setViewStatus(WorkOrder.USER_VIEW_STATUS_READ);
                    baseMapper.updateById(workOrder);
                }
            }
        }
        return list;
    }

    /**
     * 根据创建用户ID和状态查询工单
     * @param createUserId 用户ID
     * @param status 状态
     */
    public List<WorkOrder> findByCreateUserIdAndStatus(Integer createUserId,Integer status){
        LambdaQueryWrapper<WorkOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WorkOrder::getCreateUserId,createUserId);
        queryWrapper.eq(WorkOrder::getStatus,status);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 新建工单
     * @param workOrder
     * @param parent
     */
    public void addWorkOrder(WorkOrder workOrder,Parent parent){
        workOrder.setStatus(WorkOrderStatusEnum.UNTREATED.code);
        if (parent != null && StringUtils.isNotBlank(parent.getWxNickname())){
            workOrder.setWxNickname(parent.getWxNickname());
        }
        baseMapper.insert(workOrder);
    }

    /**
     * 工单查看状态
     * @param createUserId
     * @return
     */
    public int workOrderState(Integer createUserId){
      List<WorkOrder> list =  baseMapper.findByCreateUserId(createUserId);
      if (!CollectionUtils.isEmpty(list)){
          //当前用户所提交的工单中如果有一个已处理未查看就返回未读的状态
          for (WorkOrder workOrder: list){
              if (WorkOrderStatusEnum.PROCESSED.code.equals(workOrder.getStatus()) && workOrder.getViewStatus() == WorkOrder.USER_VIEW_STATUS_UNREAD){
                  return WorkOrder.USER_VIEW_STATUS_UNREAD;
              }
          }
      }
      return WorkOrder.USER_VIEW_STATUS_READ;
    }

}
