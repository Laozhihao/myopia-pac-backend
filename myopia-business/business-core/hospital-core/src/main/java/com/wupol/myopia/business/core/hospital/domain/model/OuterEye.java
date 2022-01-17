package com.wupol.myopia.business.core.hospital.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 眼外观
 * @Author wulizhou
 * @Date 2022/1/6 19:25
 */
@Data
@Accessors(chain = true)
public class OuterEye {

    private List<BaseValue> rightEyelidList;
    private List<BaseValue> leftEyelidList;
    private List<BaseValue> rightConjunctivaList;
    private List<BaseValue> leftConjunctivaList;
    private List<BaseValue> rightEyeballList;
    private List<BaseValue> leftEyeballList;
    private List<BaseValue> rightCornealList;
    private List<BaseValue> leftCornealList;
    private List<BaseValue> rightPupilList;
    private List<BaseValue> leftPupilList;
    private List<BaseValue> rightScleraList;
    private List<BaseValue> leftScleraList;
    private Boolean isAbnormal;
    private Integer studentId;
    private Integer doctorId;
    private String eyelidAbnormalContent;
    private String conjunctivaAbnormalContent;
    private String eyeballAbnormalContent;
    private String cornealAbnormalContent;
    private String pupilAbnormalContent;
    private String scleraAbnormalContent;

}
