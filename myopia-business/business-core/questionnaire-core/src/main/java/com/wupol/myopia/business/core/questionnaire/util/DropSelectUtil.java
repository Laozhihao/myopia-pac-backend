package com.wupol.myopia.business.core.questionnaire.util;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.core.questionnaire.domain.dos.DropSelect;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 下拉工具类
 *
 * @author Simple4H
 */
@UtilityClass
public class DropSelectUtil {

    /**
     * 甲乙类
     *
     * @return 甲乙类
     */
    public static List<DropSelect> one() {
        ArrayList<String> list = Lists.newArrayList("01鼠疫", "02霍乱", "03传染性非典型肺炎", "04艾滋病", "05病毒性肝炎", "06脊髓灰质炎", "07人感染高致病性禽流感", "08麻疹", "09流行性出血热", "10狂犬病", "11流行性乙型脑炎", "12登革热", "13炭疽", "14痢疾", "15肺结核", "16伤寒", "17流行性脑脊髓膜炎", "18百日咳", "19白喉", "20新生儿破伤风", "21猩红热", "22布鲁氏菌病", "23淋病", "24梅毒", "25钩端螺旋体病", "26血吸虫病", "27疟疾", "28人感染HI7N9禽流感", "40新冠肺炎");
        return list.stream().map(s -> new DropSelect(s, s)).collect(Collectors.toList());
    }

    /**
     * 丙类
     *
     * @return 丙类
     */
    public static List<DropSelect> two() {
        ArrayList<String> list = Lists.newArrayList("29流行性感冒", "30流行性腮腺炎", "31风疹", "32急性出血性结膜炎", "33麻风病", "34流行性和地方性斑疹伤寒", "35黑热病", "36包虫病", "37丝虫病", "38伤寒和副伤寒以外的感染性腹泻病", "39手足口病");
        return list.stream().map(s -> new DropSelect(s, s)).collect(Collectors.toList());
    }
}
