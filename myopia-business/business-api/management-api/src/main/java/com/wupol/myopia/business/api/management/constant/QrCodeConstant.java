package com.wupol.myopia.business.api.management.constant;

import lombok.experimental.UtilityClass;

/**
 * 二维码常量类
 *
 * @author Simple4H
 */
@UtilityClass
public class QrCodeConstant {

    /**
     * 二维码生成规则: "SA@" + 32位"学生id",不足使用0补充
     * 如studentId = 123 ,则生成的结果是:
     * SA@0000000000000000000123
     * 如studentId = 1 ,则生成的结果是:
     * SA@0000000000000000000001
     */
    public String QR_CODE_CONTENT_FORMAT_RULE = "SA@%032d";

    /**
     * [ID,name,sex,age,phone,0,schoolName,gradeName&className,idCard]
     */
    public String VS666_QR_CODE_CONTENT_FORMAT_RULE = "[%s,%s,%s,%s,%s,0,%s,%s,%s]";

    /**
     * 生成VS666筛查专属二维码Id
     */
    public String GENERATE_VS666_ID = "VS@%s_%s";
}
