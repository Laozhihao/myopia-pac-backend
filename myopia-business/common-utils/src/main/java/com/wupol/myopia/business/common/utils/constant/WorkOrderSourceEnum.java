package com.wupol.myopia.business.common.utils.constant;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 工单来源
 * @author xjl
 * @Date 2022/3/7
 */
@Getter
public enum WorkOrderSourceEnum {
    BIND_PAGE(0,"绑定页面"),
    ARCHIVES_PAGE(1,"档案页面");
    /**
     * 工单来源编码
     */
    public final Integer code;

    /**
     * 工单来源描述
     */
    public final String desc;

    WorkOrderSourceEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WorkOrderSourceEnum getByCode(Integer code){
        return Arrays.stream(WorkOrderSourceEnum.values())
                .filter(item->item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code){
        return Optional.ofNullable(code)
                .map(WorkOrderSourceEnum::getByCode)
                .map(WorkOrderSourceEnum::getDesc)
                .orElse(StrUtil.EMPTY);
    }

}
