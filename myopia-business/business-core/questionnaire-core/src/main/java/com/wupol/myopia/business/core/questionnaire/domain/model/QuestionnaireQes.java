package com.wupol.myopia.business.core.questionnaire.domain.model;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 问卷QES管理
 * @author hang.yuan
 * @date 2022/8/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName(value = "q_questionnaire_qes")
public class QuestionnaireQes implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 区域ID
     */
    private Integer districtId;
    /**
     * 年份
     */
    private Integer year;

    /**
     * 问卷名称
     */
    private String name;

    /**
     * 问卷描述
     */
    private String description;

    /**
     * qes文件的资源文件ID（m_resource_file表ID）
     */
    private Integer qesFileId;

    /**
     * 预览文件的资源文件ID (m_resource_file表ID）
     */
    private Integer previewFileId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date updateTime;
}
