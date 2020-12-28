package com.wupol.myopia.business.management.constant;

import com.wupol.myopia.business.management.domain.model.Nation;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 民族枚举类
 *
 * @author Simple4H
 */
@Getter
public enum NationEnum {

    HAN(1, "汉"),
    ZHUANG(2, "壮"),
    MANCHU(3, "满"),
    HUI(4, "回"),
    MIAO(5, "苗"),
    UYGHUR(6, "维吾尔"),
    TUJIA(7, "土家"),
    YI(8, "彝"),
    MONGOLIAN(9, "蒙古"),
    TIBETAN(10, "藏"),
    BUYEI(11, "布依"),
    DONG(12, "侗"),
    YAO(13, "瑶"),
    KOREAN(14, "朝鲜"),
    BAI(15, "白"),
    HANI(16, "哈尼"),
    KAZAKH(17, "哈萨克"),
    LI(18, "黎"),
    DAI(19, "傣"),
    SHE(20, "畲"),
    LISU(21, "傈僳"),
    GELAO(22, "仡佬"),
    DONGXIANG(23, "东乡"),
    GAOSHAN(24, "高山"),
    LAHU(25, "拉祜族"),
    SHUI(26, "水"),
    VA(27, "佤"),
    NAKHI(28, "纳西"),
    QIANG(29, "羌"),
    MONGUOR(30, "土"),
    MULAO(31, "仫佬"),
    XIBE(32, "锡伯"),
    KYRGYZ(33, "柯尔克孜"),
    DAUR(34, "达斡尔"),
    JINGPO(35, "景颇"),
    MAONAN(36, "毛南"),
    SALAR(37, "撒拉"),
    BLANG(38, "布朗"),
    TAJIK(39, "塔吉克"),
    ACHANG(40, "阿昌"),
    PUMI(41, "普米"),
    EVENK(42, "鄂温克"),
    NU(43, "怒"),
    KINH(44, "京"),
    JINO(45, "基诺"),
    DEANG(46, "德昂"),
    BONAN(47, "保安"),
    RUSSIAN(48, "俄罗斯"),
    YUGHUR(49, "裕固"),
    UZBEK(50, "乌孜别克"),
    MONPA(51, "门巴"),
    OROQEN(52, "鄂伦春"),
    DERUNG(53, "独龙"),
    TATAR(54, "塔塔尔"),
    NANAI(55, "赫哲"),
    LHOBA(56, "珞巴");

    private final Integer code;

    private final String name;

    NationEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

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
}
