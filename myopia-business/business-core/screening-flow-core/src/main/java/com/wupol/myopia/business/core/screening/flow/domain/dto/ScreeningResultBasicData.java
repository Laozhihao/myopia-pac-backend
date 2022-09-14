package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dos.AbstractDiagnosisResult;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @Description 筛查结果基本数据
 * @Date 2021/1/26 1:04
 * @Author by Jacob
 */
@Accessors(chain = true)
@Data
public abstract class ScreeningResultBasicData implements ScreeningDataInterface {
    /**
     * 学校id
     */
    private String schoolId;
    /**
     * 机构id
     */
    private Integer deptId;
    /**
     * 用户id
     */
    @JsonProperty("userId")
    private Integer createUserId;
    /**
     * 学生id
     */
    @JsonProperty("studentId")
    private String planStudentId;
    /**
     * 默认是初筛，app设计如此
     */
    private Integer isState = 0;
    /**
     * 初步诊断结果：0-正常、1-（疑似）异常
     */
    private Integer diagnosis;
    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;


    public Integer getPlanStudentId() {
        return stringToInteger(planStudentId);
    }

    public Integer getSchoolId() {
        return stringToInteger(schoolId);
    }

    public Integer getDeptId() {
        if (Objects.isNull(deptId)) {
            deptId = CurrentUserUtil.getCurrentUser().getOrgId();
        }
        return deptId;
    }

    public Integer getCreateUserId() {
        if (Objects.isNull(createUserId)) {
            createUserId = CurrentUserUtil.getCurrentUser().getId();
        }
        return createUserId;
    }

    private Integer stringToInteger(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Double doubleData = Double.valueOf(value);
        return (int) Math.ceil(doubleData);
    }

    public Integer getIsState() {
        if (Objects.isNull(isState)) {
            isState = 0;
        }
        return isState;
    }

    public void setIsState(Integer isState) {
        this.isState = isState;
    }

    /** 获取数据的类型，用于区分哪种类型的检查 */
    public abstract String getDataType();

    /** 传进来的时间是否为更加新的时间 */
    public boolean isNewerUpdateTime(String dataType, VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult == null) return false;
        AbstractDiagnosisResult abstractDiagnosisResult = null;

        switch (dataType) {
            case ScreeningConstant.SCREENING_DATA_TYPE_VISION: abstractDiagnosisResult = visionScreeningResult.getVisionData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_COMPUTER_OPTOMETRY: abstractDiagnosisResult = visionScreeningResult.getComputerOptometry(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_MULTI_CHECK:
            case ScreeningConstant.SCREENING_DATA_TYPE_FUNDUS: abstractDiagnosisResult = visionScreeningResult.getFundusData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_OCULAR_INSPECTION: abstractDiagnosisResult = visionScreeningResult.getOcularInspectionData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_SLIT_LAMP: abstractDiagnosisResult = visionScreeningResult.getSlitLampData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_VISUAL_LOSS_LEVEL: abstractDiagnosisResult = visionScreeningResult.getVisualLossLevelData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_BIOMETRIC: abstractDiagnosisResult = visionScreeningResult.getBiometricData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_PUPIL_OPTOMETRY: abstractDiagnosisResult = visionScreeningResult.getPupilOptometryData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_EYE_PRESSURE: abstractDiagnosisResult = visionScreeningResult.getEyePressureData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_OTHER_EYE_DISEASE: abstractDiagnosisResult = visionScreeningResult.getOtherEyeDiseases(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_HEIGHT_WEIGHT: abstractDiagnosisResult = visionScreeningResult.getHeightAndWeightData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_DEVIATION: abstractDiagnosisResult = visionScreeningResult.getDeviationData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_SAPRODONTIA: abstractDiagnosisResult = visionScreeningResult.getSaprodontiaData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_SPINE: abstractDiagnosisResult = visionScreeningResult.getSpineData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_BLOOD_PRESSURE: abstractDiagnosisResult = visionScreeningResult.getBloodPressureData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_DISEASES_HISTORY: abstractDiagnosisResult = visionScreeningResult.getDiseasesHistoryData(); break;
            case ScreeningConstant.SCREENING_DATA_TYPE_PRIVACY: abstractDiagnosisResult = visionScreeningResult.getPrivacyData(); break;
            default: return false;
        }

        if (Objects.isNull(abstractDiagnosisResult.getUpdateTime())) {return true;}
        if (Objects.isNull(updateTime)) {return false;}
        return updateTime > abstractDiagnosisResult.getUpdateTime();


    }
}
