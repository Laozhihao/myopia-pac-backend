package com.wupol.myopia.business.common.constant;

import com.google.common.collect.ImmutableList;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * @Description
 * @Date 2021/1/29 10:30
 * @Author by Jacob
 */
@UtilityClass
public class EyeDiseasesEnum {
    public final List<String> eyeDiseaseList;

    static {
        eyeDiseaseList = ImmutableList.of("内显斜", "外显斜", "内隐斜", "外隐斜", "交替性斜视", "眼角白斑", "小眼球", "眼睑下垂", "弱视", "白内障", "外伤", "先天异常", "其他");
    }
}
