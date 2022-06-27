package com.wupol.myopia.business.core.screening.flow.domain.vo;

import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 常见病档案卡
 *
 * @Author HaoHao
 * @Date 2022/4/15
 **/
@Accessors(chain = true)
@Data
public class CommonDiseaseArchiveCard {
    /**
     * 学校信息
     */
    private CardInfoVO studentInfo;

    /**
     * 常见病ID信息
     */
    private StudentCommonDiseaseIdInfo commonDiseaseIdInfo;

    /**
     * 疾病史
     */
    private List<String> diseasesHistoryData;

    /**
     * 视力数据
     */
    private VisionDataDO visionData;

    /**
     * 屈光数据
     */
    private ComputerOptometryDO computerOptometryData;

    /**
     * 龋齿数据
     */
    private SaprodontiaData saprodontiaData;

    /**
     * 脊柱数据
     */
    private SpineDataDO spineData;

    /**
     * 身高体重数据
     */
    private HeightAndWeightDataDO heightAndWeightData;

    /**
     * 血压数据
     */
    private BloodPressureDataDO bloodPressureData;

    /**
     * 隐私数据
     */
    private PrivacyDataDO privacyData;

}
