package com.wupol.myopia.business.core.questionnaire.constant;

import com.wupol.myopia.business.core.questionnaire.domain.dos.DropSelect;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 下拉选择
 *
 * @author Simple4H
 */
@Getter
public enum DropSelectEnum {

    // 甲乙病
    SELECT_1("01", "01鼠疫", SelectKeyEnum.KEY_1.getKey()),
    SELECT_2("02", "02霍乱", SelectKeyEnum.KEY_1.getKey()),
    SELECT_3("03", "03传染性非典型肺炎", SelectKeyEnum.KEY_1.getKey()),
    SELECT_4("04", "04艾滋病", SelectKeyEnum.KEY_1.getKey()),
    SELECT_5("05", "05病毒性肝炎", SelectKeyEnum.KEY_1.getKey()),
    SELECT_6("06", "06脊髓灰质炎", SelectKeyEnum.KEY_1.getKey()),
    SELECT_7("07", "07人感染高致病性禽流感", SelectKeyEnum.KEY_1.getKey()),
    SELECT_8("08", "08麻疹", SelectKeyEnum.KEY_1.getKey()),
    SELECT_9("09", "09流行性出血热", SelectKeyEnum.KEY_1.getKey()),
    SELECT_10("10", "10狂犬病", SelectKeyEnum.KEY_1.getKey()),
    SELECT_11("11", "11流行性乙型脑炎", SelectKeyEnum.KEY_1.getKey()),
    SELECT_12("12", "12登革热", SelectKeyEnum.KEY_1.getKey()),
    SELECT_13("13", "13炭疽", SelectKeyEnum.KEY_1.getKey()),
    SELECT_14("14", "14痢疾", SelectKeyEnum.KEY_1.getKey()),
    SELECT_15("15", "15肺结核", SelectKeyEnum.KEY_1.getKey()),
    SELECT_16("16", "16伤寒", SelectKeyEnum.KEY_1.getKey()),
    SELECT_17("17", "17流行性脑脊髓膜炎", SelectKeyEnum.KEY_1.getKey()),
    SELECT_18("18", "18百日咳", SelectKeyEnum.KEY_1.getKey()),
    SELECT_19("19", "19白喉", SelectKeyEnum.KEY_1.getKey()),
    SELECT_20("20", "20新生儿破伤风", SelectKeyEnum.KEY_1.getKey()),
    SELECT_21("21", "21猩红热", SelectKeyEnum.KEY_1.getKey()),
    SELECT_22("22", "22布鲁氏菌病", SelectKeyEnum.KEY_1.getKey()),
    SELECT_23("23", "23淋病", SelectKeyEnum.KEY_1.getKey()),
    SELECT_24("24", "24梅毒", SelectKeyEnum.KEY_1.getKey()),
    SELECT_25("25", "25钩端螺旋体病", SelectKeyEnum.KEY_1.getKey()),
    SELECT_26("26", "26血吸虫病", SelectKeyEnum.KEY_1.getKey()),
    SELECT_27("27", "27疟疾", SelectKeyEnum.KEY_1.getKey()),
    SELECT_28("28", "28人感染HI7N9禽流感", SelectKeyEnum.KEY_1.getKey()),
    SELECT_29("40", "40新冠肺炎", SelectKeyEnum.KEY_1.getKey()),

    // 丙病
    SELECT_30("29", "29流行性感冒", SelectKeyEnum.KEY_2.getKey()),
    SELECT_31("30", "30流行性腮腺炎", SelectKeyEnum.KEY_2.getKey()),
    SELECT_32("31", "31风疹", SelectKeyEnum.KEY_2.getKey()),
    SELECT_33("32", "32急性出血性结膜炎", SelectKeyEnum.KEY_2.getKey()),
    SELECT_34("33", "33麻风病", SelectKeyEnum.KEY_2.getKey()),
    SELECT_35("34", "34流行性和地方性斑疹伤寒", SelectKeyEnum.KEY_2.getKey()),
    SELECT_36("35", "35黑热病", SelectKeyEnum.KEY_2.getKey()),
    SELECT_37("36", "36包虫病", SelectKeyEnum.KEY_2.getKey()),
    SELECT_38("37", "37丝虫病", SelectKeyEnum.KEY_2.getKey()),
    SELECT_39("38", "38伤寒和副伤寒以外的感染性腹泻病", SelectKeyEnum.KEY_2.getKey()),
    SELECT_40("39", "39手足口病", SelectKeyEnum.KEY_2.getKey()),

    // 教师-类别
    SELECT_41("1", "卫生专业技术人员", SelectKeyEnum.KEY_3.getKey()),
    SELECT_42("2", "保健教师", SelectKeyEnum.KEY_3.getKey()),

    // 教师-专/兼职
    SELECT_43("1", "专职", SelectKeyEnum.KEY_4.getKey()),
    SELECT_44("2", "兼职", SelectKeyEnum.KEY_4.getKey()),

    // 教师-学历
    SELECT_45("1", "中专/高中", SelectKeyEnum.KEY_5.getKey()),
    SELECT_46("2", "大专", SelectKeyEnum.KEY_5.getKey()),
    SELECT_47("3", "本科", SelectKeyEnum.KEY_5.getKey()),
    SELECT_48("4", "硕士及以上", SelectKeyEnum.KEY_5.getKey()),

    // 教师-职称
    SELECT_49("1", "无", SelectKeyEnum.KEY_6.getKey()),
    SELECT_50("2", "初级", SelectKeyEnum.KEY_6.getKey()),
    SELECT_51("3", "中级", SelectKeyEnum.KEY_6.getKey()),
    SELECT_52("4", "副高级", SelectKeyEnum.KEY_6.getKey()),
    SELECT_53("5", "正高级", SelectKeyEnum.KEY_6.getKey()),

    // 教师-执业资格证书
    SELECT_54("1", "无证", SelectKeyEnum.KEY_7.getKey()),
    SELECT_55("2", "教师证", SelectKeyEnum.KEY_7.getKey()),
    SELECT_56("3", "护士执业证", SelectKeyEnum.KEY_7.getKey()),
    SELECT_57("4", "临床执业医师资格证", SelectKeyEnum.KEY_7.getKey()),
    SELECT_58("5", "公卫执业医师资格证", SelectKeyEnum.KEY_7.getKey()),
    SELECT_59("6", "其他", SelectKeyEnum.KEY_7.getKey()),


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

    /**
     * 通过key获取
     *
     * @param key key
     *
     * @return List<DropSelect>
     */
    public static List<DropSelect> getSelect(String key) {
        return Arrays.stream(DropSelectEnum.values())
                .filter(item -> item.key.equals(key))
                .map(s -> new DropSelect(s.getLabel(), s.getValue()))
                .collect(Collectors.toList());

    }

    /**
     * 通过value获取
     * @param value value
     */
    public static DropSelectEnum getDropSelect(String value) {
        return Arrays.stream(DropSelectEnum.values())
                .filter(item -> item.value.equals(value))
                .findFirst().orElse(null);

    }
}
