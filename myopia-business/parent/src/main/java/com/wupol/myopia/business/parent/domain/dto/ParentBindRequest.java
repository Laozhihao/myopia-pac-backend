package com.wupol.myopia.business.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 家长绑定孩子
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ParentBindRequest {

    /**
     * 家长ID
     */
    private Integer parentId;

    /**
     * 学生ID
     */
    private Integer studentId;
}
