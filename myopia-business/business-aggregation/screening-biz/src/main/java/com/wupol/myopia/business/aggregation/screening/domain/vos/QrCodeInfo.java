package com.wupol.myopia.business.aggregation.screening.domain.vos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class QrCodeInfo {

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 年级
     */
    private String gradeName;

    /**
     * 班级
     */
    private String className;

    /**
     * 二维码内容
     */
    private String qrCodeContent;

    /**
     * 出生年月
     */
    private Date birthday;
}
