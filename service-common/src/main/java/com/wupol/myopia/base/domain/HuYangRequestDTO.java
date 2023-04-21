package com.wupol.myopia.base.domain;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * HuYangRequestDTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class HuYangRequestDTO implements Serializable {

    /**
     * 数据
     */
    @NotNull(message = "数据不能为空")
    private List<ParentStudentData> data;

    /**
     * token
     */
    private String accessToken;

    /**
     * 时间戳
     */
    @NotNull(message = "时间戳不能为空")
    private Long timestamp;

    /**
     * 签名 access_token+timestamp md5
     */
    @NotBlank(message = "签名不能为空")
    private String sign;

    @Getter
    @Setter
    public static class ParentStudentData implements Serializable {

        /**
         * 家长UID
         */
        private String parentUid;

        /**
         * 学生数据
         */
        private List<StudentData> studentData;

    }

    @Getter
    @Setter
    public static class StudentData implements Serializable {

        /**
         * 证件号
         */
        private String credentials;

        /**
         * 学生姓名
         */
        private String name;

        /**
         * 性别 0-男 1-女
         */
        private Integer gender;

        /**
         * 右-裸眼
         */
        private String rightNakedVision;

        /**
         * 左-裸眼
         */
        private String leftNakedVision;
    }

}
