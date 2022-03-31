package com.wupol.myopia.business.core.parent.domain.dto;

import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 工单DTO
 * @Author xjl
 * @Date 2022/3/7
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class WorkOrderDTO extends WorkOrder {

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 学校名称
     */
    private String schoolName;


}
