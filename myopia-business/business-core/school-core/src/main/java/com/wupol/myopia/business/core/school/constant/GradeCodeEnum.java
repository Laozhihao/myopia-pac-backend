package com.wupol.myopia.business.core.school.constant;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.school.domain.dto.GradeCode;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 年级编码枚举类
 *
 * @author Simple4H
 */
@Getter
public enum GradeCodeEnum {

    /**
     * 未知
     */
    UNKNOWN("未知", "-1", SchoolAge.UNKNOWN.code, "UNKNOWN"),

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
    PRELIMINARY_SCHOOL("预初", "10", SchoolAge.JUNIOR.code, "PRELIMINARY_SCHOOL"),
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
     * 大学
     */
    ONE_UNIVERSITY("大一", "41", SchoolAge.UNIVERSITY.code, "ONE_UNIVERSITY"),
    TWO_UNIVERSITY("大二", "42", SchoolAge.UNIVERSITY.code, "TWO_UNIVERSITY"),
    THREE_UNIVERSITY("大三", "43", SchoolAge.UNIVERSITY.code, "THREE_UNIVERSITY"),
    FOUR_UNIVERSITY("大四", "44", SchoolAge.UNIVERSITY.code, "FOUR_UNIVERSITY"),

    /**
     * 幼儿园
     */
    SPECIAL_EDUCATION_KINDERGARTEN("特教班", "49", SchoolAge.KINDERGARTEN.code, "CARE_CLASSES_KINDERGARTEN"),
    /**
     * 托班
     */
    CARE_CLASSES_KINDERGARTEN("托班", "50", SchoolAge.KINDERGARTEN.code, "CARE_CLASSES_KINDERGARTEN"),
    ONE_KINDERGARTEN("小班", "51", SchoolAge.KINDERGARTEN.code, "ONE_KINDERGARTEN"),
    TWO_KINDERGARTEN("中班", "52", SchoolAge.KINDERGARTEN.code, "TWO_KINDERGARTEN"),
    THREE_KINDERGARTEN("大班", "53", SchoolAge.KINDERGARTEN.code, "THREE_KINDERGARTEN");

    /**
     * 名称
     */
    private final String name;

    /**
     * 编码
     */
    private final String code;

    /**
     * 学龄段
     * {@link SchoolAge}
     */
    private final Integer type;

