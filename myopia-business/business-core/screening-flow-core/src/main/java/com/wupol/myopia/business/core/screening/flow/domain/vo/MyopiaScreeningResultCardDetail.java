package com.wupol.myopia.business.core.screening.flow.domain.vo;

import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import lombok.Getter;
import lombok.Setter;

/**
 * 学生档案卡-近视筛查结果记录表
 *
 * @author 钓猫的小鱼
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
     * 班主任签名
     */
    private String teacherSignature;

    /**
     * 签名图片访问地址（视力筛查）
     */
    private String visionSignPicUrl;

    /**
     * 签名图片访问地址(电脑验光)
     */
    private String computerSignPicUrl;
}
