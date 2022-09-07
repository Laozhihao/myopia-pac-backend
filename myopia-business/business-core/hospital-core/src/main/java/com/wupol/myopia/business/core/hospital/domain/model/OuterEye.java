package com.wupol.myopia.business.core.hospital.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.core.hospital.domain.interfaces.HasResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 眼外观
 *
 * @Author wulizhou
 * @Date 2022/1/6 19:25
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class OuterEye implements HasResult {

    /**
     * 右眼睑
     */
    private List<BaseValue> rightEyelidList;
    /**
     * 左眼睑
     */
    private List<BaseValue> leftEyelidList;
    /**
     * 右结膜
     */
    private List<BaseValue> rightConjunctivaList;
    /**
     * 左结膜
     */
    private List<BaseValue> leftConjunctivaList;
    /**
     * 右眼球
     */
    private List<BaseValue> rightEyeballList;
    /**
     * 左眼球
     */
    private List<BaseValue> leftEyeballList;
    /**
     * 右角膜
     */
    private List<BaseValue> rightCornealList;
    /**
     * 左角膜
     */
    private List<BaseValue> leftCornealList;
    /**
     * 右瞳孔
     */
    private List<BaseValue> rightPupilList;
    /**
     * 左瞳孔
     */
    private List<BaseValue> leftPupilList;
    /**
     * 右巩膜
     */
    private List<BaseValue> rightScleraList;
    /**
     * 左巩膜
     */
    private List<BaseValue> leftScleraList;
    /**
     * 结果是否异常
     */
    private Boolean isAbnormal;
    /**
     * 学生id
     */
    private Integer studentId;
    /**
     * 医生id
     */
    private Integer doctorId;
    /**
     * 眼睑提示内容
     */
    private String eyelidContent;
    /**
     * 结膜提示内容
     */
    private String conjunctivaContent;
    /**
     * 眼球提示内容
     */
    private String eyeballContent;
    /**
     * 角膜提示内容
     */
    private String cornealContent;
    /**
     * 瞳孔提示内容
     */
    private String pupilContent;
    /**
     * 巩膜提示内容
     */
    private String scleraContent;

}
