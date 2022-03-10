package com.wupol.myopia.business.api.management.controller;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.service.WorkOrderBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderQueryDTO;
import com.wupol.myopia.business.core.parent.domain.dto.WorkOrderRequestDTO;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import com.wupol.myopia.business.core.parent.service.WorkOrderService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import org.ehcache.core.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 工单Controller
 * @Author xjl
 * @Date 2022/3/7
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/workTable")
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
    public IPage<WorkOrderDTO> getWorkOrderList(PageRequest pageRequest, WorkOrderQueryDTO workOrderQueryDTO){

        return workOrderBizService.getWorkOrderList(pageRequest,workOrderQueryDTO);

    }

    @PutMapping("/dispose")
    public boolean disposeOfWordOrder(@RequestBody @Valid WorkOrderRequestDTO workOrderRequestDTO){

        if (StringUtils.isEmpty(workOrderRequestDTO.getIdCard())&&StringUtils.isEmpty(workOrderRequestDTO.getPassport())){
            throw new BusinessException("身份证和护照不可全部为空");
        }

        return workOrderBizService.disposeOfWordOrder(workOrderRequestDTO);
    }


}
