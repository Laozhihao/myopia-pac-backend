package com.wupol.myopia.business.common.constant;

import com.google.common.collect.ImmutableMap;
import com.wupol.myopia.business.common.exceptions.ManagementUncheckedException;
import com.wupol.framework.core.util.StringUtils;
import lombok.experimental.UtilityClass;

/**
 * @Description
 * @Date 2021/1/22 16:59
 * @Author by Jacob
 */
@UtilityClass
public class WearingGlassesSituation {
    public final String NOT_WEARING_GLASSES_TYPE = "没有佩戴眼镜";
    public final Integer NOT_WEARING_GLASSES_KEY = GlassesType.NOT_WEARING.code;
    public final String WEARING_FRAME_GLASSES_TYPE = "佩戴框架眼镜";
    public final Integer WEARING_FRAME_GLASSES_KEY = GlassesType.FRAME_GLASSES.code;
    public final String WEARING_CONTACT_LENS_TYPE = "佩戴隐形眼镜";
    public final Integer WEARING_CONTACT_LENS_KEY = GlassesType.CONTACT_LENS.code;
    public final String WEARING_OVERNIGHT_ORTHOKERATOLOGY_TYPE = "夜戴角膜塑形镜";
    public final Integer WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY = GlassesType.ORTHOKERATOLOGY.code;

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
    public String getType(Integer key) {
        String glassesTypeStr = typeDescriptionMap.get(key);
        if (StringUtils.isBlank(glassesTypeStr)) {
            throw new ManagementUncheckedException("无法找到该戴镜类型 key = " + key);
        }
        return glassesTypeStr;
    }

    /**
     * 查找类型
     *
     * @param type
     * @return
     */
    public Integer getKey(String type) {
        Integer glassesTypeKey = descriptionMapType.get(type);
        if (glassesTypeKey == null) {
            throw new ManagementUncheckedException("无法找到该戴镜类型 type = " + type);
        }
        return glassesTypeKey;
    }
}
