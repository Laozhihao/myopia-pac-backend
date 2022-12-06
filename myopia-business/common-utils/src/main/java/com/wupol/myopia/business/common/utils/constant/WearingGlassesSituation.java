package com.wupol.myopia.business.common.utils.constant;

import com.google.common.collect.ImmutableMap;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * @Description
 * @Date 2021/1/22 16:59
 * @Author by Jacob
 */
@UtilityClass
public class WearingGlassesSituation {
    public final String NOT_WEARING_GLASSES_TYPE = "不佩戴眼镜";
    public final Integer NOT_WEARING_GLASSES_KEY = GlassesTypeEnum.NOT_WEARING.code;
    public final String WEARING_FRAME_GLASSES_TYPE = "佩戴框架眼镜";
    public final Integer WEARING_FRAME_GLASSES_KEY = GlassesTypeEnum.FRAME_GLASSES.code;
    public final String WEARING_CONTACT_LENS_TYPE = "佩戴隐形眼镜";
    public final Integer WEARING_CONTACT_LENS_KEY = GlassesTypeEnum.CONTACT_LENS.code;
    public final String WEARING_OVERNIGHT_ORTHOKERATOLOGY_TYPE = "夜戴角膜塑形镜";
    public final Integer WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY = GlassesTypeEnum.ORTHOKERATOLOGY.code;

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

    public ImmutableMap<Integer, String> getTypeDescriptionMap(){
        return typeDescriptionMap;
    }

    /**
     * 查找类型
     *
     * @param key
     * @return
     */
    public static String getType(Integer key) {
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

    public Boolean checkKeyByDesc(String type) {
        Integer glassesTypeKey = descriptionMapType.get(type);
        return Objects.isNull(glassesTypeKey);
    }


}
