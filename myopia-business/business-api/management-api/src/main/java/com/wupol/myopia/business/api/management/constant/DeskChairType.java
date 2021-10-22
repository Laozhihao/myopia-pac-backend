package com.wupol.myopia.business.api.management.constant;

import lombok.Getter;

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

    private DeskChairType(Integer type, Integer minHeight, Integer maxHeight) {
        this.type = type;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }
}
