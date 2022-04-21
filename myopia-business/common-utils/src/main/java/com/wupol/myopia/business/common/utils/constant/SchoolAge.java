package com.wupol.myopia.business.common.utils.constant;


import com.wupol.framework.core.util.DateFormatUtil;
import com.wupol.myopia.business.common.utils.domain.dto.SchoolAgeDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 学龄相关
 *
 * @author Simple4H
 */
public enum SchoolAge {

    UNKNOWN(-1, "未知"),
    KINDERGARTEN(5, "幼儿园"),
    PRIMARY(0, "小学"),
    JUNIOR(1, "初中"),
    HIGH(2, "高中"),
    VOCATIONAL_HIGH(3, "职业高中");

    /**
     * 学龄段ID
     */
    public final Integer code;

    /**
     * 学龄段描述
     */
    public final String desc;

    SchoolAge(Integer code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    /**
     * 通过code 获取学龄段
     *
     * @param code code
     * @return 学龄段
     */
    public static SchoolAge get(Integer code) {
        return Arrays.stream(SchoolAge.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(UNKNOWN);
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
     * @return Boolean
     */
    public static Boolean isMiddleSchool(Integer schoolAge) {
        return JUNIOR.code.equals(schoolAge) || HIGH.code.equals(schoolAge) || VOCATIONAL_HIGH.code.equals(schoolAge);
    }

    /**
     * 是否小学生和幼儿园
     *
     * @param schoolAge 学龄段
     * @return Boolean
     */
    public static Boolean isPrimaryAndKindergarten(Integer schoolAge) {
        return KINDERGARTEN.code.equals(schoolAge) || PRIMARY.code.equals(schoolAge);
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
}
