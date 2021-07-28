package com.wupol.myopia.business.core.screening.flow.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * 筛查计划关联的学校表
 *
 * @author Alix
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_plan_school")
public class ScreeningPlanSchool implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 筛查计划--计划id 
     */
    private Integer screeningPlanId;

    /**
     * 筛查计划--指定的筛查机构id
     */
    private Integer screeningOrgId;

    /**
     * 筛查计划--执行的学校id
     */
    private Integer schoolId;

    /**
     * 筛查计划--学校名字
     */
    private String schoolName;

    /**
     * 机构质控员名字
     */
    @NotBlank
    @Length(max = 15)
    private String qualityControllerName;

    /**
     * 机构质控员队长
     */
    @NotBlank
    @Length(max = 15)
    private String qualityControllerCommander;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}
