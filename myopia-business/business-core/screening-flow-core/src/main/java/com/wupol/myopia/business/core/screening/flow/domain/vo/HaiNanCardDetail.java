package com.wupol.myopia.business.core.screening.flow.domain.vo;

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
public class HaiNanCardDetail {

    /**
     * 视力检查
     */
    private VisionDataDO visionDataDO;

    /**
     * 33cm眼位
     */
    private OcularInspectionDataDO ocularInspectionData;

    /**
     * 曲光度
     */
    private ComputerOptometryDO computerOptometry;

    /**
     * 小瞳验光
     */
    private PupilOptometryDataDO pupilOptometryData;

    /**
     * 生物测量
     */
    private BiometricDataDO biometricData;

    /**
     * 眼压
     */
    private EyePressureDataDO eyePressureData;

    /**
     * 盲及视力损害分类
     */
    private VisualLossLevelDataDO visualLossLevelData;

    /**
     * 备注
     */
    private String remark;

    /**
     * 筛查结果--全身疾病在眼部的表现
     */
    private String systemicDiseaseSymptom;

    /**
     * 筛查结果--裂隙灯检查
     */
    private SlitLampDataDO slitLampData;

    /**
     * 是否正常
     */
    private Boolean isNormal;

    /**
     * 其他眼病
     */
    private List<String> otherEyeDiseases;

    /**
     * 是否屈光不正
     */
    private Boolean isRefractiveError;

    /**
     * 筛查结果--眼底
     */
    private FundusDataDO fundusData;

    /**
     * 左眼近视级别 1-轻度 2-中度 3-高度
     */
    private Integer leftMyopiaInfo;

    /**
     * 右眼近视级别 1-轻度 2-中度 3-高度
     */
    private Integer rightMyopiaInfo;

    /**
     * 左眼远视级别 1-轻度 2-中度 3-高度
     */
    private Integer leftFarsightednessInfo;

    /**
     * 右眼远视级别 1-轻度 2-中度 3-高度
     */
    private Integer rightFarsightednessInfo;

    /**
     * 左眼是否散光
     */
    private Boolean leftAstigmatismInfo;

    /**
     * 右眼是否散光
     */
    private Boolean rightAstigmatismInfo;

    /**
     * 斜视
     */
    private List<String> squint;

    /**
     * 签名图片访问地址
     */
    private String signPicUrl;

    /**
     * 五种特殊眼病
     */
    private List<String> eyeDiseases;

}
