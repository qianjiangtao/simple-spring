package com.toter.framework.mvc.servlet;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * <p>
 * <p>
 *
 * @author Tour Jiang
 * @date 2019/2/28
 */
@Setter
@Getter
public class MyModelAndView {

    private String view;

    private Map<String, Object> model;

    public MyModelAndView(String view, Map<String, Object> model) {
        this.view = view;
        this.model = model;
    }
}
