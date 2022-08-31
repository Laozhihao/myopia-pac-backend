package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @Description 误差说明
 * @Date 2021/4/012 16:50
 * @Author by xz
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class DeviationDO extends AbstractDiagnosisResult implements Serializable{
    /**
     * 视力或屈光检查误差误差
     */
    private VisionOrOptometryDeviation visionOrOptometryDeviation;


    /**
     * 身高体重误差
     */
    private HeightWeightDeviation heightWeightDeviation;

    @Data
    @Accessors(chain = true)
    public static class VisionOrOptometryDeviation implements Serializable {

        private VisionOrOptometryDeviationEnum type;

        /**
         * 视力或屈光检查误差说明
         */
        private String remark;
    }

    @Data
    @Accessors(chain = true)
    public static class HeightWeightDeviation implements Serializable {

        private HeightWeightDeviationEnum type;

        /**
         * 身高体重误差 说明
         */
        private String remark;
    }

    /**
     * 视力或屈光检查误差误差
     */
    @Getter
    @AllArgsConstructor
    public enum VisionOrOptometryDeviationEnum {
        /**
         * 测量仪器问题
         */
        ONE(1, "测量仪器问题"),
        /**
         * 学生配合问题
         */
        TWO(2, "学生配合问题"),
        /**
         * 学生眼睛疲劳影像
         */
        THREE(3, "学生眼睛疲劳影像"),
        /**
         * 其他
         */
        FOUR(4, "其他");
        /**
         * code
         */
        private final Integer code;
        /**
         * name
         */
        private final String name;

        public static VisionOrOptometryDeviationEnum getByCode(Integer code) {
            return code == null ? null : Arrays.stream(values()).filter(item -> code.equals(item.code)).findFirst().orElse(null);
        }
    }

    /**
     * 身高体重误差 说明
     */
    @Getter
    @AllArgsConstructor
    public enum HeightWeightDeviationEnum {
        /**
         * 测量仪器问题
         */
        ONE(1, "测量仪器问题"),
        /**
         * 穿着袜子或鞋子测量
         */
        TWO(2, "穿着袜子或鞋子测量"),
        /**
         * 衣着重量问题
         */
        THREE(3, "衣着重量问题"),
        /**
         * 其他
         */
        FOUR(4, "其他");
        /**
         * code
         */
        private final Integer code;
        /**
         * name
         */
        private final String name;

        public static HeightWeightDeviationEnum getByCode(Integer code) {
            return code == null ? null : Arrays.stream(values()).filter(item -> code.equals(item.code)).findFirst().orElse(null);
        }
    }
}
