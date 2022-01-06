package com.wupol.myopia.business.api.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 更新学生身份证
 *
 * @author Simple4H
 */
@Getter
@Setter
public class BindStudentRequestDTO {

    /**
     * 身份证
     */
    private String idCard;

    /**
     * 姓名
     */
    private String name;
}
