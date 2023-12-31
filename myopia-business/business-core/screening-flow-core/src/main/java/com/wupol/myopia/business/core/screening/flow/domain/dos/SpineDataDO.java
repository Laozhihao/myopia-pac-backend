package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description 脊柱弯曲
 * @Date 2021/4/06 16:50
 * @Author by xz
 */
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class SpineDataDO extends AbstractDiagnosisResult implements Serializable {
    /**
     * 胸部
     */
    private SpineItem chest;

    /**
     * 腰部
     */
    private SpineItem waist;

    /**
     * 胸腰
     */
    private SpineItem chestWaist;

    /**
     * 前后弯曲
     */
    private SpineItem entirety;

    @Data
    public static class SpineItem {
        /**
         * 非前后弯曲：1：无侧弯。2：左低右高。3：右低左高
         * 前后弯曲：0: type=1时无异常； 1：无前后弯曲异常。2：平背。3：前凸异常，4：后凸异常
         */
        private Integer type;

        /**
         * 程度 1 ，2， 3
         */
        private Integer level;
    }

    public boolean isSpinalCurvature(){
        List<SpineItem> list = Lists.newArrayList();
        if (chest != null){
            list.add(chest);
        }
        if (waist != null){
            list.add(waist);
        }
        if (chestWaist != null){
            list.add(chestWaist);
        }
        Set<SpineItem> spineItemSet = list.stream().filter(item -> !item.getType().equals(1)).collect(Collectors.toSet());
        return CollectionUtil.isNotEmpty(spineItemSet);
    }

}
