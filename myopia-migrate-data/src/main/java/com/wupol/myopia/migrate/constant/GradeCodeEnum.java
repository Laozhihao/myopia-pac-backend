package com.wupol.myopia.migrate.constant;

import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

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
    ONE_PRIMARY_SCHOOL("一年级", "01", SchoolAge.PRIMARY.code, "ONE_PRIMARY_SCHOOL", 3),
    TWO_PRIMARY_SCHOOL("二年级", "02", SchoolAge.PRIMARY.code, "TWO_PRIMARY_SCHOOL", 4),
    THREE_PRIMARY_SCHOOL("三年级", "03", SchoolAge.PRIMARY.code, "THREE_PRIMARY_SCHOOL", 5),
    FOUR_PRIMARY_SCHOOL("四年级", "04", SchoolAge.PRIMARY.code, "FOUR_PRIMARY_SCHOOL", 6),
    FIVE_PRIMARY_SCHOOL("五年级", "05", SchoolAge.PRIMARY.code, "FIVE_PRIMARY_SCHOOL", 7),
    SIX_PRIMARY_SCHOOL("六年级", "06", SchoolAge.PRIMARY.code, "SIX_PRIMARY_SCHOOL", 8),

    /**
     * 初中
     */
    ONE_JUNIOR_SCHOOL("初一", "11", SchoolAge.JUNIOR.code, "ONE_JUNIOR_SCHOOL", 9),
    TWO_JUNIOR_SCHOOL("初二", "12", SchoolAge.JUNIOR.code, "TWO_JUNIOR_SCHOOL", 10),
    THREE_JUNIOR_SCHOOL("初三", "13", SchoolAge.JUNIOR.code, "THREE_JUNIOR_SCHOOL", 11),
    FOUR_JUNIOR_SCHOOL("初四", "14", SchoolAge.JUNIOR.code, "FOUR_JUNIOR_SCHOOL", -1),

    /**
     * 高中
     */
    ONE_HIGH_SCHOOL("高一", "21", SchoolAge.HIGH.code, "ONE_HIGH_SCHOOL", 12),
    TWO_HIGH_SCHOOL("高二", "22", SchoolAge.HIGH.code, "TWO_HIGH_SCHOOL", 13),
    THREE_HIGH_SCHOOL("高三", "23", SchoolAge.HIGH.code, "THREE_HIGH_SCHOOL", 14),

    /**
     * 职高
     */
    ONE_VOCATIONAL_HIGH_SCHOOL("职高一", "31", SchoolAge.VOCATIONAL_HIGH.code, "ONE_VOCATIONAL_HIGH_SCHOOL", 15),
    TWO_VOCATIONAL_HIGH_SCHOOL("职高二", "32", SchoolAge.VOCATIONAL_HIGH.code, "TWO_VOCATIONAL_HIGH_SCHOOL", 16),
    THREE_VOCATIONAL_HIGH_SCHOOL("职高三", "33", SchoolAge.VOCATIONAL_HIGH.code, "THREE_VOCATIONAL_HIGH_SCHOOL", 17),

    /**
     * 幼儿园
     */
    ONE_KINDERGARTEN("小班", "51", SchoolAge.KINDERGARTEN.code, "ONE_KINDERGARTEN", 0),
    TWO_KINDERGARTEN("中班", "52", SchoolAge.KINDERGARTEN.code, "TWO_KINDERGARTEN", 1),
    THREE_KINDERGARTEN("大班", "53", SchoolAge.KINDERGARTEN.code, "THREE_KINDERGARTEN", 2);

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

    /**
     * 种类（山西版）
     */
    private final Integer sort;

    GradeCodeEnum(String name, String code, Integer type, String enName, Integer sort) {
        this.name = name;
        this.code = code;
        this.type = type;
        this.enName = enName;
        this.sort = sort;
    }

    /**
     * 根据类型获取编码
     *
     * @param sort 种类（山西版）
     * @return 编码
     */
    public static String getCodeBySort(int sort) {
        GradeCodeEnum h = Arrays.stream(GradeCodeEnum.values())
                .filter(item -> item.sort == sort)
                .findFirst().orElse(null);
        return Objects.nonNull(h) ? h.code : null;
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
