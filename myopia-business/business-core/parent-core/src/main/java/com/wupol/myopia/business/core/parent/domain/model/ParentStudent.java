package com.wupol.myopia.business.core.parent.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 家长学生关系表
 *
 * @Author HaoHao
 * @Date 2021-02-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_parent_student")
public class ParentStudent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 家长ID
     */
    private Integer parentId;

    /**
     * 学生ID
     */
    private Integer studentId;

}
