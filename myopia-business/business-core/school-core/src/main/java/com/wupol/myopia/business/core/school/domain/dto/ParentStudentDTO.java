package com.wupol.myopia.business.core.school.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 家长端-家长绑定的学生列表
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ParentStudentDTO {

    /**
     * 学生ID
     */
    private Integer id;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 头像文件Id
     */
    private Integer avatarFileId;
}
