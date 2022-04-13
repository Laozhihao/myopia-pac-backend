package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Arrays;

/**
 * @Description 误差说明
 * @Date 2021/4/012 16:50
 * @Author by xz
 */
@Data
public class DeviationDO {
    /**
     * 视光检查误差
     */
    private EyesightDeviationEnum eyesightDeviation;

    /**
     * 视光检查误差 说明
     */
    private String eyesightDeviationRemark;

    /**
     * 身高体重误差
     */
    private HeightWeightDeviationEnum heightWeightDeviation;

    /**
     * 身高体重误差 说明
     */
    private String heightWeightDeviationRemark;

    @Getter
    @AllArgsConstructor
    public enum EyesightDeviationEnum {
        ONE(1, "测量仪器问题"),
        TWO(2, "学生配合问题"),
        THREE(3, "学生眼睛疲劳影像"),
        FOUR(4, "其他");
        private final Integer code;
        private final String name;

        public static EyesightDeviationEnum getByCode(Integer code) {
            return code == null ? null : Arrays.stream(values()).filter((item) -> code.equals(item.code)).findFirst().orElse(null);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum HeightWeightDeviationEnum {
        ONE(1, "测量仪器问题"),
        TWO(2, "穿着袜子或鞋子测量"),
        THREE(3, "衣着重量问题"),
        FOUR(4, "其他");
        private final Integer code;
        private final String name;

        public static HeightWeightDeviationEnum getByCode(Integer code) {
            return code == null ? null : Arrays.stream(values()).filter((item) -> code.equals(item.code)).findFirst().orElse(null);
        }
    }
}
