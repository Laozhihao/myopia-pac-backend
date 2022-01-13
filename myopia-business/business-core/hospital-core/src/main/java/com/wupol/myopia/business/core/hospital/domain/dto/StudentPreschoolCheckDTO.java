package com.wupol.myopia.business.core.hospital.domain.dto;

import lombok.Data;

/**
 * @Author wulizhou
 * @Date 2022/1/13 20:12
 */
@Data
public class StudentPreschoolCheckDTO {

    /**
     * 学生id
     */
    private Integer studentId;

    /**
     * 检查数
     */
    private Integer count;

}
