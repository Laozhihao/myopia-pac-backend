package com.wupol.myopia.business.api.management.constant;

import java.math.BigDecimal;

/**
 * 报告常量类型
 *
 * @author Simple4H
 */
public class ReportConst {

    /**
     * 转诊单
     */
    public static final String REFERRAL_PDF_URL = "%s/?referralId=%s&isHospital=%s&token=%s";

    /**
     * 转诊单
     */
    public static final String TYPE_REFERRAL = "referral";

    /**
     * 检查记录表
     */
    public static final String EXAMINE_PDF_URL = "%s/?examineId=%s&isHospital=%s&token=%s";

    /**
     * 检查记录表
     */
    public static final String TYPE_EXAMINE = "examine";

    /**
     * 回执单
     */
    public static final String RECEIPT_PDF_URL = "%s/?receiptId=%s&isHospital=%s&token=%s";

    /**
     * 回执单
     */
    public static final String TYPE_RECEIPT = "receipt";

    /**
     * 零
     */
    public static final Integer ZERO = 0;
    /**
     * 零（占比）
     */
    public static final BigDecimal ZERO_BIG_DECIMAL = new BigDecimal("0.00");
    public static final String ZERO_RATIO_STR = "0.00%";
    public static final String ZERO_STR = "0.00";

    /**
     * 文字
     */
    public static final  String hypertension ="高血压";
    public static final  String anemia ="贫血";
    public static final  String diabetes ="糖尿病";
    public static final  String allergicAsthma ="过敏性哮喘";
    public static final  String physicalDisability ="身体残疾";

}
