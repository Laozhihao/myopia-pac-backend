package com.wupol.myopia.business.core.school.constant;

import com.wupol.myopia.business.core.school.domain.dto.SchoolAgeDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SchoolAge {
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
     * @return Boolean
     */
    public static Boolean isMiddleSchool(Integer schoolAge) {
        return JUNIOR.code.equals(schoolAge) || HIGH.code.equals(schoolAge) || VOCATIONAL_HIGH.code.equals(schoolAge);
    }
}
