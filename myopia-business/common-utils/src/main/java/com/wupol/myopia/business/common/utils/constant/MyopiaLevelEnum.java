package com.wupol.myopia.business.common.utils.constant;

import lombok.Getter;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * 近视等级
 *
 * @author Simple4H
 */
public enum MyopiaLevelEnum {

    ZERO(0, "正常"),
    SCREENING_MYOPIA(1, "筛查性近视"),
    MYOPIA_LEVEL_EARLY(2, "近视前期"),
    MYOPIA_LEVEL_LIGHT(3, "低度近视"),
    MYOPIA_LEVEL_MIDDLE(4, "中度近视"),
    MYOPIA_LEVEL_HIGH(5, "高度近视");

    @Getter
    public final Integer code;
    @Getter
    public final String desc;

    MyopiaLevelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MyopiaLevelEnum get(int code) {
        return Arrays.stream(MyopiaLevelEnum.values())
                .filter(item -> item.code == code)
                .findFirst()
                .orElse(null);
    }

    public static String getDesc(Integer code) {
        if (Objects.isNull(code)) {
            return "";
        }
        MyopiaLevelEnum myopiaLevelEnum = get(code);
        return Objects.isNull(myopiaLevelEnum) ? "" : myopiaLevelEnum.desc;
    }

    /**
     * 是否有座椅建议
     * @param code 等级
     * @return 是否有座椅建议
     */
    public static Boolean seatSuggest(Integer code) {
        if (Objects.isNull(code)) {
            return false;
        }
        ArrayList<Integer> levelList = Lists.newArrayList(MYOPIA_LEVEL_EARLY.code, MYOPIA_LEVEL_EARLY.code, MYOPIA_LEVEL_EARLY.code);
        return levelList.stream().anyMatch(s -> Objects.equals(s, code));
    }
}
