package com.wupol.myopia.base.domain;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.AESUtil;
import com.wupol.myopia.base.util.MD5Util;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * TODO:
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

    public static void main(String[] args) throws Exception {
        HuYangRequestDTO requestDTO = new HuYangRequestDTO();

        ParentStudentData parentStudent = new ParentStudentData();
        parentStudent.setParentUid("cZcbe7e6RnQ6XiwOLksBLA==");
        StudentData studentData1 = new StudentData();
        studentData1.setCredentials(AESUtil.encrypt("340303201304058493"));
        studentData1.setName("张三");
        studentData1.setGender(0);
        studentData1.setRightNakedVision("5.2");
        studentData1.setLeftNakedVision("5.1");
        StudentData studentData2 = new StudentData();
        studentData2.setCredentials(AESUtil.encrypt("HZ12345678"));
        studentData2.setName("小梅");
        studentData2.setGender(1);
        studentData2.setRightNakedVision("4.9");
        studentData2.setLeftNakedVision("4.8");
        parentStudent.setStudentData(Lists.newArrayList(studentData1, studentData2));

        requestDTO.setData(Lists.newArrayList(parentStudent));
        requestDTO.setAccessToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9");
        requestDTO.setTimestamp(1673496732000L);
        requestDTO.setSign(MD5Util.generate(requestDTO.getAccessToken() + requestDTO.getTimestamp()));
        System.out.println(JSON.toJSONString(requestDTO));
    }

}
