package com.wupol.myopia.business.core.questionnaire.constant;

import lombok.Getter;

/**
 * 下拉选择
 *
 * @author Simple4H
 */
@Getter
public enum DropSelectEnum {
    ONE_1("01", "01鼠疫", "infectiousDiseaseKeyOne"),
    ONE_2("", "02霍乱", ""),
    ONE_3("", "03传染性非典型肺炎", ""),
    ONE_4("", "04艾滋病", ""),
    ONE_5("", "05病毒性肝炎", ""),
    ONE_6("", "06脊髓灰质炎", ""),
    ONE_7("", "07人感染高致病性禽流感", ""),
    ONE_8("", "08麻疹", ""),
    ONE_9("", "09流行性出血热", ""),
    ONE_10("", "10狂犬病", ""),
    ONE_11("", "11流行性乙型脑炎", ""),
    ONE_12("", "12登革热", ""),
    ONE_13("", "13炭疽", ""),
    ONE_14("", "14痢疾", ""),
    ONE_15("", "15肺结核", ""),
    ONE_16("", "16伤寒", ""),
    ONE_17("", "17流行性脑脊髓膜炎", ""),
    ONE_18("", "18百日咳", ""),
    ONE_19("", "19白喉", ""),
    ONE_20("", "20新生儿破伤风", ""),
    ONE_21("", "21猩红热", ""),
    ONE_22("", "22布鲁氏菌病", ""),
    ONE_23("", "23淋病", ""),
    ONE_24("", "24梅毒", ""),
    ONE_25("", "25钩端螺旋体病", ""),
    ONE_26("", "26血吸虫病", ""),
    ONE_27("", "27疟疾", ""),
    ONE_28("", "28人感染HI7N9禽流感", ""),
    ONE_40("", "40新冠肺炎", ""),
    ;

    /**
     * 选中的后台需要存的值
     **/
    private final String value;

    /**
     * 显示的文本
     **/
    private final String label;

    /**
     * key
     */
    private final String key;

    DropSelectEnum(String value, String label, String key) {
        this.value = value;
        this.label = label;
        this.key = key;
    }
}
