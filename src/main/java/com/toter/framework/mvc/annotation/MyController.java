package com.toter.framework.mvc.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * <p>
 *
 * @author Tour Jiang
 * @date 2019/2/28
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {

    String value() default "";
}
