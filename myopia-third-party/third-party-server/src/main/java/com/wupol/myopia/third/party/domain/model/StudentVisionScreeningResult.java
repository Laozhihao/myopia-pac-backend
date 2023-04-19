package com.wupol.myopia.third.party.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 
 *
 * @Author lzh
 * @Date 2023-04-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("myopia_student_vision_screening_result")
public class StudentVisionScreeningResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字符串主键
     */
    @TableId(value = "uuid", type = IdType.AUTO)
    private String uuid;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 检测年度
     */
    private Integer yearTest;

    /**
     * 检测次数
     */
    private Integer second;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 身份证号
     */
    private String studentIdCard;

    /**
     * 学籍号
     */
    private String studentNum;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 左眼裸眼视力（数字格式范围在3.0~5.3之间，3.0以下填 9）
     */
    private String leftNakedVision;

    /**
     * 右眼裸眼视力（数字格式范围在3.0~5.3之间，3.0以下填 9）
     */
    private String rightNakedVision;

    /**
     * 是否戴镜（否：0、是：1）
     */
    private Integer isWear;

    /**
     * 戴镜类型（隐形眼镜：0	框架眼镜：1	角膜塑形镜：2）
     */
    private Integer wearGlassType;

    /**
     * 左眼戴镜视力（是否戴镜值等于1且戴镜类型等于0或1时需填写，即填戴隐形和框架眼镜的矫正视力）
     */
    private String leftGlassedVision;

    /**
     * 右眼戴镜视力
     */
    private String rightGlassedVision;

    /**
     * 左眼矫正视力（是否戴镜值等于1且戴镜类型等于2时需填写，即填戴OK镜的矫正视力）
     */
    private String leftCorrectedVision;

    /**
     * 右眼矫正视力
     */
    private String rightCorrectedVision;

    /**
     * 左眼眼镜度数（是否戴镜值等于1时需填写）
     */
    private String leftGlassesDegree;

    /**
     * 右眼眼镜度数（是否戴镜值等于1时需填写）
     */
    private String rightGlassesDegree;

    /**
     * 左眼串镜检测（0:近视，1:远视，2:其他，9:未测）
     */
    private Integer leftMirrorCheck;

    /**
     * 右眼串镜检测（0:近视，1:远视，2:其他，9:未测）
     */
    private Integer rightMirrorCheck;

    /**
     * 左眼屈光不正（0:正常，1:近视，2:远视，3:其他，9:未测）
     */
    private Integer leftAmetropia;

    /**
     * 右眼屈光不正（0:正常，1:近视，2:远视，3:其他，9:未测）
     */
    private Integer rightAmetropia;

    /**
     * 左眼近视力(3米)（只能输入0到4.9之间的数值）
     */
    private String leftCloseVision;

    /**
     * 右眼近视力(3米)（只能输入0到4.9之间的数值）
     */
    private String rightCloseVision;

    /**
     * 左眼结膜炎（0:无,1:有）
     */
    private Integer leftConjunctivitis;

    /**
     * 右眼结膜炎（0:无,1:有）
     */
    private Integer rightConjunctivitis;

    /**
     * 左眼沙眼（0:无,1:可疑,2:有沙）
     */
    private Integer leftTrachoma;

    /**
     * 右眼沙眼（0:无,1:可疑,2:有沙）
     */
    private Integer rightTrachoma;

    /**
     * 左眼球镜（最多输入两位小数，必须包含+/-；缺失值（无法检测）请填999）
     */
    private String leftSphericalMirror;

    /**
     * 右眼球镜
     */
    private String rightSphericalMirror;

    /**
     * 左眼柱镜（最多输入两位小数，必须包含+/-；缺失值（无法检测）请填999）
     */
    private String leftCylindricalMirror;

    /**
     * 右眼柱镜
     */
    private String rightCylindricalMirror;

    /**
     * 左眼轴位（只能输入0到180之间的数值，缺失值（无法检测）请填999）
     */
    private String leftAxialPosition;

    /**
     * 右眼轴位
     */
    private String rightAxialPosition;

    /**
     * 左眼角膜曲率（最多输入两位小数）
     */
    private String leftCornealCurvature;

    /**
     * 右眼角膜曲率
     */
    private String rightCornealCurvature;

    /**
     * 瞳距（最多输入两位小数）
     */
    private String pupillaryDistance;


}
