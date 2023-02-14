package com.wupol.myopia.business.common.utils.constant;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * @Author 钓猫的小鱼
 * @Date 2023/2/14 10:41
 * @Description
 **/
@Getter
public enum DeviceConfigTypes {
    PROVINCIAL_LEVEL(0, "省级配置"),
    SINGLE_POINT(1, "单点配置"),
    VS666(2, "VS666配置"),
    VS666_SINGLE(3, "单点+VS666配置"),
    VS550_25D(4, "VS550（0.25D）配置"),
    VS550_01D(5, "VS550（0.01D）配置");

    public Integer code;
    public String desc;

    DeviceConfigTypes(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
