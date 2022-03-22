package com.wupol.myopia.base.util;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据校验工具
 *
 * @author Simple4H
 */
public class ValidatorUtils {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * bean整体校验，有不合规范，抛出第1个违规异常
     */
    public static void validate(Object obj, Class<?>... groups) {
        Set<ConstraintViolation<Object>> resultSet = validator.validate(obj, groups);
        if (resultSet.size() > 0) {
            //如果存在错误结果，则将其解析并进行拼凑后异常抛出
            List<String> errorMessageList = resultSet.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            StringBuilder errorMessage = new StringBuilder();
            errorMessageList.forEach(o -> errorMessage.append(o).append(";"));
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }
}
