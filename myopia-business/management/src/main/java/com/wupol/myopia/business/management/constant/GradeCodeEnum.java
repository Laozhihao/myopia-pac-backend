package com.wupol.myopia.business.management.constant;

import com.wupol.myopia.business.management.domain.model.GradeCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 年级编码枚举类
 *
 * @author Simple4H
 */
@Getter
public enum GradeCodeEnum {

    ONE_PRIMARY_SCHOOL("一年级", "01"),
    TWO_PRIMARY_SCHOOL("二年级", "02"),
    THREE_PRIMARY_SCHOOL("三年级", "03"),
    FOUR_PRIMARY_SCHOOL("四年级", "04"),
    FIVE_PRIMARY_SCHOOL("五年级", "05"),
    SIX_PRIMARY_SCHOOL("六年级", "06"),

    ONE_JUNIOR_SCHOOL("初一", "11"),
    TWO_JUNIOR_SCHOOL("初二", "12"),
    THREE_JUNIOR_SCHOOL("初三", "13"),
    FOUR_JUNIOR_SCHOOL("初四", "14"),

    ONE_HIGH_SCHOOL("高一", "21"),
    TWO_HIGH_SCHOOL("高二", "22"),
    THREE_HIGH_SCHOOL("高三", "23"),

    ONE_VOCATIONAL_HIGH_SCHOOL("职高一", "31"),
    TWO_VOCATIONAL_HIGH_SCHOOL("职高二", "32"),
    THREE_VOCATIONAL_HIGH_SCHOOL("职高三", "33"),

    ONE_UNIVERSITY("大一", "41"),
    TWO_UNIVERSITY("大二", "42"),
    THREE_UNIVERSITY("大三", "43"),
    FOUR_UNIVERSITY("大四", "44"),

    KINDERGARTEN("幼儿园大班", "53");

    private final String name;

    private final String code;

    GradeCodeEnum(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static List<GradeCode> getGradeCodeList() {
        List<GradeCode> gradeCodeList = new ArrayList<>();
        for (GradeCodeEnum value : values()) {
            GradeCode gradeCode = new GradeCode();
            gradeCode.setCode(value.getCode());
            gradeCode.setName(value.getName());
            gradeCode.setValue(value.toString());
            gradeCodeList.add(gradeCode);
        }
        return gradeCodeList;
    }

    /** 根据类型获取描述 */
    public static String getName(String code) {
        GradeCodeEnum h = Arrays.stream(GradeCodeEnum.values()).filter(item -> item.code.equals(code)).findFirst().orElse(null);
        return Objects.nonNull(h) ? h.name : null;
    }
}
