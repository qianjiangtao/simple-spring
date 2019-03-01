package com.toter.framework.mvc.servlet;

import com.toter.framework.mvc.annotation.MyController;
import com.toter.framework.mvc.annotation.MyReqeustParam;
import com.toter.framework.mvc.annotation.MyRequestMapping;
import com.toter.framework.mvc.context.MyApplicationContext;
import com.toter.framework.mvc.handler.MyHandlerAdapter;
import com.toter.framework.mvc.handler.MyHandlerMapping;
import com.toter.framework.mvc.view.MyViewResolver;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * <p>
 *
 * @author Tour Jiang
 * @date 2019/2/28
 */
public class MyDispatcherServlet extends HttpServlet {

    private static final String LOCATION = "contextConfigLocation";

    private static final long serialVersionUID = 2109731363096233456L;

    /**
     * 请求映射
     */
    private static List<MyHandlerMapping> handlerMapping = new ArrayList<>();

    /**
     * 适配映射
     */
    private static Map<MyHandlerMapping, MyHandlerAdapter> handlerAdapter = new ConcurrentHashMap<>();

    /**
     * 模版映射
     */
    private static List<MyViewResolver> viewResolvers = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            initStrategies(new MyApplicationContext(config.getInitParameter(LOCATION)), config.getServletContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String url = request.getRequestURI().replace(request.getContextPath(), "");

        MyHandlerMapping handler = null;
        for (MyHandlerMapping mapping : handlerMapping) {
            Matcher matcher = mapping.getPattern().matcher(url);
            if (matcher.matches()) {
                handler = mapping;
                break;
            }
        }
        if (handler == null) {
            response.getWriter().write("404 Not Found");
            return;
        }

        MyHandlerAdapter adapter = handlerAdapter.get(handler);
        if (adapter == null) {
            throw new RuntimeException("adapter not found");
        }
        MyModelAndView mav = adapter.handle(request, response, handler);

        showView(response, mav);
    }

    /**
     * 页面渲染
     *
     * @param resp
     * @param mv
     * @throws Exception
     */
    private void showView(HttpServletResponse resp, MyModelAndView mv) throws Exception {
        if (null == mv) {
            return;
        }
        if (viewResolvers.isEmpty()) {
            return;
        }
        for (MyViewResolver resolver : viewResolvers) {
            if (!mv.getView().equals(resolver.getViewName())) {
                continue;
            }
            String r = resolver.parse(mv);
            if (r != null) {
                resp.getWriter().write(r);
                break;
            }
        }
    }

    /**
     * 初始化servlet使用的对象
     *
     * @param context
     */
    protected void initStrategies(MyApplicationContext context, ServletContext servletContext) {

        //处理文件上传功能须在容器中注册一个MultipartResolver bean,(未实现)
        //initMultipartResolver(context);

        //地区解析器(未实现)
        //initLocaleResolver(context);

        //主题解析器(未实现)
        //initThemeResolver(context);

        //handlerMapping检查
        initHandlerMappings(context);

        //handlerAdapter检查
        initHandlerAdapters(context);

        //异常解析(未实现)
        //initHandlerExceptionResolvers(context);

        //视图转发（根据视图名字匹配到一个具体模板）(未实现)
        //initRequestToViewNameTranslator(context);

        //解析模板中的内容（拿到服务器传过来的数据，生成HTML代码）(未实现)
        initViewResolvers(context, servletContext);

        //initFlashMapManager(context);
    }

    /**
     * 请求映射
     *
     * @param context
     */
    private void initHandlerMappings(MyApplicationContext context) {

        Map<String, Object> container = context.getContainer();
        if (container.isEmpty()) {
            return;
        }
        container.forEach((beanName, bean) -> {
            Class<?> clazz = bean.getClass();
            if (!clazz.isAnnotationPresent(MyController.class)) {
                return;
            }
            //如果controller上设置了requestMapping则拼接
            MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
            String url = "";
            if (!"".equals(requestMapping.value())) {
                url = requestMapping.value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    return;
                }
                String methodUrl = method.getAnnotation(MyRequestMapping.class).value();
                if (!"".equals(methodUrl)) {
                    handlerMapping.add(new MyHandlerMapping(bean, method, Pattern.compile(url + methodUrl)));
                    System.out.println("Mapping url:" + url + methodUrl);
                }
            }
        });

    }


    /**
     * 适配器 用来适配我们传递的参数
     */
    private void initHandlerAdapters(MyApplicationContext context) {
        if (handlerMapping.isEmpty()) {
            return;
        }

        //参数类型作为key，参数的索引号作为值
        Map<String, Integer> paramMapping = new HashMap<>();

        //把这个方法上面所有的参数全部获取到
        handlerMapping.forEach(handler -> {

            //处理参数为request或response
            Class<?>[] paramsTypes = handler.getMethod().getParameterTypes();
            for (int i = 0; i < paramsTypes.length; i++) {
                Class<?> type = paramsTypes[i];
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramMapping.put(type.getName(), i);
                }
            }
            //匹配自定参数列表
            Annotation[][] annotations = handler.getMethod().getParameterAnnotations();
            for (int i = 0; i < annotations.length; i++) {
                for (Annotation annotation : annotations[i]) {
                    if (annotation instanceof MyReqeustParam) {
                        String value = ((MyReqeustParam) annotation).value();
                        if (!"".equals(value)) {
                            paramMapping.put(value, i);
                        }
                    }
                }
            }
            handlerAdapter.put(handler, new MyHandlerAdapter(paramMapping));
        });

    }

    private void initHandlerExceptionResolvers(MyApplicationContext context) {
    }

    private void initRequestToViewNameTranslator(MyApplicationContext context) {
    }

    private void initViewResolvers(MyApplicationContext context, ServletContext servletContext) {
        //模板一般是不会放到WebRoot下的，而是放在WEB-INF下，或者classes下
        //这样就避免了用户直接请求到模板
        //加载模板的个数，存储到缓存中
        //检查模板中的语法错误
        String templateRoot = context.getProperties().getProperty("templateRoot");

        //归根到底就是一个文件，普通文件
        try {
            String fileName = servletContext.getRealPath("/") + templateRoot;
            fileName = fileName.replace("\\", "/");
            File rootDir = new File(fileName);
            File[] files = rootDir.listFiles();
            if (files == null || files.length == 0) {
                return;
            }
            for (File template : files) {
                viewResolvers.add(new MyViewResolver(template.getName(), template));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initFlashMapManager(MyApplicationContext context) {
    }

    private void initThemeResolver(MyApplicationContext context) {
    }

    private void initLocaleResolver(MyApplicationContext context) {
    }


    private void initMultipartResolver(MyApplicationContext context) {
    }

}
