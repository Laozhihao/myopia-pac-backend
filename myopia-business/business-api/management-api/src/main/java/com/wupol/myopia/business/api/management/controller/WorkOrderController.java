package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.service.WorkOrderBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderQueryDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderRequestDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 工单Controller
 * @Author xjl
 * @Date 2022/3/7
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/workOrder")
public class WorkOrderController {

    @Autowired
    private WorkOrderBizService workOrderBizService;

    /**
     * 获取工单列表
     * @param pageRequest 分页查询
     * @param workOrderQueryDTO 请求条件
     * @return 工单列表
     */
    @GetMapping("/list")
    public IPage<WorkOrderDTO> getWorkOrderPage(PageRequest pageRequest, WorkOrderQueryDTO workOrderQueryDTO){

        return workOrderBizService.getWorkOrderPage(pageRequest,workOrderQueryDTO);

    }

    /**
     * 工单处理
     * @param workOrderRequestDTO
     * @return
     */
    @PutMapping("/dispose")
    public boolean disposeOfWordOrder(@RequestBody @Valid WorkOrderRequestDTO workOrderRequestDTO){

        if (StringUtils.isAllBlank(workOrderRequestDTO.getIdCard(),workOrderRequestDTO.getPassport())){
            throw new BusinessException("身份证和护照不可全部为空");
        }
        workOrderBizService.dispose(workOrderRequestDTO);
        return true;
    }


}
