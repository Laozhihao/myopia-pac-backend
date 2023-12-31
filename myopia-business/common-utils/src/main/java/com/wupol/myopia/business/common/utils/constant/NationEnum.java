package com.wupol.myopia.business.common.utils.constant;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.domain.dto.Nation;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 民族枚举类
 *
 * @author Simple4H
 */
@Getter
public enum NationEnum {
    /** 民族 */
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

    /**
     * 常见民族
     */
    public static final List<NationEnum> COMMON_NATION = Lists.newArrayList(HAN, MONGOLIAN, TIBETAN, ZHUANG, HUI, MANCHU, UYGHUR);

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
        return Arrays.stream(values())
                .map(value -> new Nation(value.toString(),value.getName(),value.getCode()))
                .collect(Collectors.toList());
    }

    /**
     * 根据类型获取描述
     *
     * @param nation 民族
     * @return 描述
     */
    public static String getNameByCode(Integer nation) {
        return Arrays.stream(NationEnum.values())
                .filter(item -> Objects.equals(item.code,nation))
                .findFirst()
                .map(NationEnum::getName)
                .orElse(null);
    }

    /**
     * 通过名称获取code
     *
     * @param name 民族名称
     * @return code 民族code
     */
    public static Integer getCodeByName(String name) {
        return Arrays.stream(NationEnum.values())
                .filter(item -> Objects.equals(item.name,name))
                .findFirst()
                .map(NationEnum::getCode)
                .orElse(null);
    }
}
