package com.wupol.myopia.business.management.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wupol.myopia.business.management.constant.RescreeningStatisticEnum;
import com.wupol.myopia.business.management.domain.dos.BiometricDataDO;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.dos.OtherEyeDiseasesDO;
import com.wupol.myopia.business.management.domain.dos.VisionDataDO;
import lombok.Data;

import java.util.Date;

/**
 * @Description
 * @Date 2021/2/1 0:19
 * @Author by Jacob
 */
@Data
@TableName(autoResultMap = true)
public class StudentScreeningInfoWithResultDTO {
    /**
     * 筛查结果--所属的任务id
     */
    private Integer taskId;
    /**
     * 筛查结果--所属的计划id
     */
    private Integer screeningPlanId;
    /**
     * 筛查计划--执行的学校名字
     */
    private String schoolName;
    /**
     * 筛查结果--学校id
     */
    private Integer schoolId;
    /**
     * 筛查结果--班级id
     */
    private Integer clazzId;
    /**
     * 筛查计划--班级名称
     */
    private String clazzName;
    /**
     * 筛查计划--年级名称
     */
    private String gradeName;
    /**
     * 筛查结果--年级id
     */
    private Integer gradeId;
    /**
     * 筛查结果--学生id
     */
    private Integer studentId;
    /**
     * 筛查计划--参与筛查的学生名字
     */
    private String studentName;
    /**
     * 筛查计划--参与筛查的学生年龄
     */
    private Integer studentAge;
    /**
     * 筛查计划--参与筛查的学生性别
     */
    private Integer studentGender;
    /**
     * 分组key
     */
    private String groupByKey = "";
    /**
     * 筛查结果--医生姓名
     */
    private String doctorName;
    /**
     * 筛查结果--视力检查结果
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private VisionDataDO visionData;

    /**
     * 筛查结果--电脑验光
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ComputerOptometryDO computerOptometry;

    /**
     * 筛查结果--生物测量
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BiometricDataDO biometricData;
    /**
     * 筛查结果--其他眼病
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private OtherEyeDiseasesDO otherEyeDiseases;
    /**
     * 筛查结果--是否复筛（0否，1是）
     */
    private Integer isDoubleScreen;
    /**
     * 筛查结果最后更新时间
     */
    private Date updateTime;

    public boolean judgeCompleted() {
        if (this.getVisionData() == null || this.getComputerOptometry() == null) {
            return false;
        }
        return this.getVisionData().judgeValidData() && this.getComputerOptometry().judgeValidData();
    }

    /**
     * 获取分组的key
     * @return
     */
    public String getGroupKey(RescreeningStatisticEnum rescreeningStatisticEnum) {
        switch (rescreeningStatisticEnum) {
            case SCHOOL:
                return String.valueOf(schoolId);
            case GRADE:
                return String.valueOf(schoolId) + gradeId;
            case CLASS:
                return String.valueOf(schoolId) + gradeId + clazzId;
            default:
                return "";
        }
    }
}
