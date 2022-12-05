package com.wupol.myopia.business.aggregation.export.utils;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ListUtil;
import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 通用校验
 *
 * @author Simple4H
 */
@UtilityClass
public class CommonCheck {


    /**
     * 检查身份证、学号、护照是否重复
     *
     * @param idCards  身份证
     * @param snoList  学号
     * @param isSchool 是否学校端
     */
    public static void checkHaveDuplicate(List<String> idCards, List<String> snoList, List<String> passports, Boolean isSchool) {

        if (Objects.equals(isSchool, Boolean.TRUE) && CollectionUtils.isEmpty(snoList)) {
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

    /**
     * 检查学号长度限制
     *
     * @param snoList 学号
     */
    public static void checkSnoLength(List<String> snoList) {

        //处理护照异常
        List<String> errorList = snoList.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.length() > 25)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorList)) {
            throw new BusinessException(String.format("学号异常:%s", errorList));
        }
    }
}
