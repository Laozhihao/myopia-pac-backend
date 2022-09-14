package com.wupol.myopia.business.api.school.management.domain.vo;

import lombok.Data;

/**
 * 学生筛查详情
 *
 * @author hang.yuan 2022/9/13 18:14
 */
@Data
public class StudentScreeningDetailVO {

    /**
     * 视力数据
     */
    private VisionDataVO visionData;
    /**
     * 电脑验光
     */
    private ComputerOptometryDataVO computerOptometryData;
    /**
     * 其它
     */
    private OtherDataVO otherData;
    /**
     * 身高体重
     */
    private HeightAndWeightDataVO heightAndWeightData;
    /**
     * 生物测量
     */
    private BiometricDataVO biometricData;
    /**
     * 小瞳验光
     */
    private PupilOptometryDataVO pupilOptometryData;
    /**
     * 眼压
     */
    private EyePressureDataVO eyePressureData;









}
