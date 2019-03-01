package com.toter.framework.mvc.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * <p>
 *
 * @author Tour Jiang
 * @date 2019/2/28
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyReqeustParam {

    String value() default "";
}
