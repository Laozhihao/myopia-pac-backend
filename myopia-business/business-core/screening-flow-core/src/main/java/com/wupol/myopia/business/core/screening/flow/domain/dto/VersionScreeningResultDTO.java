package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 学生筛查结果详情
 * @author tastyb
 */

@Data
@Accessors(chain = true)
public class VersionScreeningResultDTO extends VisionScreeningResult {

    /**
     * 学生性别
     */
    private Integer gender;

    /**
     * 复测情况
     */
    private ReScreenDTO rescreening;

    /**
     * 龋齿，龋失补
     */
    private SaprodontiaDataDTO saprodontiaDataDTO;

    /**
     * 等效球镜(左眼)
     */
    private BigDecimal leftSE;

    /**
     * 等效球镜(右眼)
     */
    private BigDecimal rightSE;


    public static OtherEyeDiseasesDO otherEyeDiseasesDOIsNull(OtherEyeDiseasesDO otherEyeDiseases){
        if (otherEyeDiseases == null){
            return new OtherEyeDiseasesDO();
        }else {
            return  otherEyeDiseases;
        }
    }

    public static SaprodontiaDataDO saprodontiaDataDOIsNull(SaprodontiaDataDO saprodontiaDataDO){
        if (saprodontiaDataDO == null){
            return new SaprodontiaDataDO();
        }else {
            return  saprodontiaDataDO;
        }
    }

    public static SpineDataDO spineDataDOIsNull(SpineDataDO spineDataDO){
        if (spineDataDO == null){
            return new SpineDataDO();
        }else {
            return  spineDataDO;
        }
    }

    public static BloodPressureDataDO bloodPressureDataDOIsNull(BloodPressureDataDO bloodPressureDataDO){
        if (bloodPressureDataDO == null){
            return new BloodPressureDataDO();
        }else {
            return  bloodPressureDataDO;
        }
    }

    public static DiseasesHistoryDO diseasesHistoryDOIsNull(DiseasesHistoryDO diseasesHistoryDO){
        if (diseasesHistoryDO == null){
            return new DiseasesHistoryDO();
        }else {
            return  diseasesHistoryDO;
        }
    }

    public static PrivacyDataDO privacyDataDOIsNull(PrivacyDataDO privacyDataDO){
        if (privacyDataDO == null){
            return new PrivacyDataDO();
        }else {
            return  privacyDataDO;
        }
    }

    public static DeviationDO deviationDOIsNull(DeviationDO deviationDO){
        if (deviationDO == null){
            return new DeviationDO();
        }else {
            return  deviationDO;
        }
    }

    public static ReScreenDTO reScreenDTOIsNull(ReScreenDTO reScreenDTO){
        if (reScreenDTO == null){
            return new ReScreenDTO();
        }else {
            return  reScreenDTO;
        }
    }

}
