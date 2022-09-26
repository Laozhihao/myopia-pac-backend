package com.wupol.myopia.business.aggregation.screening.domain.vos;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 学生筛查详情
 *
 * @author hang.yuan 2022/9/13 18:14
 */
@Data
public class StudentScreeningDetailVO implements Serializable {

    /**
     * 戴镜类型
     */
    private Integer glassesType;

    /**
     * 视力数据
     */
    private List<VisionDataVO> visionData;
    /**
     * 电脑验光
     */
    private List<ComputerOptometryDataVO> computerOptometryData;
    /**
     * 其它
     */
    private List<OtherDataVO> otherData;
    /**
     * 身高体重
     */
    private HeightAndWeightDataVO heightAndWeightData;
    /**
     * 生物测量
     */
    private List<BiometricDataVO> biometricData;
    /**
     * 小瞳验光
     */
    private List<PupilOptometryDataVO> pupilOptometryData;
    /**
     * 眼压
     */
    private List<EyePressureDataVO> eyePressureData;


}
