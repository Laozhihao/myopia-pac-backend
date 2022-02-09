package com.wupol.myopia.business.core.school.domain.model;

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
 * 学校-班级表
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_school_class")
public class SchoolClass implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 年级ID
     */
    @NotNull(message = "年级id不能为空")
    private Integer gradeId;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 学校ID
     */
    @NotNull(message = "学校ID不能为空")
    private Integer schoolId;

    /**
     * 班级名称
     */
    @NotBlank(message = "班级名称不能为空")
    private String name;

    /**
     * 座位数
     */
    private Integer seatCount;

    /**
     * 状态 0-启用 1-禁止 2-删除
     */
    private Integer status;

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

    public SchoolClass() {
    }

    public SchoolClass(@NotNull(message = "年级id不能为空") Integer gradeId, Integer createUserId, @NotNull(message = "学校ID不能为空") Integer schoolId,
                       @NotBlank(message = "班级名称不能为空") String name, @NotNull(message = "座位数不能为空") Integer seatCount) {
        this.gradeId = gradeId;
        this.createUserId = createUserId;
        this.schoolId = schoolId;
        this.name = name;
        this.seatCount = seatCount;
    }
}
