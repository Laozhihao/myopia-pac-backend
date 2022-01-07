package com.wupol.myopia.business.core.hospital.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 视力检查检查数据
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class VisionMedicalRecord {
    /** 学生id */
    private Integer studentId;
    /**
     * 医生id
     */
    private Integer doctorId;

    /** 眼压类型。眼压计测量。 */
    public static final Integer PRESSURE_1 = 1;
    /** 眼压类型。指测眼压法。 */
    public static final Integer PRESSURE_2 = 2;

    /** 眼压类型。1眼压计测量。2.指测眼压法。 */
    private Integer pressureType;
    /** 眼压单位 */
    private String pressureUnit;
    /** 是否佩镜 */
    private Boolean wearGlasses;
    /** 眼镜类型。0没有佩戴眼镜, 1佩戴框架眼镜，2佩戴隐形眼镜，3夜戴角膜塑形镜。 */
    private Integer glassesType;
    /** 旧镜验镜时间，选择是否佩镜后必填 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date oldGlassTime;
    /** 右眼裸眼视力 */
    private String rightRawVision;
    /** 左眼裸眼视力 */
    private String leftRawVision;
    /** 右眼矫正视力 */
    private String rightVision;
    /** 左眼矫正视力 */
    private String leftVision;
    /** 右眼评估视力 */
    private String rightAssessVision;
    /** 左眼评估视力 */
    private String leftAssessVision;
    /** 右眼压力 */
    private String rightPressure;
    /** 左眼压力 */
    private String leftPressure;
    /** 右眼球镜 */
    private String rightDS;
    /** 左眼球镜 */
    private String leftDS;
    /** 右眼柱镜 */
    private String rightDC;
    /** 左眼柱镜 */
    private String leftDC;
    /** 右眼轴位 */
    private String rightAxis;
    /** 左眼轴位 */
    private String leftAxis;
    /** 右眼近用附加度 */
    private String rightADD;
    /** 左眼近用附加度 */
    private String leftADD;
    /** 右眼佩镜视力 */
    private String rightGlassVision;
    /** 左眼佩镜视力 */
    private String leftGlassVision;
    /** 瞳距 */
    private String pd;

    private Integer rightStatus;
    private Integer leftStatus;

}
