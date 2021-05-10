package com.wupol.myopia.business.management.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 学校-年级表
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_school_grade")
public class SchoolGrade implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 学校ID
     */
    @NotNull(message = "学校id不能为空")
    private Integer schoolId;

    /**
     * 年级code
     */
    @NotBlank(message = "年级code不能为空")
    private String gradeCode;

    /**
     * 年级名称
     */
    @NotBlank(message = "年级名称不能为空")
    private String name;

    /**
     * 状态 0-启用 1-禁止 2-删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public SchoolGrade(Integer createUserId, @NotNull(message = "学校id不能为空") Integer schoolId, @NotBlank(message = "年级code不能为空") String gradeCode, @NotBlank(message = "年级名称不能为空") String name) {
        this.createUserId = createUserId;
        this.schoolId = schoolId;
        this.gradeCode = gradeCode;
        this.name = name;
    }

    public SchoolGrade() {
    }
}
