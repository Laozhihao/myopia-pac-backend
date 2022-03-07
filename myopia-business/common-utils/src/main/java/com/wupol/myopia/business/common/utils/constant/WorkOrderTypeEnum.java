package com.wupol.myopia.business.common.utils.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * 工单类型
 * @Author xjl
 * @Date 2022/3/7
 */
@Getter
public enum WorkOrderTypeEnum {
    STUDENT_INFO(1,"学生信息");

    /**
     * 工单类型编码
     */
    public final Integer code;

    /**
     * 工单类型描述
     */
    public final String desc;

    WorkOrderTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WorkOrderTypeEnum get(Integer code){
        return Arrays.stream(WorkOrderTypeEnum.values())
                .filter(item->item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code){
        if (Objects.isNull(code)) {
            return StringUtils.EMPTY;
        }
        WorkOrderTypeEnum workOrderType = get(code);
        if (Objects.isNull(workOrderType)) {
            return StringUtils.EMPTY;
        }
        return workOrderType.getDesc();
    }
}
