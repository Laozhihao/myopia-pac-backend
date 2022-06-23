package com.wupol.myopia.business.core.screening.flow.constant;


import lombok.experimental.UtilityClass;

/**
 * @Author  钓猫的小鱼
 * @Date  2022/4/15 16:39
 * @Email: shuailong.wu@vistel.cn
 * @Des: 复测工具类参数
 */
@UtilityClass
public class ReScreenConstant {
    /**
     *  复测项(常见病-佩戴眼镜)
     */
    public static final Integer COMMON_RESCREEN_IS_GLASS_NUM = 8;
    /**
     *  复测项(常见病-没有佩戴眼镜)
     */
    public static final Integer COMMON_RESCREEN_NOT_GLASS_NUM = 6;
    /**
     *  复测项(视力-佩戴眼镜)
     */
    public static final Integer VISION_RESCREEN_IS_GLASS_NUM = 6;
    /**
     *  复测项（视力-没有佩戴眼镜）
     */
    public static final Integer VISION_RESCREEN_NOT_GLASS_NUM = 4;
    /**
     * 视力验光误差标准（行）
     */
    public static final String VISION_DEVIATION = "0.1";
    /**
     * 电脑验光误差标准
     */
    public static final String COMPUTEROPTOMETRY_DEVIATION = "0.5";
    /**
     * 等效球镜误差标准（D）
     */
    public static final String SE_DEVIATION = "0.5";
    /**
     * 体重误差标准（kg）
     */
    public static final String HEIGHT_DEVIATION = "0.1";
    /**
     * 升高误差标准（cm）
     */
    public static final String WEIGHT_DEVIATION = "0.5";


}
