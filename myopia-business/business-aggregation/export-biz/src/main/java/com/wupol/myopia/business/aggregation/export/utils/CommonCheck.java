package com.wupol.myopia.business.aggregation.export.utils;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ListUtil;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 通用校验
 *
 * @author Simple4H
 */
public class CommonCheck {

    /**
     * 检查身份证、学号、护照是否重复
     *
     * @param idCards  身份证
     * @param snoList  学号
     * @param isSchool 是否学校端
     */
    public static void checkHaveDuplicate(List<String> idCards, List<String> snoList, List<String> passports, Boolean isSchool) {

        if (isSchool && CollectionUtils.isEmpty(snoList)) {
            throw new BusinessException("学号为空");
        }

        if (!CollectionUtils.isEmpty(idCards)) {
            List<String> duplicateList = ListUtil.getDuplicateElements(idCards);
            if (!CollectionUtils.isEmpty(duplicateList)) {
                throw new BusinessException("身份证号码：" + String.join(",", duplicateList) + "重复");
            }
        }
        if (!CollectionUtils.isEmpty(passports)) {
            List<String> duplicateList = ListUtil.getDuplicateElements(passports);
            if (!CollectionUtils.isEmpty(duplicateList)) {
                throw new BusinessException("护照：" + String.join(",", duplicateList) + "重复");
            }
        }
        List<String> snoDuplicate = ListUtil.getDuplicateElements(snoList);
        if (!CollectionUtils.isEmpty(snoDuplicate)) {
            throw new BusinessException("学号：" + String.join(",", snoDuplicate) + "重复");
        }
    }
}
