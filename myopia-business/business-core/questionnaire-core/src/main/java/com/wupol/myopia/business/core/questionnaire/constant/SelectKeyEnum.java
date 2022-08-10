package com.wupol.myopia.business.core.questionnaire.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 下拉选择
 *
 * @author Simple4H
 */
@Getter
public enum SelectKeyEnum {
    KEY_1("infectiousDiseaseKeyOne", "甲乙病"),
    KEY_2("infectiousDiseaseKeyTwo", "丙病"),
    KEY_3("teacherType", "教师-类别"),
    KEY_4("workType", "教师-专/兼职"),
    KEY_5("educationType", "教师-学历"),
    KEY_6("jobTitle", "教师-职称"),
    KEY_7("qualificationCertificate", "教师-执业资格证书"),
    ;

    /**
     * key
     **/
    private final String key;

    /**
     * 描述
     **/
    private final String desc;

    SelectKeyEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public static List<SelectKey> getList() {
        return Arrays.stream(SelectKeyEnum.values()).map(s -> new SelectKey(s.getKey(), s.getDesc())).collect(Collectors.toList());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectKey {
        /**
         * key
         **/
        private String key;

        /**
         * 描述
         **/
        private String desc;
    }
}
