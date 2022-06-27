package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.constant.SaprodontiaType;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SaprodontiaDataDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 龋齿统计
 *
 * @Author HaoHao
 * @Date 2022/4/18
 **/
@Accessors(chain = true)
@Data
public class SaprodontiaStat implements Serializable {

    private StatItem deciduous;

    private StatItem permanent;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private static class StatItem implements Serializable {
        /**
         * 为龋（d/D）的牙齿总数
         */
        private Integer dCount;
        /**
         * 为失（m/M）的牙齿总数
         */
        private Integer mCount;
        /**
         * 为补（f/F）的牙齿总数
         */
        private Integer fCount;
    }

    /**
     * 根据龋齿数据统计
     *
     * @param saprodontiaDataDO 龋齿数据
     * @return com.wupol.myopia.business.core.screening.flow.domain.dto.SaprodontiaStat
     **/
    public static SaprodontiaStat parseFromSaprodontiaDataDO(SaprodontiaDataDO saprodontiaDataDO) {
        if (Objects.isNull(saprodontiaDataDO)) {
            return null;
        }
        SaprodontiaStat.StatItem deciduousStat = new SaprodontiaStat.StatItem(0, 0, 0);
        SaprodontiaStat.StatItem permanentStat = new SaprodontiaStat.StatItem(0, 0, 0);
        // 合并上下牙床数据
        List<SaprodontiaDataDO.SaprodontiaItem> allSaprodontiaList = new ArrayList<>();
        allSaprodontiaList.addAll(saprodontiaDataDO.getAbove());
        allSaprodontiaList.addAll(saprodontiaDataDO.getUnderneath());
        // 乳牙
        Map<String, List<String>> deciduousMap = allSaprodontiaList.stream().map(SaprodontiaDataDO.SaprodontiaItem::getDeciduous).filter(StringUtils::hasText).collect(Collectors.groupingBy(Function.identity()));
        if (!CollectionUtils.isEmpty(deciduousMap)) {
            deciduousStat.setDCount(Optional.ofNullable(deciduousMap.get(SaprodontiaType.DECIDUOUS_D.getName())).orElse(Collections.emptyList()).size());
            deciduousStat.setMCount(Optional.ofNullable(deciduousMap.get(SaprodontiaType.DECIDUOUS_M.getName())).orElse(Collections.emptyList()).size());
            deciduousStat.setFCount(Optional.ofNullable(deciduousMap.get(SaprodontiaType.DECIDUOUS_F.getName())).orElse(Collections.emptyList()).size());
        }
        // 恒牙
        Map<String, List<String>> permanentMap = allSaprodontiaList.stream().map(SaprodontiaDataDO.SaprodontiaItem::getPermanent).filter(StringUtils::hasText).collect(Collectors.groupingBy(Function.identity()));
        if (!CollectionUtils.isEmpty(permanentMap)) {
            permanentStat.setDCount(Optional.ofNullable(permanentMap.get(SaprodontiaType.PERMANENT_D.getName())).orElse(Collections.emptyList()).size());
            permanentStat.setMCount(Optional.ofNullable(permanentMap.get(SaprodontiaType.PERMANENT_M.getName())).orElse(Collections.emptyList()).size());
            permanentStat.setFCount(Optional.ofNullable(permanentMap.get(SaprodontiaType.PERMANENT_F.getName())).orElse(Collections.emptyList()).size());
        }
        return new SaprodontiaStat().setDeciduous(deciduousStat).setPermanent(permanentStat);
    }
}
