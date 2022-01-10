package com.wupol.myopia.base.constant;

/**
 * @Author wulizhou
 * @Date 2022/1/7 17:56
 */
public enum MonthAgeEnum {

    NB(0, "新生儿"),
    MONTH1(1, "满月"),
    MONTH3(2, "3月龄"),
    MONTH6(3, "6月龄"),
    MONTH8(4, "8月龄"),
    MONTH12(5, "12月龄"),
    MONTH18(6, "18月龄"),
    MONTH24(7, "24月龄"),
    MONTH30(8, "30月龄"),
    MONTH36(9, "36月龄"),
    YEAR4(10, "4岁"),
    YEAR5(11, "5岁"),
    YEAR6(12, "6岁"),
    ;

    /**
     * id
     */
    private final Integer id;

    /**
     * 说明
     */
    private final String name;

    MonthAgeEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

}
