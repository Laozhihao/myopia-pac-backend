package com.wupol.myopia.business.management.constant;

import com.wupol.myopia.business.management.domain.vo.SchoolAgeVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SchoolAge {
    KINDERGARTEN(5, "幼儿园"),
    PRIMARY(0, "小学"),
    JUNIOR(1, "初中"),
    HIGH(2, "高中"),
    VOCATIONAL_HIGH(3, "职业高中");

    /** 学龄段ID */
    public final Integer code;

    /** 学龄段描述 */
    public final String desc;

    SchoolAge(Integer code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public static SchoolAge get(Integer code) {
        return Arrays.stream(SchoolAge.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static List<SchoolAgeVO> getSchoolAgeList() {
        List<SchoolAgeVO> schoolAgeList = new ArrayList<>();
        for (SchoolAge value : values()) {
            SchoolAgeVO schoolAgeVO = new SchoolAgeVO();
            schoolAgeVO.setCode(value.code);
            schoolAgeVO.setDesc(value.desc);
            schoolAgeList.add(schoolAgeVO);
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
