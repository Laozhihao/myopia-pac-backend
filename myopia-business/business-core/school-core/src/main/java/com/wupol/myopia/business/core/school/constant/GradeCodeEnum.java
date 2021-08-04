package com.wupol.myopia.business.core.school.constant;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.school.domain.dto.GradeCode;
import lombok.Getter;

import java.util.*;

/**
 * 年级编码枚举类
 *
 * @author Simple4H
 */
@Getter
public enum GradeCodeEnum {

    /**
     * 小学
     */
    ONE_PRIMARY_SCHOOL("一年级", "01", SchoolAge.PRIMARY.code, "ONE_PRIMARY_SCHOOL"),
    TWO_PRIMARY_SCHOOL("二年级", "02", SchoolAge.PRIMARY.code, "TWO_PRIMARY_SCHOOL"),
    THREE_PRIMARY_SCHOOL("三年级", "03", SchoolAge.PRIMARY.code, "THREE_PRIMARY_SCHOOL"),
    FOUR_PRIMARY_SCHOOL("四年级", "04", SchoolAge.PRIMARY.code, "FOUR_PRIMARY_SCHOOL"),
    FIVE_PRIMARY_SCHOOL("五年级", "05", SchoolAge.PRIMARY.code, "FIVE_PRIMARY_SCHOOL"),
    SIX_PRIMARY_SCHOOL("六年级", "06", SchoolAge.PRIMARY.code, "SIX_PRIMARY_SCHOOL"),

    /**
     * 初中
     */
    ONE_JUNIOR_SCHOOL("初一", "11", SchoolAge.JUNIOR.code, "ONE_JUNIOR_SCHOOL"),
    TWO_JUNIOR_SCHOOL("初二", "12", SchoolAge.JUNIOR.code, "TWO_JUNIOR_SCHOOL"),
    THREE_JUNIOR_SCHOOL("初三", "13", SchoolAge.JUNIOR.code, "THREE_JUNIOR_SCHOOL"),
    FOUR_JUNIOR_SCHOOL("初四", "14", SchoolAge.JUNIOR.code, "FOUR_JUNIOR_SCHOOL"),

    /**
     * 高中
     */
    ONE_HIGH_SCHOOL("高一", "21", SchoolAge.HIGH.code, "ONE_HIGH_SCHOOL"),
    TWO_HIGH_SCHOOL("高二", "22", SchoolAge.HIGH.code, "TWO_HIGH_SCHOOL"),
    THREE_HIGH_SCHOOL("高三", "23", SchoolAge.HIGH.code, "THREE_HIGH_SCHOOL"),

    /**
     * 职高
     */
    ONE_VOCATIONAL_HIGH_SCHOOL("职高一", "31", SchoolAge.VOCATIONAL_HIGH.code, "ONE_VOCATIONAL_HIGH_SCHOOL"),
    TWO_VOCATIONAL_HIGH_SCHOOL("职高二", "32", SchoolAge.VOCATIONAL_HIGH.code, "TWO_VOCATIONAL_HIGH_SCHOOL"),
    THREE_VOCATIONAL_HIGH_SCHOOL("职高三", "33", SchoolAge.VOCATIONAL_HIGH.code, "THREE_VOCATIONAL_HIGH_SCHOOL"),

    /**
     * 幼儿园
     */
    ONE_KINDERGARTEN("小班", "51", SchoolAge.KINDERGARTEN.code, "ONE_KINDERGARTEN"),
    TWO_KINDERGARTEN("中班", "52", SchoolAge.KINDERGARTEN.code, "TWO_KINDERGARTEN"),
    THREE_KINDERGARTEN("大班", "53", SchoolAge.KINDERGARTEN.code, "THREE_KINDERGARTEN");

    private final String name;

    private final String code;

    private final Integer type;

    private final String enName;

    GradeCodeEnum(String name, String code, Integer type, String enName) {
        this.name = name;
        this.code = code;
        this.type = type;
        this.enName = enName;
    }

    public static final Map<Integer, List<GradeCodeEnum>> gradeByMap = new HashMap<>();

    static {
        gradeByMap.put(SchoolAge.PRIMARY.code, privateSchool());
        gradeByMap.put(SchoolAge.JUNIOR.code, juniorSchool());
        gradeByMap.put(SchoolAge.HIGH.code, highSchool());
        gradeByMap.put(SchoolAge.VOCATIONAL_HIGH.code, vocationalHighSchool());
        gradeByMap.put(SchoolAge.KINDERGARTEN.code, kindergartenSchool());
    }

    public static List<GradeCodeEnum> kindergartenSchool() {
        return Lists.newArrayList(GradeCodeEnum.ONE_KINDERGARTEN, GradeCodeEnum.TWO_KINDERGARTEN,
                GradeCodeEnum.THREE_KINDERGARTEN);
    }

    public static List<GradeCodeEnum> privateSchool() {
        return Lists.newArrayList(GradeCodeEnum.ONE_PRIMARY_SCHOOL, GradeCodeEnum.TWO_PRIMARY_SCHOOL,
                GradeCodeEnum.THREE_PRIMARY_SCHOOL, GradeCodeEnum.FOUR_PRIMARY_SCHOOL,
                GradeCodeEnum.FIVE_PRIMARY_SCHOOL, GradeCodeEnum.SIX_PRIMARY_SCHOOL);
    }

    public static List<GradeCodeEnum> juniorSchool() {
        return Lists.newArrayList(GradeCodeEnum.ONE_JUNIOR_SCHOOL, GradeCodeEnum.TWO_JUNIOR_SCHOOL,
                GradeCodeEnum.THREE_JUNIOR_SCHOOL, GradeCodeEnum.FOUR_JUNIOR_SCHOOL);
    }

    public static List<GradeCodeEnum> highSchool() {
        return Lists.newArrayList(GradeCodeEnum.ONE_HIGH_SCHOOL, GradeCodeEnum.TWO_HIGH_SCHOOL,
                GradeCodeEnum.THREE_HIGH_SCHOOL);
    }

    public static List<GradeCodeEnum> vocationalHighSchool() {
        return Lists.newArrayList(GradeCodeEnum.ONE_VOCATIONAL_HIGH_SCHOOL, GradeCodeEnum.TWO_VOCATIONAL_HIGH_SCHOOL,
                GradeCodeEnum.THREE_VOCATIONAL_HIGH_SCHOOL);
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
     *
     * @param code code
     * @return 描述
     */
    public static String getName(String code) {
        GradeCodeEnum h = Arrays.stream(GradeCodeEnum.values())
                .filter(item -> item.code.equals(code))
                .findFirst().orElse(null);
        return Objects.nonNull(h) ? h.name : null;
    }

    /**
     * 根据code获取
     *
     * @param code code
     * @return GradeCodeEnum
     */
    public static GradeCodeEnum getByCode(String code) {
        return Arrays.stream(GradeCodeEnum.values())
                .filter(item -> item.code.equals(code))
                .findFirst().orElse(null);
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
