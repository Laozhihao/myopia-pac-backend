package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.wupol.myopia.business.api.management.constant.ReportConst;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * 实体方法
 *
 * @author hang.yuan 2022/5/30 22:03
 */
public class EntityFunction {


    public <T> Integer getCount(List<T> statConclusionList, Function<T,Boolean> function){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return ReportConst.ZERO;
        }
        return (int)statConclusionList.stream().map(function).filter(Objects::nonNull).filter(Boolean::booleanValue).count();
    }
    public <T> Integer getCount(List<T> statConclusionList, Function<T,Integer> mapper,Integer value){
        if (CollectionUtil.isEmpty(statConclusionList)){
            return ReportConst.ZERO;
        }
        return (int)statConclusionList.stream().filter(sc->Objects.equals(mapper.apply(sc),value)).count();
    }
    public BigDecimal getRatioNotSymbol(Integer numerator, Integer denominator) {
        return Optional.ofNullable(MathUtil.ratioNotSymbol(numerator,denominator)).orElse(ReportConst.ZERO_BIG_DECIMAL);
    }
    public String getRatio(Integer numerator,Integer denominator) {
        return Optional.ofNullable(MathUtil.ratio(numerator,denominator)).orElse(ReportConst.ZERO_RATIO_STR);
    }

}
