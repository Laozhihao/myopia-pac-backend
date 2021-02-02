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


    ONE_PRIMARY_SCHOOL("一年级", "01", 0),
    TWO_PRIMARY_SCHOOL("二年级", "02", 0),
    THREE_PRIMARY_SCHOOL("三年级", "03", 0),
    FOUR_PRIMARY_SCHOOL("四年级", "04", 0),
    FIVE_PRIMARY_SCHOOL("五年级", "05", 0),
    SIX_PRIMARY_SCHOOL("六年级", "06", 0),

    ONE_JUNIOR_SCHOOL("初一", "11", 1),
    TWO_JUNIOR_SCHOOL("初二", "12", 1),
    THREE_JUNIOR_SCHOOL("初三", "13", 1),
    FOUR_JUNIOR_SCHOOL("初四", "14", 1),

    ONE_HIGH_SCHOOL("高一", "21", 2),
    TWO_HIGH_SCHOOL("高二", "22", 2),
    THREE_HIGH_SCHOOL("高三", "23", 2),

    ONE_VOCATIONAL_HIGH_SCHOOL("职高一", "31", 3),
    TWO_VOCATIONAL_HIGH_SCHOOL("职高二", "32", 3),
    THREE_VOCATIONAL_HIGH_SCHOOL("职高三", "33", 3),

    ONE_UNIVERSITY("大一", "41", 4),
    TWO_UNIVERSITY("大二", "42", 4),
    THREE_UNIVERSITY("大三", "43", 4),
    FOUR_UNIVERSITY("大四", "44", 4),

    ONE_KINDERGARTEN("小班", "51", 5),
    TWO_KINDERGARTEN("中班", "52", 5),
    THREE_KINDERGARTEN("大班", "53", 5),

    OTHER("其他", "90", 0);

    private final String name;

    private final String code;

    private final Integer type;

    GradeCodeEnum(String name, String code, Integer type) {
        this.name = name;
        this.code = code;
        this.type = type;
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

    /**
     * 根据类型获取描述
     */
    public static String getName(String code) {
        GradeCodeEnum h = Arrays.stream(GradeCodeEnum.values())
                .filter(item -> item.code.equals(code))
                .findFirst().orElse(null);
        return Objects.nonNull(h) ? h.name : null;
    }

    /**
     * 通过名称获取学校code和type
     *
     * @param name 名称
     * @return GradeCodeEnum
     */
    public static GradeCodeEnum getByName(String name) {
        return Arrays.stream(GradeCodeEnum.values())
                .filter(item -> item.name.equals(name))
                .findFirst().orElse(null);
    }
}
