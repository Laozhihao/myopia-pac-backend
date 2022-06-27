package com.wupol.myopia.business.common.utils.util;

import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.QrCodeConstant;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QrcodeUtil {

    /**
     * 获取二维码内容
     *
     * @param planId        筛查计划ID
     * @param planStudentId 学生ID
     * @param age           学生年龄
     * @param gender        学生性别
     * @param parentPhone   家长手机号
     * @param idCard        身份证
     * @param type          类型
     * @return 二维码内容
     */
    public static String getQrCodeContent(Integer planId, Integer planStudentId, Integer age, Integer gender,
                                          String parentPhone, String idCard, Integer type) {
        if (CommonConst.EXPORT_SCREENING_QRCODE.equals(type)) {
            return String.format(QrCodeConstant.SCREENING_CODE_QR_CONTENT_FORMAT_RULE, planStudentId);
        } else if (CommonConst.EXPORT_VS666.equals(type)) {
            return setVs666QrCodeRule(planId, planStudentId,
                    age, gender, parentPhone, idCard);
        }
        return String.format(QrCodeConstant.QR_CODE_CONTENT_FORMAT_RULE, planStudentId);
    }


    /**
     * 获取VS666格式所需要的二维码
     * @param planId 筛查计划ID
     * @param planStudentId 学生ID
     * @param age 学生年龄
     * @param gender 学生性别
     * @param parentPhone 家长手机号
     * @param idCard 身份证
     * @return
     */
    public static String setVs666QrCodeRule(Integer planId, Integer planStudentId,
                                            Integer age, Integer gender,
                                            String parentPhone, String idCard) {
        return String.format(QrCodeConstant.VS666_QR_CODE_CONTENT_FORMAT_RULE,
                String.format(QrCodeConstant.GENERATE_VS666_ID, planId, planStudentId),
                planStudentId,
                GenderEnum.getEnGenderDesc(gender),
                age,
                StringUtils.getDefaultIfBlank(parentPhone, "null"),
                "null",
                "null",
                StringUtils.getDefaultIfBlank(idCard, "null"));
    }
}
