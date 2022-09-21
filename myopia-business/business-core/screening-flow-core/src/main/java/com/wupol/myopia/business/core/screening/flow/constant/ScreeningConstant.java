package com.wupol.myopia.business.core.screening.flow.constant;

import lombok.experimental.UtilityClass;

/**
 * @Author wulizhou
 * @Date 2021/5/26 16:45
 */
@UtilityClass
public class ScreeningConstant {

    public static final Integer NO_EXIST_NOTICE = 0;
    
    /** 筛查数据类型，区分是哪种类型的数据，视力*/
    public static final String SCREENING_DATA_TYPE_VISION = "vision";
    /** 筛查数据类型，区分是哪种类型的数据，电脑验光*/
    public static final String SCREENING_DATA_TYPE_COMPUTER_OPTOMETRY = "computer_optometry";
    /** 筛查数据类型，区分是哪种类型的数据，复合检查数据*/
    public static final String SCREENING_DATA_TYPE_MULTI_CHECK = "multi_check";
    /** 筛查数据类型，区分是哪种类型的数据，生物测量 */
    public static final String SCREENING_DATA_TYPE_BIOMETRIC = "biometric";
    /** 筛查数据类型，区分是哪种类型的数据，小瞳验光 */
    public static final String SCREENING_DATA_TYPE_PUPIL_OPTOMETRY = "pupil_optometry";
    /** 筛查数据类型，区分是哪种类型的数据，眼压 */
    public static final String SCREENING_DATA_TYPE_EYE_PRESSURE = "eye_pressure";
    /** 筛查数据类型，区分是哪种类型的数据，其他眼病 */
    public static final String SCREENING_DATA_TYPE_OTHER_EYE_DISEASE = "other_eye_disease";
    /** 筛查数据类型，区分是哪种类型的数据，身高体重 */
    public static final String SCREENING_DATA_TYPE_HEIGHT_WEIGHT = "height_weight";
    /** 筛查数据类型，区分是哪种类型的数据，误差说明 */
    public static final String SCREENING_DATA_TYPE_DEVIATION = "deviation";
    /** 筛查数据类型，区分是哪种类型的数据，龋齿 */
    public static final String SCREENING_DATA_TYPE_SAPRODONTIA = "saprodontia";
    /** 筛查数据类型，区分是哪种类型的数据，脊柱 */
    public static final String SCREENING_DATA_TYPE_SPINE = "spine";
    /** 筛查数据类型，区分是哪种类型的数据，血压 */
    public static final String SCREENING_DATA_TYPE_BLOOD_PRESSURE = "blood_pressure";
    /** 筛查数据类型，区分是哪种类型的数据，疾病史 */
    public static final String SCREENING_DATA_TYPE_DISEASES_HISTORY = "diseases_history";
    /** 筛查数据类型，区分是哪种类型的数据，个人隐私 */
    public static final String SCREENING_DATA_TYPE_PRIVACY = "privacy";
    /** 筛查数据类型，区分是哪种类型的数据，眼位 */
    public static final String SCREENING_DATA_TYPE_OCULAR_INSPECTION = "ocular_inspection";
    /** 筛查数据类型，区分是哪种类型的数据，眼底*/
    public static final String SCREENING_DATA_TYPE_FUNDUS = "fundus";
    /** 筛查数据类型，区分是哪种类型的数据，裂隙灯*/
    public static final String SCREENING_DATA_TYPE_SLIT_LAMP = "slit_lamp";
    /** 筛查数据类型，区分是哪种类型的数据，盲及视力损害*/
    public static final String SCREENING_DATA_TYPE_VISUAL_LOSS_LEVEL = "visual_loss_level";

}
