package com.wupol.myopia.third.party.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 视力筛查结果数据
 *
 * @Author lzh
 * @Date 2023/4/14
 **/
@Accessors(chain = true)
@Data
public class VisionScreeningResultDTO {

    /**
     * 计划ID
     */
    private Integer planId;

    /**
     * 计划学生ID
     */
    private Integer planStudentId;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 学校名称
     */
    @NotBlank(message = "学校名称[schoolName]不能为空")
    private String schoolName;

    /**
     * 检测年度
     */
    @NotNull(message = "检测年度[year]不能为空")
    private Integer year;

    /**
     * 检测次数
     */
    @NotNull(message = "检测次数[time]不能为空")
    private Integer time;

    /**
     * 学生姓名
     */
    @NotBlank(message = "学生姓名[studentName]不能为空")
    private String studentName;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号[studentIdCard]不能为空")
    private String studentIdCard;

    /**
     * 学籍号
     */
    private String studentNo;

    /**
     * 左眼裸眼视力
     */
    private BigDecimal leftNakedVision;

    /**
     * 右眼裸眼视力
     */
    private BigDecimal rightNakedVision;

    /**
     * 戴镜类型：0-无、1-框架、2-隐形、3-角膜塑形
     */
    private Integer glassesType;

    /**
     * 左眼矫正视力
     */
    private BigDecimal leftCorrectedVision;

    /**
     * 右眼矫正视力
     */
    private BigDecimal rightCorrectedVision;

    /**
     * 左眼眼镜度数（是否戴镜值等于1时需填写）
     */
    private BigDecimal leftGlassesDegree;

    /**
     * 右眼眼镜度数（是否戴镜值等于1时需填写）
     */
    private BigDecimal rightGlassesDegree;

    /**
     * 左眼屈光不正
     */
    private BigDecimal leftAmetropia;

    /**
     * 右眼屈光不正
     */
    private Integer rightAmetropia;

    /**
     * 左眼球镜
     */
    private BigDecimal leftSphericalMirror;

    /**
     * 右眼球镜
     */
    private BigDecimal rightSphericalMirror;

    /**
     * 左眼柱镜
     */
    private BigDecimal leftCylindricalMirror;

    /**
     * 右眼柱镜
     */
    private BigDecimal rightCylindricalMirror;

    /**
     * 左眼轴位
     */
    private BigDecimal leftAxialPosition;

    /**
     * 右眼轴位
     */
    private BigDecimal rightAxialPosition;

}
