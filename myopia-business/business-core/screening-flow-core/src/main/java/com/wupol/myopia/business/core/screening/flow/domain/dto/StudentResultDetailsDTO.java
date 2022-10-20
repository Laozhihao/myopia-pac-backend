package com.wupol.myopia.business.core.screening.flow.domain.dto;

import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentResultDetailsDTO implements Serializable {

    /**
     * 0 为左眼 1 为右眼
     */
    private Integer lateriality;
    /**
     * 佩戴眼镜的类型： @{link com.wupol.myopia.business.common.constant.WearingGlassesSituation}
     */
    private Integer glassesType;
    /**
     * 佩戴眼镜的类型描述： @{link com.wupol.myopia.business.common.constant.WearingGlassesSituation}
     */
    private String glassesTypeDes;
    /**
     * 夜戴角膜塑形镜的度数
     */
    private BigDecimal okDegree;

    /**
     * 矫正视力
     */
    private BigDecimal correctedVision;

    /**
     * 裸眼视力
     */
    private BigDecimal nakedVision;

    /**
     * 轴位
     */
    private BigDecimal axial;

    /**
     * 球镜
     */
    private BigDecimal sph;

    /**
     * 等效球镜
     */
    private BigDecimal se;

    /**
     * 柱镜
     */
    private BigDecimal cyl;

    /**
     * 房水深度（前房深度）
     */
    private String ad;

    /**
     * 眼轴
     */
    private String al;

    /**
     * 角膜中央厚度
     */
    private String cct;

    /**
     * 晶状体厚度
     */
    private String lt;

    /**
     * 角膜白到白距离
     */
    private String wtw;

    /**
     * 眼部疾病
     */
    private String eyeDiseases;

    /**
     * 是否远视
     */
    private Boolean isHyperopia;

    /**
     * 身高体重
     */
    private HeightAndWeightDataDO heightAndWeightData;


    /**
     * 客户端ID
     */
    private Integer clientId;


    /**
     * 夜戴角膜塑形镜的描述
     */
    private String okDegreeDesc;


    public Object getCorrectedVision() {
        return getValue(clientId,correctedVision,1);
    }

    public Object getNakedVision() {
        return getValue(clientId,nakedVision,1);
    }

    public Object getSe() {
        if (Objects.nonNull(clientId) && Objects.equals(clientId,SystemCode.SCHOOL_CLIENT.getCode())){
            if (Objects.isNull(se)){
                return se;
            }
            String hyperopiaText = Objects.equals(isHyperopia, Boolean.TRUE) ? "远视" : StrUtil.EMPTY;
            String myopiaText = BigDecimalUtil.lessThan(se, "0") ? "近视" : hyperopiaText;
            String text = "%sD，%s%s度";
            String bigDecimalStr = BigDecimalUtil.getBigDecimalStr(se, 2);
            String degreeValue = BigDecimalUtil.getBigDecimalStr(BigDecimalUtil.multiply(se, "100"),0,Boolean.TRUE);
            return String.format(text,bigDecimalStr,myopiaText,degreeValue);
        }
        return se;
    }

    public Object getSph() {
        if (Objects.nonNull(clientId) && Objects.equals(clientId,SystemCode.SCHOOL_CLIENT.getCode())){
            if (Objects.isNull(sph)){
                return sph;
            }
            String bigDecimalStr = BigDecimalUtil.getBigDecimalStr(sph, 2);
            return BigDecimalUtil.getText(bigDecimalStr,Boolean.TRUE);
        }
        return sph;
    }

    public Object getCyl() {
        if (Objects.nonNull(clientId) && Objects.equals(clientId,SystemCode.SCHOOL_CLIENT.getCode())){
            if (Objects.isNull(cyl)){
                return cyl;
            }
            String text = "%sD，%s%s度";
            String descText = BigDecimalUtil.isBetweenNo(cyl, "0.5", "0.5") ? StrUtil.EMPTY : "散光";
            String bigDecimalStr = BigDecimalUtil.getBigDecimalStr(cyl, 2);
            String degreeValue = BigDecimalUtil.getBigDecimalStr(BigDecimalUtil.multiply(cyl, "100"),0,Boolean.TRUE);
            return String.format(text,bigDecimalStr,descText,degreeValue);
        }
        return cyl;
    }

    public Object getAxial() {
        if (Objects.nonNull(clientId)&& Objects.equals(clientId,SystemCode.SCHOOL_CLIENT.getCode())) {
            if (Objects.isNull(axial)){
                return axial;
            }
            String text = "%s，散光的轴位为%s度方向";
            return String.format(text,axial,axial);
        }

        return axial;
    }

    private Object getValue(Integer clientId, BigDecimal value, int scale){
        if (Objects.nonNull(clientId) && Objects.equals(clientId,SystemCode.SCHOOL_CLIENT.getCode())){
            if (Objects.isNull(value)){
                return value;
            }
            return BigDecimalUtil.getBigDecimalStr(value, scale,Boolean.FALSE);
        }
        return value;
    }

}
