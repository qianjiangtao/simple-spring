package com.toter.framework.mvc.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * <p>
 *
 * @author Tour Jiang
 * @date 2019/2/28
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {

    String value() default "";
}
