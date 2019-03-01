package com.toter.framework.mvc.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * <p>
 *
 * @author Tour Jiang
 * @date 2019/2/28
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutowired {

    String value() default "";
}
