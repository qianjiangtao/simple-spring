package com.toter.framework.mvc.view;

import com.toter.framework.mvc.servlet.MyModelAndView;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * <p>
 *
 * @author Tour Jiang
 * @date 2019/2/28
 */
public class MyViewResolver {

    private String viewName;
    private File file;

    public MyViewResolver(String viewName, File file) {
        this.viewName = viewName;
        this.file = file;
    }

    public String parse(MyModelAndView mv) throws Exception {
        StringBuilder sb = new StringBuilder();
        RandomAccessFile ra = new RandomAccessFile(this.file, "r");
        try {
            //模板框架的语法是非常复杂，但是，原理是一样的
            //无非都是用正则表达式来处理字符串而已
            String line = null;
            while (null != (line = ra.readLine())) {
                Matcher m = matcher(line);
                while (m.find()) {
                    for (int i = 1; i <= m.groupCount(); i++) {
                        String paramName = m.group(i);
                        Object paramValue = mv.getModel().get(paramName);
                        if (null == paramValue) {
                            continue;
                        }
                        line = line.replaceAll("@\\{" + paramName + "\\}", paramValue.toString());
                    }
                }

                sb.append(line);
            }
        } finally {
            ra.close();
        }
        return sb.toString();
    }

    private Matcher matcher(String str) {
        Pattern pattern = Pattern.compile("@\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(str);
        return m;
    }

    public String getViewName() {
        return viewName;
    }
}
