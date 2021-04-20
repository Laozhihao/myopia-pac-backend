package com.wupol.myopia.business.management.constant;

import lombok.experimental.UtilityClass;

/**
 * 模板路径
 *
 * @author Alix
 */
@UtilityClass
public class PDFTemplateConst {
    /** 筛查学生二维码 */
    public final String QRCODE_TEMPLATE_PATH = "/screeningStudentQrCode.ftl";
    /** 筛查学生告知书 */
    public final String NOTICE_TEMPLATE_PATH = "/screeningStudentNotice.ftl";
}
