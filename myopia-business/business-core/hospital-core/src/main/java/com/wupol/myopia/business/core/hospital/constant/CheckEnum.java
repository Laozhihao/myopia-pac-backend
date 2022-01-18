package com.wupol.myopia.business.core.hospital.constant;

/**
 * @Author wulizhou
 * @Date 2022/1/18 11:49
 */
public enum CheckEnum {

    OUTER_EYE( "眼外观"),
    EYE_DISEASE_FACTOR( "主要眼部高危因素"),
    LIGHT_REACTION( "光照反应"),
    BLINK_REFLEX( "瞬目反射"),
    RED_BALL_TEST( "红球试验"),
    VISUAL_BEHAVIOR_OBSERVATION( "视物行为观察"),
    RED_REFLEX( "红光反射"),
    OCULAR_INSPECTION( "眼位检查"),
    VISION_DATA( "视力检查"),
    MONOCULAR_MASKING_AVERSION_TEST( "单眼遮盖厌恶试验"),
    REFRACTION_DATA( "屈光检查"),


    ;
    /**
     * 描述
     **/
    private final String name;

    CheckEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
