package com.wupol.myopia.business.management.constant;


import java.util.Arrays;
import java.util.Objects;

/**
 * 机构相关常量
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
public enum ScreeningOrganizationEnum {
    TYPE_COMMON(0, "普通医院"),
    TYPE_MATERNAL_AND_CHILD_HEALTH_CARE(1, "妇幼保健院"),
    TYPE_DISEASE_CONTROL_AND_PREVENTION(2, "疾病预防控制中心"),
    TYPE_COMMUNITY_HEALTH_CENTER(3, "社区卫生服务中心"),
    TYPE_TOWNSHIP_HEALTH(4, "乡镇卫生院"),
    TYPE_HEALTH_CARE_INSTITUTIONS_FOR_SCHOOL(5, "中小学生保健机构"),
    TYPE_OTHER(6, "其他");

    /** 类型 **/
    private final Integer type;
    /** 描述 **/
    private final String name;

    ScreeningOrganizationEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    /** 根据类型获取描述 */
    public static String getNameByType(Integer type) {
        ScreeningOrganizationEnum h = Arrays.stream(ScreeningOrganizationEnum.values()).filter(item -> item.type.equals(type)).findFirst().orElse(null);
        return Objects.nonNull(h) ? h.name : null;
    }

    public Integer getType() {
        return this.type;
    }
    public String getName() {
        return this.name;
    }
   }

