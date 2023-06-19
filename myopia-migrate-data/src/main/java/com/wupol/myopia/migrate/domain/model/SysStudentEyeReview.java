package com.wupol.myopia.migrate.domain.model;

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
 * 视力复测
 *
 * @Author lzh
 * @Date 2023-06-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_student_eye_review")
public class SysStudentEyeReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "eye_id", type = IdType.AUTO)
    private String eyeId;

    /**
     * 学生ID
     */
    private String studentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学生手机号
     */
    private String studentPhone;

    /**
     * 学生身份证号
     */
    private String studentIdcard;

    /**
     * 学术性别
     */
    private String studentSex;

    /**
     * 学术出生日期
     */
    private String studentBirthday;

    /**
     * 省份
     */
    private String studentProvince;

    /**
     * 城市
     */
    private String studentCity;

    /**
     * 区域
     */
    private String studentRegion;

    /**
     * 学校
     */
    private String schoolName;

    /**
     * 拼接名称
     */
    private String splicing;

    /**
     * 年级
     */
    private String schoolGrade;

    /**
     * 班级
     */
    private String schoolClazz;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 学校ID
     */
    private String schoolId;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 筛查人员ID
     */
    private String userId;

    /**
     * 左眼球镜
     */
    private String lSph;

    /**
     * 左眼柱镜
     */
    private String lCyl;

    /**
     * 左眼轴位
     */
    private String lAxial;

    /**
     * 右眼球镜
     */
    private String rSph;

    /**
     * 右眼镜柱
     */
    private String rCyl;

    /**
     * 右眼轴位
     */
    private String rAxial;

    /**
     * 左眼裸视力
     */
    private String lLsl;

    /**
     * 右眼裸视力
     */
    private String rLsl;

    /**
     * 左眼矫正视力
     */
    private String lJzsl;

    /**
     * 右眼矫正视力
     */
    private String rJzsl;

    /**
     * 左眼串镜
     */
    private String lLcj;

    /**
     * 右眼串镜
     */
    private String rLcj;

    /**
     * 左眼屈光
     */
    private String lQg;

    /**
     * 右眼屈光
     */
    private String rQg;

    /**
     * 左眼近视/远视度数
     */
    private String lSe;

    /**
     * 右眼近视/远视度数
     */
    private String rSe;

    /**
     * 左边眼轴
     */
    private String lYz;

    /**
     * 右眼眼轴
     */
    private String rYz;

    /**
     * 带何种眼镜
     */
    private String glasses;

    /**
     * 眼位
     */
    private String positive;

    /**
     * 眼病(左/右)
     */
    private String diseaseEye;

    /**
     * 眼病(左眼)
     */
    private String lDisease;

    /**
     * 眼病(右眼)
     */
    private String rDisease;

    /**
     * 缓存key
     */
    private String cacheKey;

    /**
     * 电脑验光仪型号
     */
    private String model;

    /**
     * 视力筛查人员ID
     */
    private String visionUserId;

    /**
     * 电脑验光筛查人员ID
     */
    private String optometryUserId;

    /**
     * 生物测量筛查人员ID
     */
    private String biologyUserId;

    /**
     * 其他眼病筛查人员ID
     */
    private String diseaseUserId;

    /**
     * ok镜左眼度数
     */
    private String okGlassesLeft;

    /**
     * OK镜右眼度数
     */
    private String okGlassesRight;

    /**
     * 自定义眼病
     */
    private String diyDisease;

}
