package com.chris.spring.dispatch;

import com.chris.spring.annotation.Autowired;
import com.chris.spring.annotation.Controller;
import com.chris.spring.annotation.RequestMapping;
import com.chris.spring.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 分发请求类
 */
public class DispatcherServlet extends HttpServlet {
    /**
     * 上下文，定义在web.xml中的变量
     */
    Properties context = new Properties();

    /**
     * IOC 容器，实际为一个HashMap
     */
    Map<String, Object> ioc = new HashMap<>();

    Map<String, Method> handlerMappings = new HashMap<>();

    /**
     * 类的根路径
     */
    private String rootPath;

    /**
     * 包路径
     */
    private String packageName;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // 1, 加载配置文件
        loadConfig(config);

        // 2, 扫描相关联的类,初始化所有的相关联类，并保存到IOC容器中
        File file = new File(Paths.get(rootPath, packageName.replace(".", "\\")).toString());
        doScanner(file);
        // 4, 完成依赖注入
        doWired();
        // 5, 初始化HandlerMapping
        initHandlerMapping();
    }

    /**
     * 加载配置
     *
     * @param config 配置对象
     */
    private void loadConfig(ServletConfig config) {
        String configLocation = config.getInitParameter("contextConfigLocation");

        if (configLocation == null || configLocation.isEmpty()) {
            throw new RuntimeException("the location of bean is not configured, please check the web.xml");
        }

        String configPath = this.getServletContext().getRealPath("") + configLocation;
        try {
            InputStream inputStream = new FileInputStream(configPath);
            context.load(inputStream);
            packageName = context.getProperty("root");
            rootPath = Paths.get(getServletContext().getRealPath(""), "WEB-INF", "classes").toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("can't find the contextConfigLocation, please check the web.xml");
        } catch (IOException e) {
            throw new RuntimeException("load properties failed, please check errors in contextConfig file");
        }
    }

    /**
     * 初始化HandlerMapping
     */
    private void initHandlerMapping() {
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            String url = "";
            Class<?> clz = entry.getValue().getClass();
            if (clz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping mapping = clz.getAnnotation(RequestMapping.class);
                url = (url + mapping.value()).replace("//", "/");
            }

            Method[] methods = clz.getDeclaredMethods();
            if (methods.length <= 0) {
                continue;
            }
            for (int index = 0; index < methods.length; index++) {
                Method method = methods[0];
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }

                RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                url = (url + "/" + mapping.value()).replace("//", "/");
                handlerMappings.put(url, method);
            }
        }
    }

    /**
     * 完成依赖注入
     */
    private void doWired() {
        // 遍历所有对象，看是否有没有@Autowired注解
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Object bean = entry.getValue();
            Class<?> clz = bean.getClass();
            for (Field field : clz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }

                Autowired autowired = field.getAnnotation(Autowired.class);
                String fieldName = autowired.value();
                Object fieldBean = ioc.get(fieldName);
                if (fieldBean == null) {
                    throw new RuntimeException("can't find bean named " + fieldName + "in ioc container");
                }

                field.setAccessible(true);
                try {
                    // 依赖注入完成
                    field.set(bean, fieldBean);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 扫描包，将相关的类保存到IOC容器中
     * 递归扫描所有的文件
     */
    private void doScanner(File file) {
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            for (int index = 0; index < subFiles.length; index++)
                doScanner(subFiles[index]);
        } else {
            if (!file.getName().endsWith(".class")) {
                return;
            }

            String className = file.getAbsolutePath()
                    .replace(rootPath + "\\", "")
                    .replace(".class", "")
                    .replace("\\", ".");

            try {
                Class<?> clzz = Class.forName(className);
                if (!clzz.isAnnotationPresent(Controller.class) && !clzz.isAnnotationPresent(Service.class)) {
                    return;
                }
                String beanName = lowerFirstCase(clzz.getSimpleName());
                Object obj = clzz.newInstance();
                if (clzz.isAnnotationPresent(Service.class)) {
                    Service service = clzz.getAnnotation(Service.class);
                    beanName = service.value();
                }
                ioc.put(beanName, obj);

            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doDispatch(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doDispatch(req, resp);
    }

    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getRequestURI();
        Method method;
        if ((method = handlerMappings.get(url)) == null) {
            throw new RuntimeException("please check the method mapped with the url:" + url);
        }

        String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());
        Object obj;
        if ((obj = ioc.get(beanName)) == null) {
            throw new RuntimeException("can't find the bean named \"" + beanName + "\" in the IOC container");
        }

        try {
            method.invoke(obj, new Object[]{request, response});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.out.println(Arrays.toString(e.getStackTrace()));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            System.out.println(Arrays.toString(e.getStackTrace()));
        }


    }

    private String lowerFirstCase(String s) {
        if (s == null || s.isEmpty()) {
            throw new RuntimeException("the bean name is null");
        }

        char[] chars = s.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
