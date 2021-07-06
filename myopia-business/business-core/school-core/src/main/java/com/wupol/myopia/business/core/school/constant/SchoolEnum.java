package com.wupol.myopia.business.core.school.constant;


import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 学校相关常量
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public enum SchoolEnum {
    LODGE_ALL(0, "全部住校"),
    LODGE_PART(1, "部分住校"),
    LODGE_NON(2, "不住校"),
    TYPE_PRIMARY(0, "小学"),
    TYPE_MIDDLE(1, "初级中学"),
    TYPE_HIGH(2, "高级中学"),
    TYPE_INTEGRATED_MIDDLE(3, "完全中学"),
    TYPE_9(4, "九年一贯制学校"),
    TYPE_12(5, "十二年一贯制学校"),
    TYPE_VOCATIONAL(6, "职业高中"),
    TYPE_OTHER(7, "其他"),
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

    private static final List<SchoolEnum> schoolLodge = Lists.newArrayList(SchoolEnum.LODGE_ALL,
            SchoolEnum.LODGE_PART, SchoolEnum.LODGE_NON);

    private static final List<SchoolEnum> schoolType = Lists.newArrayList(SchoolEnum.TYPE_PRIMARY,
            SchoolEnum.TYPE_MIDDLE, SchoolEnum.TYPE_HIGH, SchoolEnum.TYPE_INTEGRATED_MIDDLE,
            SchoolEnum.TYPE_9, SchoolEnum.TYPE_12, SchoolEnum.TYPE_VOCATIONAL, SchoolEnum.TYPE_OTHER);

    private static final List<SchoolEnum> schoolKind = Lists.newArrayList(SchoolEnum.KIND_1,
            SchoolEnum.KIND_2, SchoolEnum.KIND_3);

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
        return SchoolEnum.schoolType.stream().filter(x -> x.type.equals(type)).findFirst().orElse(null);
    }

    /**
     * 根据类型获取描述
     *
     * @param type 类型
     * @return 描述
     */
    public static String getLodgeName(Integer type) {
        return schoolLodge.stream()
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
        return schoolType.stream()
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
        return schoolKind.stream()
                .filter(item -> item.type.equals(type))
                .map(SchoolEnum::getName)
                .findFirst().orElse(StringUtils.EMPTY);
    }

    public Integer getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }
}

