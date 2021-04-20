package com.wupol.myopia.business.core.hospital.domian.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 角膜地形图检查数据
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class ToscaMedicalRecord {
    /** 学生id */
    private Integer studentId;
    /** 散瞳前 */
    private Tosco nonMydriasis;
    /** 散瞳后 */
    private Tosco mydriasis;

    @Getter
    @Setter
    @Accessors(chain = true)
    public class Tosco {
        /** 学生id */
        private Integer studentId;
        /** 1散瞳前，2散瞳后 */
        private Integer checkType;
        /** 右眼轴位 */
        private String rightAxis;
        /** 左眼轴位 */
        private String leftAxis;
        /** 右眼散光 */
        private String rightAstigmatism;
        /** 左眼散光 */
        private String leftAstigmatism;
        /** 右眼E值1 */
        private String rightE1;
        /** 右眼E值2 */
        private String rightE2;
        /** 左眼E值1 */
        private String leftE1;
        /** 左眼E值2 */
        private String leftE2;
        /** 右眼视力 */
        private String rightVision;
        /** 左眼视力 */
        private String leftVision;
        /** 右眼瞳孔直径 */
        private String rightPupilDiameter;
        /** 左眼瞳孔直径 */
        private String leftPupilDiameter;
        /** 右眼角膜直径 */
        private String rightCornealDiameter;
        /** 左眼角膜直径 */
        private String leftCornealDiameter;
        /** 右眼角膜K1 */
        private String rightSimK1Radius;
        /** 右眼角膜K1 */
        private String rightSimK1Axis;
        /** 左眼角膜K1 */
        private String leftSimK1Radius;
        /** 左眼角膜K1 */
        private String leftSimK1Axis;
        /** 右眼角膜K2 */
        private String rightSimK2Radius;
        /** 右眼角膜K2 */
        private String rightSimK2Axis;
        /** 左眼角膜K2 */
        private String leftSimK2Radius;
        /** 左眼角膜K2 */
        private String leftSimK2Axis;
        /** 备注 */
        private String remark;
        /** 影像列表 */
        private List<Integer> imageIdList;
        /** 影像列表 */
        private List<String> imageUrlList;
    }

}
