package com.toter.framework.mvc.context;

import com.toter.framework.mvc.annotation.MyAutowired;
import com.toter.framework.mvc.annotation.MyController;
import com.toter.framework.mvc.annotation.MyService;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * <p>
 *
 * @author Tour Jiang
 * @date 2019/2/28
 */
public class MyApplicationContext {

    /**
     * ioc容器
     */
    private Map<String, Object> container = new ConcurrentHashMap<String, Object>();

    /**
     * 缓存扫描的class
     */
    private List<String> classNameCache = new ArrayList<String>();

    /**
     * 默认配置文件路径
     */
    private static final String BASE_PACKAGE = "basePackage";

    /**
     * 配置
     */
    private Properties properties = new Properties();

    public MyApplicationContext(String location) {

        try {
            this.loadProperty(location);

            //扫描注册
            doRegister(properties.getProperty(BASE_PACKAGE));

            //实例化
            doInstance();

            //对已经实例化的bean进行依赖注入
            doAutowired();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Container complete init......");
    }

    /**
     * 依赖注入
     */
    private void doAutowired() {
        if (container.isEmpty()) {
            return;
        }
        container.forEach((beanName, bean) -> {
            Field[] fields = bean.getClass().getDeclaredFields();
            //只有autowired标记的字段才进行依赖注入
            for (Field field : fields) {
                if (!field.isAnnotationPresent(MyAutowired.class)) {
                    continue;
                }
                //如果自定义了名称则使用自定义的名字去容器中查找
                MyAutowired autowire = field.getAnnotation(MyAutowired.class);
                String name = autowire.value();
                if ("".equals(name)) {
                    name = field.getType().getName();
                }
                //把私有变量开放访问权限
                field.setAccessible(true);
                try {
                    field.set(bean, container.get(name));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 实例化
     */
    private void doInstance() {
        if (classNameCache.isEmpty()) {
            return;
        }
        classNameCache.forEach(name -> {
            try {
                Class<?> clazz = Class.forName(name);
                //如果是controller key就使用名称首字母小写
                if (clazz.isAnnotationPresent(MyController.class)) {
                    container.put(lowerFirstChar(clazz.getSimpleName()), clazz.newInstance());
                } else if (clazz.isAnnotationPresent(MyService.class)) {
                    //如果是service注解判断是否自定义名称
                    MyService myService = clazz.getAnnotation(MyService.class);
                    String beanName = myService.value();
                    if ("".equals(beanName)) {
                        beanName = clazz.getSimpleName();
                    }
                    container.put(beanName, clazz.newInstance());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 加载配置
     *
     * @param location
     */
    private void loadProperty(String location) {
        InputStream stream = null;
        try {
            stream = this.getClass().getClassLoader().getResourceAsStream(location);
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    /**
     * 扫描包 注册class name
     */
    private void doRegister(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File file : files) {
            //如果是文件夹则递归
            if (file.isDirectory()) {
                doRegister(packageName + "." + file.getName());
                continue;
            }
            classNameCache.add(packageName + "." + file.getName().replace(".class", ""));
        }
    }

    /**
     * 将首字母小写
     */
    private String lowerFirstChar(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Map<String, Object> getContainer() {
        return container;
    }

    public Properties getProperties() {
        return properties;
    }
}
