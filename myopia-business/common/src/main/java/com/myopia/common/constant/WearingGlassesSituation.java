package com.myopia.common.constant;

import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * @Description
 * @Date 2021/1/22 16:59
 * @Author by Jacob
 */
@UtilityClass
public class WearingGlassesSituation {
    public final String NOT_WEARING_GLASSES_TYPE = "没有佩戴眼镜";
    public final Integer NOT_WEARING_GLASSES_KEY = 0;
    public final String WEARING_FRAME_GLASSES_TYPE = "佩戴框架眼镜";
    public final Integer WEARING_FRAME_GLASSES_KEY = 1;
    public final String WEARING_CONTACT_LENS_TYPE = "佩戴隐形眼睛";
    public final Integer WEARING_CONTACT_LENS_KEY = 2;
    public final String WEARING_OVERNIGHT_ORTHOKERATOLOGY_TYPE = "夜戴角膜塑形镜";
    public final Integer WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY = 3;

    private final ImmutableMap<Integer, String> typeDescriptionMap;
    private final ImmutableMap<String, Integer> descriptionMapType;

    static {
        typeDescriptionMap = ImmutableMap.of(
                NOT_WEARING_GLASSES_KEY, NOT_WEARING_GLASSES_TYPE,
                WEARING_FRAME_GLASSES_KEY, WEARING_FRAME_GLASSES_TYPE,
                WEARING_CONTACT_LENS_KEY, WEARING_CONTACT_LENS_TYPE,
                WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY, WEARING_OVERNIGHT_ORTHOKERATOLOGY_TYPE);
        descriptionMapType = ImmutableMap.of(
                NOT_WEARING_GLASSES_TYPE, NOT_WEARING_GLASSES_KEY,
                WEARING_FRAME_GLASSES_TYPE, WEARING_FRAME_GLASSES_KEY,
                WEARING_CONTACT_LENS_TYPE, WEARING_CONTACT_LENS_KEY,
                WEARING_OVERNIGHT_ORTHOKERATOLOGY_TYPE, WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY);
    }

    /**
     * 查找类型
     *
     * @param key
     * @return
     */
    public Optional<String> getType(Integer key) {
        return Optional.ofNullable(typeDescriptionMap.get(key));
    }

    /**
     * 查找类型
     *
     * @param type
     * @return
     */
    public Optional<Integer> getKey(String type) {
        return Optional.ofNullable(descriptionMapType.get(type));
    }
}
