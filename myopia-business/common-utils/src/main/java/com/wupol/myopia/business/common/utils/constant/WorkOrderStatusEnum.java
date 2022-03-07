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
    PROCESSED(0,"已处理","已解决，请重新操作"),
    UNTREATED(1,"未处理","已收到反馈，正在处理中，请稍后"),
    ;
    /**
     * 工单状态编码
     */
    public final Integer code;

    /**
     * 工单状态描述
     */
    public final String desc;

    /**
     * 工单状态内容
     */
    public final String content;


    WorkOrderStatusEnum(Integer code, String desc, String content) {
        this.code = code;
        this.desc = desc;
        this.content = content;
    }

    public static WorkOrderStatusEnum get(Integer code){
        return Arrays.stream(WorkOrderStatusEnum.values())
                .filter(item->item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code){
        if (Objects.isNull(code)) {
            return StringUtils.EMPTY;
        }
        WorkOrderStatusEnum workOrderStatus = get(code);
        if (Objects.isNull(workOrderStatus)) {
            return StringUtils.EMPTY;
        }
        return workOrderStatus.getDesc();
    }

    public static String getContentByCode(Integer code){
        if (Objects.isNull(code)) {
            return StringUtils.EMPTY;
        }
        WorkOrderStatusEnum workOrderStatus = get(code);
        if (Objects.isNull(workOrderStatus)) {
            return StringUtils.EMPTY;
        }
        return workOrderStatus.getContent();
    }
}
