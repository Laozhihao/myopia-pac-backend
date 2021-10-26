package com.wupol.myopia.business.api.management.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课桌椅的型号
 *
 * @Author HaoHao
 * @Date 2021/10/21
 **/
@Getter
public enum DeskChairType {
    /** 中小学 */
    PRIMARY_AND_SECONDARY_DESK_0(0, 180, 300),
    PRIMARY_AND_SECONDARY_DESK_1(1, 173, 187),
    PRIMARY_AND_SECONDARY_DESK_2(2, 165, 179),
    PRIMARY_AND_SECONDARY_DESK_3(3, 158, 172),
    PRIMARY_AND_SECONDARY_DESK_4(4, 150, 164),
    PRIMARY_AND_SECONDARY_DESK_5(5, 143, 157),
    PRIMARY_AND_SECONDARY_DESK_6(6, 135, 149),
    PRIMARY_AND_SECONDARY_DESK_7(7, 128, 142),
    PRIMARY_AND_SECONDARY_DESK_8(8, 120, 134),
    PRIMARY_AND_SECONDARY_DESK_9(9, 113, 127),
    PRIMARY_AND_SECONDARY_DESK_10(10, 0, 119),

    /** 幼儿园 */
    KINDERGARTEN_DESK_1(1, 113, 180),
    KINDERGARTEN_DESK_2(2, 105, 119),
    KINDERGARTEN_DESK_3(3, 98, 104),
    KINDERGARTEN_DESK_4(4, 90, 104),
    KINDERGARTEN_DESK_5(5, 83, 97),
    KINDERGARTEN_DESK_6(6, 0, 89);

    Integer type;
    Integer minHeight;
    Integer maxHeight;

    private static final List<DeskChairType> PRIMARY_SECONDARY_DESK_LIST = Arrays.asList(PRIMARY_AND_SECONDARY_DESK_0, PRIMARY_AND_SECONDARY_DESK_1,
            PRIMARY_AND_SECONDARY_DESK_2, PRIMARY_AND_SECONDARY_DESK_3,
            PRIMARY_AND_SECONDARY_DESK_4, PRIMARY_AND_SECONDARY_DESK_5,
            PRIMARY_AND_SECONDARY_DESK_6, PRIMARY_AND_SECONDARY_DESK_7,
            PRIMARY_AND_SECONDARY_DESK_8, PRIMARY_AND_SECONDARY_DESK_9, PRIMARY_AND_SECONDARY_DESK_10);

    private static final List<DeskChairType> KINDERGARTEN_DESK_LIST = Arrays.asList(KINDERGARTEN_DESK_1, KINDERGARTEN_DESK_2,
            KINDERGARTEN_DESK_3, KINDERGARTEN_DESK_4,
            KINDERGARTEN_DESK_5, KINDERGARTEN_DESK_6);

    DeskChairType(Integer type, Integer minHeight, Integer maxHeight) {
        this.type = type;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    /**
     * 根据身高，获取中小学课桌椅型号
     *
     * @param height 身高
     * @return java.util.List<java.lang.Integer>
     **/
    public static List<Integer> getPrimarySecondaryTypeByHeight(Float height) {
        return PRIMARY_SECONDARY_DESK_LIST.stream().filter(x -> height >= x.getMinHeight() && height < x.getMaxHeight()).map(DeskChairType::getType).collect(Collectors.toList());
    }

    /**
     * 根据身高，获取幼儿园课桌椅型号
     *
     * @param height 身高
     * @return java.util.List<java.lang.Integer>
     **/
    public static List<Integer> getKindergartenTypeByHeight(Float height) {
        return KINDERGARTEN_DESK_LIST.stream().filter(x -> height >= x.getMinHeight() && height < x.getMaxHeight()).map(DeskChairType::getType).collect(Collectors.toList());
    }
}
