package com.wupol.myopia.business.api.questionnaire.service;

import com.wupol.myopia.base.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 问题工厂类
 *
 * @author Simple4H
 */
@Component
public class UserAnswerFactory {

    private final List<IUserAnswerService> iUserAnswerServices;

    public UserAnswerFactory(List<IUserAnswerService> iUserAnswerServices) {
        this.iUserAnswerServices = iUserAnswerServices;
    }

    public IUserAnswerService getUserAnswerService(Integer userType) {
        return iUserAnswerServices.stream().filter(service -> Objects.equals(service.getUserType(), userType))
                .findFirst().orElseThrow(() -> new BusinessException("获取类型异常"));
    }
}
