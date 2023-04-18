package com.wupol.myopia.third.party.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

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
    @NotBlank(message = "学校名称schoolName不能为空")
    private String schoolName;

    /**
     * 检测年度
     */
    @NotBlank(message = "检测年度year不能为空")
    private Integer year;

    /**
     * 检测次数
     */
    @NotBlank(message = "检测次数second不能为空")
    private Integer second;

    /**
     * 学生姓名
     */
    @NotBlank(message = "学生姓名studentName不能为空")
    private String studentName;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号studentIdCard不能为空")
    private String studentIdCard;

    /**
     * 学籍号
     */
    private String studentNo;

    /**
     * 左眼裸眼视力
     */
    private String leftNakedVision;

    /**
     * 右眼裸眼视力
     */
    private String rightNakedVision;

    /**
     * 是否戴镜
     */
    private Integer isWear;

    /**
     * 戴镜类型
     */
    private Integer wearGlassType;

    /**
     * 左眼戴镜视力
     */
    private String leftGlassedVision;

    /**
     * 右眼戴镜视力
     */
    private String rightGlassedVision;

    /**
     * 左眼矫正视力
     */
    private String leftCorrectedVision;

    /**
     * 右眼矫正视力
     */
    private String rightCorrectedVision;

    /**
     * 左眼屈光不正
     */
    private Integer leftAmetropia;

    /**
     * 右眼屈光不正
     */
    private Integer rightAmetropia;

    /**
     * 左眼球镜
     */
    private String leftSphericalMirror;

    /**
     * 右眼球镜
     */
    private String rightSphericalMirror;

    /**
     * 左眼柱镜
     */
    private String leftCylindricalMirror;

    /**
     * 右眼柱镜
     */
    private String rightCylindricalMirror;

    /**
     * 左眼轴位
     */
    private String leftAxialPosition;

    /**
     * 右眼轴位
     */
    private String rightAxialPosition;

    /**
     * 左眼角膜曲率
     */
    private String leftCornealCurvature;

    /**
     * 右眼角膜曲率
     */
    private String rightCornealCurvature;

}
