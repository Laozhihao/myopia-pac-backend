package com.wupol.myopia.business.common.utils.util;

import com.wupol.framework.core.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * @Author 钓猫的小鱼
 * @Date 2023/2/14 9:35
 * @Description VS550 设备数据格式化
 **/
public class VS550Util {
    /**
     * 计算等效球镜
     * 根据：MedicalRecordService中的 computerSE 改编
     *
     * @param ds 球镜
     * @param dc 柱镜
     * @return 等效球镜
     */
    public static Double computerSE(Double ds, Double dc) {
        if (StringUtils.allHasLength(ds.toString(), dc.toString())) {
            return new BigDecimal(ds).add(new BigDecimal(dc).multiply(new BigDecimal("0.5")))
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        return 0.00;
    }
}