    /**
     * 英文名称
     */
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
        gradeByMap.put(SchoolAge.UNIVERSITY.code, universitySchool());
    }

    public static List<GradeCodeEnum> kindergartenSchool() {
        return Lists.newArrayList(GradeCodeEnum.SPECIAL_EDUCATION_KINDERGARTEN, GradeCodeEnum.CARE_CLASSES_KINDERGARTEN, GradeCodeEnum.ONE_KINDERGARTEN,
                GradeCodeEnum.TWO_KINDERGARTEN, GradeCodeEnum.THREE_KINDERGARTEN);
    }

    public static List<String> kindergartenSchoolName() {
        return Lists.newArrayList(GradeCodeEnum.SPECIAL_EDUCATION_KINDERGARTEN.getName(),GradeCodeEnum.CARE_CLASSES_KINDERGARTEN.getName(), GradeCodeEnum.ONE_KINDERGARTEN.getName(),
                GradeCodeEnum.TWO_KINDERGARTEN.getName(), GradeCodeEnum.THREE_KINDERGARTEN.getName());
    }

    public static List<String> kindergartenSchoolCode() {
        return Lists.newArrayList(GradeCodeEnum.SPECIAL_EDUCATION_KINDERGARTEN.getCode(),GradeCodeEnum.CARE_CLASSES_KINDERGARTEN.getCode(), GradeCodeEnum.ONE_KINDERGARTEN.getCode(),
                GradeCodeEnum.TWO_KINDERGARTEN.getCode(), GradeCodeEnum.THREE_KINDERGARTEN.getCode());
    }

    public static List<GradeCodeEnum> privateSchool() {
        return Lists.newArrayList(GradeCodeEnum.ONE_PRIMARY_SCHOOL, GradeCodeEnum.TWO_PRIMARY_SCHOOL,
                GradeCodeEnum.THREE_PRIMARY_SCHOOL, GradeCodeEnum.FOUR_PRIMARY_SCHOOL,
                GradeCodeEnum.FIVE_PRIMARY_SCHOOL, GradeCodeEnum.SIX_PRIMARY_SCHOOL);
    }

    public static List<String> privateSchoolCodes() {
        return Lists.newArrayList(GradeCodeEnum.ONE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.TWO_PRIMARY_SCHOOL.getCode(),
                GradeCodeEnum.THREE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.FOUR_PRIMARY_SCHOOL.getCode(),
                GradeCodeEnum.FIVE_PRIMARY_SCHOOL.getCode(), GradeCodeEnum.SIX_PRIMARY_SCHOOL.getCode());
    }

    public static List<GradeCodeEnum> juniorSchool() {
        return Lists.newArrayList(GradeCodeEnum.PRELIMINARY_SCHOOL, GradeCodeEnum.ONE_JUNIOR_SCHOOL, GradeCodeEnum.TWO_JUNIOR_SCHOOL,
                GradeCodeEnum.THREE_JUNIOR_SCHOOL, GradeCodeEnum.FOUR_JUNIOR_SCHOOL);
    }

    public static List<String> juniorSchoolCodes() {
        return Lists.newArrayList(GradeCodeEnum.PRELIMINARY_SCHOOL.getCode(), GradeCodeEnum.ONE_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.TWO_JUNIOR_SCHOOL.getCode(),
                GradeCodeEnum.THREE_JUNIOR_SCHOOL.getCode(), GradeCodeEnum.FOUR_JUNIOR_SCHOOL.getCode());
    }

    public static List<GradeCodeEnum> highSchool() {
        return Lists.newArrayList(GradeCodeEnum.ONE_HIGH_SCHOOL, GradeCodeEnum.TWO_HIGH_SCHOOL,
                GradeCodeEnum.THREE_HIGH_SCHOOL);
    }

    public static List<String> highSchoolCodes() {
        return Lists.newArrayList(GradeCodeEnum.ONE_HIGH_SCHOOL.getCode(), GradeCodeEnum.TWO_HIGH_SCHOOL.getCode(),
                GradeCodeEnum.THREE_HIGH_SCHOOL.getCode());
    }

    public static List<GradeCodeEnum> vocationalHighSchool() {
        return Lists.newArrayList(GradeCodeEnum.ONE_VOCATIONAL_HIGH_SCHOOL, GradeCodeEnum.TWO_VOCATIONAL_HIGH_SCHOOL,
                GradeCodeEnum.THREE_VOCATIONAL_HIGH_SCHOOL);
    }

    public static List<GradeCodeEnum> universitySchool() {
        return Lists.newArrayList(GradeCodeEnum.ONE_UNIVERSITY, GradeCodeEnum.TWO_UNIVERSITY,
                GradeCodeEnum.THREE_UNIVERSITY,GradeCodeEnum.FOUR_UNIVERSITY);
    }

    public static List<String> vocationalHighSchoolCodes() {
        return Lists.newArrayList(GradeCodeEnum.ONE_VOCATIONAL_HIGH_SCHOOL.getCode(), GradeCodeEnum.TWO_VOCATIONAL_HIGH_SCHOOL.getCode(),
                GradeCodeEnum.THREE_VOCATIONAL_HIGH_SCHOOL.getCode());
    }

    public static List<String> universitySchoolCodes() {
        return Lists.newArrayList(GradeCodeEnum.ONE_UNIVERSITY.getCode(), GradeCodeEnum.TWO_UNIVERSITY.getCode(),
                GradeCodeEnum.THREE_UNIVERSITY.getCode(),GradeCodeEnum.FOUR_UNIVERSITY.getCode());
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
     * 根据code获取
     *
     * @param code code
     * @return GradeCodeEnum
     */
    public static GradeCodeEnum getByCode(String code) {
        return Arrays.stream(GradeCodeEnum.values())
                .filter(item -> item.code.equals(code))
                .findFirst().orElse(UNKNOWN);
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
                .findFirst().orElse(UNKNOWN);
    }

    public static List<String> primaryAbove() {
        return Lists.newArrayList(Iterables.concat(privateSchoolCodes(), juniorSchoolCodes(), highSchoolCodes(), vocationalHighSchoolCodes(),universitySchoolCodes()));
    }

    /**
     * 通过code 获取学龄段
     *
     * @param code code
     *
     * @return 学龄段
     */
    public static String getDesc(String code) {
        return Arrays.stream(GradeCodeEnum.values())
                .filter(item -> item.code.equals(code))
                .findFirst().map(GradeCodeEnum::getName)
                .orElse(null);
    }

    public static List<String> getAllName() {
        return getGradeCodeList().stream().map(GradeCode::getName).collect(Collectors.toList());
    }
}
