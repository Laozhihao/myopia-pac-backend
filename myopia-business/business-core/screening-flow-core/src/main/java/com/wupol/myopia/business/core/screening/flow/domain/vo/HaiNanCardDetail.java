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
    private IntraocularPressureDataDO intraocularPressureData;

    /**
     * 盲及视力损害分类
     */
    private VisualLossLevelDataDO visualLossLevelData;

    /**
     * 备注
     */
    private String remark;

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
     * 左眼近视信息
     */
    private Integer leftMyopiaInfo;

    /**
     * 右眼近视信息
     */
    private Integer rightMyopiaInfo;

    /**
     * 左眼远视信息
     */
    private Integer leftFarsightednessInfo;

    /**
     * 右眼远视信息
     */
    private Integer rightFarsightednessInfo;

    /**
     * 散光信息
     */
    private Boolean leftAstigmatismInfo;

    /**
     * 散光信息
     */
    private Boolean rightAstigmatismInfo;
}
