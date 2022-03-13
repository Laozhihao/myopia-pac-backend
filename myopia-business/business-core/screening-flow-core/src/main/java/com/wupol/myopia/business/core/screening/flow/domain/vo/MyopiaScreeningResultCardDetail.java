package com.wupol.myopia.business.core.screening.flow.domain.vo;

import com.wupol.myopia.business.common.utils.constant.HyperopiaLevelEnum;
import com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 海南省学生眼疾病筛查单
 *
 * @author Simple4H
 */
@Getter
@Setter
public class MyopiaScreeningResultCardDetail {

    /**
     * 筛查结果--视力检查结果
     */
    private VisionDataDO visionData;

    /**
     * 筛查结果--电脑验光
     */
    private ComputerOptometryDO computerOptometry;

    /**
     * 扩展年龄
     */
    private String ageInfo;
    /**
     * 医生签名
     */
    private String doctorSignature;

    /**
     * 签名图片访问地址（视力筛查）
     */
    private String visionSignPicUrl;

    /**
     * 签名图片访问地址(电脑验光)
     */
    private String computerSignPicUrl;
}
