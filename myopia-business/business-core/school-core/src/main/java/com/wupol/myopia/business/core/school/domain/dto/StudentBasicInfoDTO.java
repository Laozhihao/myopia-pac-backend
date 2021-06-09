package com.wupol.myopia.business.core.school.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 基础的学生信息DTO
 *
 * @author jacob
 */
@Data
@Accessors(chain = true)
public class StudentBasicInfoDTO {
    /**
     * 年级名称
     */
    private String studentName;

    /**
     * 相关联系人的电话
     */
    private List<String> phoneNums;
    /**
     * 学生id
     */
    private Integer studentId;
}
