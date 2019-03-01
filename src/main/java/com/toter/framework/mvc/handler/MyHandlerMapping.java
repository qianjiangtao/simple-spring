package com.toter.framework.mvc.handler;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * <p>
 * <p>
 *
 * @author Tour Jiang
 * @date 2019/2/28
 */
@Getter
@Setter
public class MyHandlerMapping {

    /**
     * controller
     */
    private Object controller;

    /**
     * 调用的method
     */
    private Method method;

    /**
     * url pattern
     */
    private Pattern pattern;

    public MyHandlerMapping(Object controller, Method method, Pattern pattern) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MyHandlerMapping that = (MyHandlerMapping) o;

        if (controller != null ? !controller.equals(that.controller) : that.controller != null) {
            return false;
        }
        return method != null ? method.equals(that.method) : that.method == null && (pattern != null ? pattern.equals(that.pattern) : that.pattern == null);

    }

    @Override
    public int hashCode() {
        int result = controller != null ? controller.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
        return result;
    }
}
