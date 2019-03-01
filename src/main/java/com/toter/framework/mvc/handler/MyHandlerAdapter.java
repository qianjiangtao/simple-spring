package com.toter.framework.mvc.handler;

import com.toter.framework.mvc.servlet.MyModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

/**
 * <p>
 * 适配器
 * <p>
 *
 * @author Tour Jiang
 * @date 2019/2/28
 */
public class MyHandlerAdapter {

    /**
     * 参数
     */
    private Map<String, Integer> paramMapping;

    public MyHandlerAdapter(Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }

    /**
     */
    public MyModelAndView handle(HttpServletRequest req, HttpServletResponse resp, MyHandlerMapping handler) throws Exception {

        Class<?>[] paramTypes = handler.getMethod().getParameterTypes();
        Object[] paramValues = new Object[paramTypes.length];
        if (paramTypes.length > 0) {
            //请求参数
            Map<String, String[]> params = req.getParameterMap();

            //对参数顺序进行匹配
            params.forEach((k, v) -> {
                if (!paramMapping.containsKey(k)) {
                    return;
                }
                int index = paramMapping.get(k);
                paramValues[index] = castStringValue(Arrays.toString(v), paramTypes[index]);
            });

            //request 和 response 要赋值
            String reqName = HttpServletRequest.class.getName();
            if (this.paramMapping.containsKey(reqName)) {
                int reqIndex = this.paramMapping.get(reqName);
                paramValues[reqIndex] = req;
            }
            String respName = HttpServletResponse.class.getName();
            if (this.paramMapping.containsKey(respName)) {
                int respIndex = this.paramMapping.get(respName);
                paramValues[respIndex] = resp;
            }
        }

        //判断是否是返回页面
        boolean isModelAndView = handler.getMethod().getReturnType() == MyModelAndView.class;

        Object result = handler.getMethod().invoke(handler.getController(), paramValues);
        if (isModelAndView) {
            return (MyModelAndView) result;
        }
        return null;
    }

    private Object castStringValue(String value, Class<?> clazz) {
        if (clazz == String.class) {
            return value;
        } else if (clazz == Integer.class) {
            return Integer.valueOf(value);
        } else if (clazz == int.class) {
            return Integer.valueOf(value);
        } else {
            return null;
        }
    }
}
