package com.wupol.myopia.business.core.school.constant;


import com.google.common.collect.Lists;
import com.wupol.myopia.business.core.school.domain.model.School;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 学校相关常量
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@Getter
public enum SchoolEnum {
    /** 住宿类型 */
    LODGE_ALL(0, "全部住校"),
    LODGE_PART(1, "部分住校"),
    LODGE_NON(2, "不住校"),

    /** 学校类型 */
    TYPE_PRIMARY(0, "小学"),
    TYPE_MIDDLE(1, "初级中学"),
    TYPE_HIGH(2, "高级中学"),
    TYPE_INTEGRATED_MIDDLE(3, "完全中学"),
    TYPE_9(4, "九年一贯制学校"),
    TYPE_12(5, "十二年一贯制学校"),
    TYPE_VOCATIONAL(6, "职业高中"),
    TYPE_OTHER(7, "其他"),
    TYPE_KINDERGARTEN(8, "幼儿园"),
    TYPE_UNIVERSITY(9, "大学"),

    /** 学校性质 */
    KIND_1(0, "公办"),
    KIND_2(1, "民办"),
    KIND_3(2, "其他");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 描述
     **/
    private final String name;

    private static final List<SchoolEnum> SCHOOL_LODGE = Lists.newArrayList(SchoolEnum.LODGE_ALL,
            SchoolEnum.LODGE_PART, SchoolEnum.LODGE_NON);

    private static final List<SchoolEnum> SCHOOL_TYPE = Lists.newArrayList(SchoolEnum.TYPE_PRIMARY,
            SchoolEnum.TYPE_MIDDLE, SchoolEnum.TYPE_HIGH, SchoolEnum.TYPE_INTEGRATED_MIDDLE,
            SchoolEnum.TYPE_9, SchoolEnum.TYPE_12, SchoolEnum.TYPE_VOCATIONAL, SchoolEnum.TYPE_OTHER, TYPE_KINDERGARTEN, TYPE_UNIVERSITY);

    private static final List<SchoolEnum> SCHOOL_KIND = Lists.newArrayList(SchoolEnum.KIND_1,
            SchoolEnum.KIND_2, SchoolEnum.KIND_3);


    /**
     * 非幼儿园数组
     */
    private static final List<SchoolEnum> NOT_KINDERGARTEN_SCHOOL_LIST = Lists.newArrayList(SchoolEnum.TYPE_PRIMARY,SchoolEnum.TYPE_MIDDLE,SchoolEnum.TYPE_HIGH,
            SchoolEnum.TYPE_INTEGRATED_MIDDLE,SchoolEnum.TYPE_9,SchoolEnum.TYPE_12,SchoolEnum.TYPE_VOCATIONAL,SchoolEnum.TYPE_OTHER,SchoolEnum.TYPE_UNIVERSITY);

    SchoolEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * 根据type获取对象
     *
     * @param type
     * @return
     */
    public static SchoolEnum getByType(Integer type) {
        return SchoolEnum.SCHOOL_TYPE.stream().filter(x -> x.type.equals(type)).findFirst().orElse(null);
    }

    /**
     * 根据类型获取描述
     *
     * @param type 类型
     * @return 描述
     */
    public static String getLodgeName(Integer type) {
        return SCHOOL_LODGE.stream()
                .filter(item -> item.type.equals(type))
                .map(SchoolEnum::getName)
                .findFirst().orElse(StringUtils.EMPTY);
    }

    /**
     * 根据类型获取描述
     *
     * @param type 类型
     * @return 描述
     */
    public static String getTypeName(Integer type) {
        return SCHOOL_TYPE.stream()
                .filter(item -> item.type.equals(type))
                .map(SchoolEnum::getName)
                .findFirst().orElse(StringUtils.EMPTY);
    }

    /**
     * 根据类型获取描述
     *
     * @param type 类型
     * @return 描述
     */
    public static String getKindName(Integer type) {
        return SCHOOL_KIND.stream()
                .filter(item -> item.type.equals(type))
                .map(SchoolEnum::getName)
                .findFirst().orElse(StringUtils.EMPTY);
    }


    /**
     * 是否不是幼儿园
     *
     * @param type
     * @return
     */
    public static boolean checkNotKindergartenSchool(Integer type) {
        return !Objects.equals(type, TYPE_KINDERGARTEN.getType()) && !Objects.equals(type, TYPE_UNIVERSITY.type);
    }
}

