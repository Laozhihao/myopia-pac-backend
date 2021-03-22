package com.wupol.myopia.business.management.constant;

import com.wupol.myopia.business.management.domain.model.Nation;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 民族枚举类
 *
 * @author Simple4H
 */
@Getter
public enum NationEnum {

    HAN(1, "汉族"),
    ZHUANG(2, "壮族"),
    MANCHU(3, "满族"),
    HUI(4, "回族"),
    MIAO(5, "苗族"),
    UYGHUR(6, "维吾尔族"),
    TUJIA(7, "土家族"),
    YI(8, "彝族"),
    MONGOLIAN(9, "蒙古族"),
    TIBETAN(10, "藏族"),
    BUYEI(11, "布依族"),
    DONG(12, "侗族"),
    YAO(13, "瑶族"),
    KOREAN(14, "朝鲜族"),
    BAI(15, "白族"),
    HANI(16, "哈尼族"),
    KAZAKH(17, "哈萨克族"),
    LI(18, "黎族"),
    DAI(19, "傣族"),
    SHE(20, "畲族"),
    LISU(21, "傈僳族"),
    GELAO(22, "仡佬族"),
    DONGXIANG(23, "东乡族"),
    GAOSHAN(24, "高山族"),
    LAHU(25, "拉祜族族"),
    SHUI(26, "水族"),
    VA(27, "佤族"),
    NAKHI(28, "纳西族"),
    QIANG(29, "羌族"),
    MONGUOR(30, "土族"),
    MULAO(31, "仫佬族"),
    XIBE(32, "锡伯族"),
    KYRGYZ(33, "柯尔克孜族"),
    DAUR(34, "达斡尔族"),
    JINGPO(35, "景颇族"),
    MAONAN(36, "毛南族"),
    SALAR(37, "撒拉族"),
    BLANG(38, "布朗族"),
    TAJIK(39, "塔吉克族"),
    ACHANG(40, "阿昌族"),
    PUMI(41, "普米族"),
    EVENK(42, "鄂温克族"),
    NU(43, "怒族"),
    KINH(44, "京族"),
    JINO(45, "基诺族"),
    DEANG(46, "德昂族"),
    BONAN(47, "保安族"),
    RUSSIAN(48, "俄罗斯族"),
    YUGHUR(49, "裕固族"),
    UZBEK(50, "乌孜别克族"),
    MONPA(51, "门巴族"),
    OROQEN(52, "鄂伦春族"),
    DERUNG(53, "独龙族"),
    TATAR(54, "塔塔尔族"),
    NANAI(55, "赫哲族"),
    LHOBA(56, "珞巴族"),
    OTHER(57, "其他");

    private final Integer code;

    private final String name;

    NationEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取民族列表
     *
     * @return 民族列表
     */
    public static List<Nation> getNationList() {
        List<Nation> nationLists = new ArrayList<>();
        for (NationEnum value : values()) {
            Nation nation = new Nation();
            nation.setCode(value.getCode());
            nation.setCnName(value.getName());
            nation.setEnName(value.toString());
            nationLists.add(nation);
        }
        return nationLists;
    }

    /**
     * 根据类型获取描述
     *
     * @param nation 民族
     * @return 描述
     */
    public static String getName(Integer nation) {
        NationEnum h = Arrays.stream(NationEnum.values()).filter(item -> item.code.equals(nation)).findFirst().orElse(null);
        return Objects.nonNull(h) ? h.name : null;
    }

    /**
     * 通过名称获取code
     *
     * @param name 民族名称
     * @return code 民族code
     */
    public static Integer getCode(String name) {
        NationEnum h = Arrays.stream(NationEnum.values())
                .filter(item -> item.name.equals(name))
                .findFirst().orElse(null);
        return Objects.nonNull(h) ? h.code : null;
    }
}
