package com.wupol.myopia.business.common.utils.annotation.validation;

import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.business.common.utils.annotation.CheckTimeInterval;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Date;

/**
 * 校验结束时间大于等于开始时间
 *
 * @author Alix
 * @date 2021-02-18
 */
public class CheckTimeIntervalValidation implements ConstraintValidator<CheckTimeInterval, Object> {

    private String beginTime;

    private String endTime;

    @Override
    public void initialize(CheckTimeInterval constraintAnnotation) {
        this.beginTime = constraintAnnotation.beginTime();
        this.endTime = constraintAnnotation.endTime();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(value);
        Object start = beanWrapper.getPropertyValue(beginTime);
        Object end = beanWrapper.getPropertyValue(endTime);
        if (ObjectsUtil.hasNull(start, end)) {
            return false;
        }
        Date d1 = (Date) start;
        Date d2 = (Date) end;
        if (null == d1 || null == d2) {
            return false;
        }
        int result = d2.compareTo(d1);
        return result >= 0;
    }
}
