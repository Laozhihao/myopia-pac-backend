package com.wupol.myopia.business.api.management.constant;

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
}
