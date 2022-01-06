package com.wupol.myopia.business.api.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

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
    @NotBlank(message = "身份证不能为空")
    private String idCard;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String name;
}
