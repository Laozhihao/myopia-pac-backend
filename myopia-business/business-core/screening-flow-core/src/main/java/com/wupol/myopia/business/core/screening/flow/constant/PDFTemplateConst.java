package com.wupol.myopia.business.core.screening.flow.constant;

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
    /** 虚拟学生二维码 */
    public final String SCREENING_QRCODE_TEMPLATE_PATH = "/virtualCode.ftl";
}
