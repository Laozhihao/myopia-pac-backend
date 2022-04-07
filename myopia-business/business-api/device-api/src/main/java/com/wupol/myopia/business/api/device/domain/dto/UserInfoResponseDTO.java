package com.wupol.myopia.business.api.device.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户信息返回
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
public class UserInfoResponseDTO {

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级名称
     */
    private String className;

    public UserInfoResponseDTO(String name, String gender, String gradeName, String className) {
        this.name = name;
        this.gender = gender;
        this.gradeName = gradeName;
        this.className = className;
    }
}
