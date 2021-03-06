package com.ikats.scheduler.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 从jar包里面加载class,用于动态部署,新增任务
 * @Author : liu kuo
 * @Date : 2017/11/9 15:51.
 * @Description : Indulge in study , wasting away
 */
public class GetClassFromJar
{
    protected static Method addURL = null ;
    static{
        try
        {
            addURL = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class }) ;
            addURL.setAccessible(true);
        }catch(Exception e)
        {
            throw new RuntimeException("初始化失败");
        }
    }

    /**
     * 动态加载Jar包到内存中
     * */
    public static Class loadJar(String jarFile, String className) {
        try {
            File file = new File( jarFile );
            if (!file.exists())
            {
                throw new RuntimeException(jarFile + "不存在");
            }
            // 设置类加载器
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            // 将当前类路径加入到类加载器中
            addURL.invoke(classLoader, file.toURI().toURL());
            addURL.invoke(Thread.currentThread().getContextClassLoader(), new Object[] { file.toURI().toURL() });
            return Class.forName( className ,false ,Thread.currentThread().getContextClassLoader());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
