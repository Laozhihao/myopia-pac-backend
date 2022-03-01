package com.wupol.myopia.business.aggregation.screening.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AppQueryQrCodeParams {

    /**
     * 学校ID
     **/
    @NotNull(message = "学校ID不能为空")
    private Integer schoolId;

    /**
     * 年级ID
     **/
    @NotNull(message = "年级ID不能为空")
    private Integer gradeId;

    /**
     * 班级Id
     */
    @NotNull(message = "班级Id不能为空")
    private Integer classId;

    /**
     * 二维码类型
     */
    @NotNull(message = "二维码类型不能为空")
    private Integer type;

    /**
     * 学生姓名
     */
    @NotBlank(message = "学生姓名不能为空")
    private String studentName;
}
