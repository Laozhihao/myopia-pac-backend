package com.wupol.myopia.business.common.utils.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * 工单状态
 * @author xjl
 * @Date 2022/3/7
 */
@Getter
public enum WorkOrderStatusEnum {
    PROCESSED(0,"已处理"),
    UNTREATED(1,"未处理");
    /**
     * 工单状态编码
     */
    public final Integer code;

    /**
     * 工单状态描述
     */
    public final String desc;

    WorkOrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WorkOrderStatusEnum getByCode(Integer code){
        return Arrays.stream(WorkOrderStatusEnum.values())
                .filter(item->item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code){
        if (Objects.isNull(code)) {
            return StringUtils.EMPTY;
        }
        WorkOrderStatusEnum workOrderStatus = getByCode(code);
        if (Objects.isNull(workOrderStatus)) {
            return StringUtils.EMPTY;
        }
        return workOrderStatus.getDesc();
    }

}
