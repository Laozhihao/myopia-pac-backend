package com.wupol.myopia.business.management.domain.model;

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
 * 筛查原始数据表
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_student_screening_raw_data")
public class StudentScreeningRawData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 筛查原始数据--所属的学生id
     */
    private Integer screeningPlanSchoolStudentId;

    /**
     * 筛查原始数据
     */
    private String screeningRawData;

    /**
     * 筛查原始数据--创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}
