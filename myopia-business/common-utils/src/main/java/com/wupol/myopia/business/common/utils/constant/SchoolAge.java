package com.wupol.myopia.business.common.utils.constant;


import com.google.common.collect.Lists;
import com.wupol.framework.core.util.DateFormatUtil;
import com.wupol.myopia.business.common.utils.domain.dto.SchoolAgeDTO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学龄相关
 *
 * @author Simple4H
 */
public enum SchoolAge {


    UNKNOWN(-1, "未知", -1),
    GRADUATE(-2, "毕业", -2),
    KINDERGARTEN(5, "幼儿园", SchoolTypeEnum.KINDERGARTEN.getType()),
    PRIMARY(0, "小学", SchoolTypeEnum.PRIMARY_AND_SECONDARY.getType()),
    JUNIOR(1, "初中", SchoolTypeEnum.PRIMARY_AND_SECONDARY.getType()),
    HIGH(2, "普高", SchoolTypeEnum.PRIMARY_AND_SECONDARY.getType()),
    VOCATIONAL_HIGH(3, "职高", SchoolTypeEnum.PRIMARY_AND_SECONDARY.getType()),
    UNIVERSITY(4, "大学", SchoolTypeEnum.UNIVERSITY.getType());

    /**
     * 学龄段ID
     */
    @Getter
    public final Integer code;

    /**
     * 学龄段描述
     */
    @Getter
    public final String desc;

    /**
     * 学校类型
     */
    @Getter
    public final Integer type;

    SchoolAge(Integer code, String desc, Integer type) {
        this.desc = desc;
        this.code = code;
        this.type = type;
    }

    /**
     * 通过code 获取学龄段
     *
     * @param code code
     *
     * @return 学龄段
     */
    public static SchoolAge get(Integer code) {
        return Arrays.stream(SchoolAge.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(UNKNOWN);
    }

    /**
     * 通过code 获取学龄段
     *
     * @param code code
     *
     * @return 学龄段
     */
    public static String getDesc(Integer code) {
        return Arrays.stream(SchoolAge.values())
                .filter(item -> item.code.equals(code))
                .findFirst().map(s -> s.desc)
                .orElse(null);
    }

    /**
     * 获取所有的学龄段
     *
     * @return 学龄段列表
     */
    public static List<SchoolAgeDTO> getSchoolAgeList() {
        List<SchoolAgeDTO> schoolAgeList = new ArrayList<>();
        for (SchoolAge value : values()) {
            SchoolAgeDTO schoolAgeDTO = new SchoolAgeDTO();
            schoolAgeDTO.setCode(value.code);
            schoolAgeDTO.setDesc(value.desc);
            schoolAgeList.add(schoolAgeDTO);
        }
        return schoolAgeList;
    }

    /**
     * 是否初中生（包括初中、高中、职业高中）
     *
     * @param schoolAge 学龄段
     *
     * @return Boolean
     */
    public static Boolean isMiddleSchool(Integer schoolAge) {
        return JUNIOR.code.equals(schoolAge) || HIGH.code.equals(schoolAge) || VOCATIONAL_HIGH.code.equals(schoolAge);
    }

    /**
     * 是否小学生和幼儿园
     *
     * @param schoolAge 学龄段
     *
     * @return Boolean
     */
    public static Boolean isPrimaryAndKindergarten(Integer schoolAge) {
        return KINDERGARTEN.code.equals(schoolAge) || PRIMARY.code.equals(schoolAge);
    }

    /**
     * 批量通过code获取名称
     *
     * @param codes code
     *
     * @return 名称
     */
    public static List<String> batchNameByCode(List<Integer> codes) {
        return codes.stream().map(s -> SchoolAge.get(s).desc).collect(Collectors.toList());
    }

    /**
     * 获取学龄段描述
     */
    public static List<String> getAllDesc() {
        return getSchoolAgeList().stream().map(SchoolAgeDTO::getDesc).collect(Collectors.toList());
    }


    public static List<Integer> getList() {
        return Lists.newArrayList(KINDERGARTEN.code, PRIMARY.code, JUNIOR.code, HIGH.code, VOCATIONAL_HIGH.code, UNIVERSITY.code);
    }

    public static List<String> getNameList() {
        return Lists.newArrayList(KINDERGARTEN.desc, PRIMARY.desc, JUNIOR.desc, HIGH.desc, VOCATIONAL_HIGH.desc, "高中", UNIVERSITY.desc);
    }

    public static List<Integer> sortList(List<Integer> schoolAgeList) {
        List<Integer> result = new ArrayList<>();
        getList().forEach(schoolAge -> {
            if (schoolAgeList.contains(schoolAge)) {
                result.add(schoolAge);
            }
        });
        return result;
    }

    public static List<String> sortByNameList(List<String> schoolAgeNames) {
        List<String> result = new ArrayList<>();
        getNameList().forEach(schoolAge -> {
            if (schoolAgeNames.contains(schoolAge)) {
                result.add(schoolAge);
            }
        });
        return result;
    }

    /**
     * 通过学龄类型获取生日
     *
     * @param schoolAgeType 学龄类型
     * @return 生日
     */
    public static Date getBirthdayBySchoolAgeType(Integer schoolAgeType) {
        // 幼儿园
        if (KINDERGARTEN.code.equals(schoolAgeType)) {
            return DateFormatUtil.parse("2017-1-1", DateFormatUtil.FORMAT_ONLY_DATE);
        }
        // 中小学
        return DateFormatUtil.parse("2010-1-1", DateFormatUtil.FORMAT_ONLY_DATE);
    }

    /**
     * 是否小学生
     *
     * @param schoolAge 学龄段
     *
     * @return Boolean
     */
    public static Boolean isPrimary(Integer schoolAge) {
        return PRIMARY.code.equals(schoolAge);
    }

    /**
     * 是否大学生
     *
     * @param schoolAge 学龄段
     *
     * @return Boolean
     */
    public static Boolean isUniversity(Integer schoolAge) {
        return UNIVERSITY.code.equals(schoolAge);
    }

    /**
     * 判断是否为幼儿园
     */
    public static boolean checkKindergarten(Integer schoolAgeType) {
        return SchoolAge.KINDERGARTEN.code.equals(schoolAgeType);

    }

    /**
     * 幼儿园code
     */
    public static List<Integer> kindergartenCode(){
        return Lists.newArrayList(KINDERGARTEN.getCode());
    }

    /**
     * 小学及以上code
     */
    public static List<Integer> primaryAndAboveCode(){
        return Lists.newArrayList(PRIMARY.getCode(),JUNIOR.getCode(),HIGH.getCode(),VOCATIONAL_HIGH.getCode(),UNIVERSITY.getCode());
    }
}
