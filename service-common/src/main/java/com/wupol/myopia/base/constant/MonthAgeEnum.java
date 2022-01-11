package com.wupol.myopia.base.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author wulizhou
 * @Date 2022/1/7 17:56
 */
public enum MonthAgeEnum {

    NB(0, "新生儿", 0),
    MONTH1(1, "满月", 1),
    MONTH3(2, "3月龄", 3),
    MONTH6(3, "6月龄", 6),
    MONTH8(4, "8月龄", 8),
    MONTH12(5, "12月龄", 12),
    MONTH18(6, "18月龄", 18),
    MONTH24(7, "24月龄", 24),
    MONTH30(8, "30月龄", 30),
    MONTH36(9, "36月龄", 36),
    YEAR4(10, "4岁", 48),
    YEAR5(11, "5岁", 60),
    YEAR6(12, "6岁", 72),
    ;

    /**
     * id
     */
    private final Integer id;

    /**
     * 说明
     */
    private final String name;

    /**
     * 月份
     */
    private final Integer offset;


    public static final List<MonthAgeEnum> monthAgeList = new ArrayList<>();

    static {
        monthAgeList.add(MONTH3);
        monthAgeList.add(MONTH6);
        monthAgeList.add(MONTH8);
        monthAgeList.add(MONTH12);
        monthAgeList.add(MONTH18);
        monthAgeList.add(MONTH24);
        monthAgeList.add(MONTH30);
        monthAgeList.add(MONTH36);
        monthAgeList.add(YEAR4);
        monthAgeList.add(YEAR5);
        monthAgeList.add(YEAR6);
    }

    MonthAgeEnum(Integer id, String name, Integer offset) {
        this.id = id;
        this.name = name;
        this.offset = offset;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

}
