package com.wupol.myopia.business.management.annotation;

import com.wupol.myopia.business.management.annotation.validation.CheckTimeIntervalValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 校验结束时间大于等于开始时间
 * @author Alix
 * @date 2021-02-18
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE,
        ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckTimeIntervalValidation.class)
@Documented
@Repeatable(CheckTimeInterval.List.class)
public @interface CheckTimeInterval {

    String beginTime() default "from";

    String endTime() default "to";

    String message() default "{org.hibernate.validator.referneceguide.chapter06.CheckCase.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
            ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {

        CheckTimeInterval[] value();
    }
}