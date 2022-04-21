package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/04/21/11:30
 * @Description: 常见病扩展类
 */
@Data
public class CommonDiseasesDTO {
    /**
     * 筛查结果--龋齿
     */
    private SaprodontiaDataDO saprodontiaData;

    /**
     * 筛查结果--脊柱
     */
    private SpineDataDO spineData;

    /**
     * 筛查结果--血压
     */
    private BloodPressureDataDO bloodPressureData;

    /**
     * 筛查结果--疾病史(汉字)
     */
    private DiseasesHistoryDO diseasesHistoryData;
    /**
     * 筛查结果--隐私项
     */
    private PrivacyDataDO privacyData;

    /**
     * 筛查结果--全身疾病在眼部的表现
     */
    private String systemicDiseaseSymptom;
    /**
     * 筛查结果--身高体重
     */
    private HeightAndWeightDataDO heightAndWeightData;
}
