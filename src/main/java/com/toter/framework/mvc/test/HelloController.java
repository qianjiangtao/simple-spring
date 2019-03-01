package com.toter.framework.mvc.test;

import com.toter.framework.mvc.annotation.MyController;
import com.toter.framework.mvc.annotation.MyReqeustParam;
import com.toter.framework.mvc.annotation.MyRequestMapping;
import com.toter.framework.mvc.servlet.MyModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * <p>
 *
 * @author Tour Jiang
 * @date 2019/2/28
 */
@MyController
@MyRequestMapping(value = "/mvc")
public class HelloController {

    @MyRequestMapping(value = "/hello")
    public void hello(HttpServletResponse response, @MyReqeustParam("name") String name) {
        try {
            response.getWriter().write("name:" + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MyRequestMapping(value = "/helloView")
    public MyModelAndView helloView() {
        Map<String, Object> model = new HashMap<>();
        model.put("name", "Tour Jiang");
        MyModelAndView mav = new MyModelAndView("hello.tr", model);
        return mav;
    }
}
