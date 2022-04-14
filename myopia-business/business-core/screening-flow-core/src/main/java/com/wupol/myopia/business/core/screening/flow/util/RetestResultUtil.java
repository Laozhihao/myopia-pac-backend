package com.wupol.myopia.business.core.screening.flow.util;
import com.wupol.myopia.business.core.screening.flow.domain.dto.RetestResultCard;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.experimental.UtilityClass;


/*
 * @Author  钓猫的小鱼
 * @Date  2022/4/13 20:42
 * @Email: shuailong.wu@vistel.cn
 * @Des: 复测工具类
 */

@UtilityClass
public class RetestResultUtil {

    /**
     * 复测卡结果
     * @param screeningResult 初筛结果
     * @param retestResult 复测结果
     * @return 复测卡工具类
     */
    public static RetestResultCard retestResultCard(VisionScreeningResult screeningResult, VisionScreeningResult retestResult){
        RetestResultCard retestResultCard = new RetestResultCard();
//        retestResultCard.setGlassesType(1);
//        retestResultCard.setDeviationData(retestResult.getDeviationData());

        return retestResultCard;
    }
}

