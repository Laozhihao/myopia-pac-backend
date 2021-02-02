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



    ONE_PRIMARY_SCHOOL("一年级", "01", 1),
    TWO_PRIMARY_SCHOOL("二年级", "02", 1),
    THREE_PRIMARY_SCHOOL("三年级", "03", 1),
    FOUR_PRIMARY_SCHOOL("四年级", "04", 1),
    FIVE_PRIMARY_SCHOOL("五年级", "05", 1),
    SIX_PRIMARY_SCHOOL("六年级", "06", 1),

    ONE_JUNIOR_SCHOOL("初一", "11", 2),
    TWO_JUNIOR_SCHOOL("初二", "12", 2),
    THREE_JUNIOR_SCHOOL("初三", "13", 2),
    FOUR_JUNIOR_SCHOOL("初四", "14", 2),

    ONE_HIGH_SCHOOL("高一", "21", 3),
    TWO_HIGH_SCHOOL("高二", "22", 3),
    THREE_HIGH_SCHOOL("高三", "23", 3),

    ONE_VOCATIONAL_HIGH_SCHOOL("职高一", "31", 4),
    TWO_VOCATIONAL_HIGH_SCHOOL("职高二", "32", 4),
    THREE_VOCATIONAL_HIGH_SCHOOL("职高三", "33", 4),

    ONE_UNIVERSITY("大一", "41", 5),
    TWO_UNIVERSITY("大二", "42", 5),
    THREE_UNIVERSITY("大三", "43", 5),
    FOUR_UNIVERSITY("大四", "44", 5),

    KINDERGARTEN("幼儿园大班", "53", 6),

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
}
