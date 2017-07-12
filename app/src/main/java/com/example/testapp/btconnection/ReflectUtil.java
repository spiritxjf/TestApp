package com.example.testapp.btconnection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtil
{
    /**
     * Find Method object by name <br>
     * if not found, null will be returned
     * 
     * @param clazz
     * @param methodName
     * @return
     */
    public static Method findMethodByName(Class<?> clazz, String methodName)
    {
        Method[] ms = clazz.getDeclaredMethods();
        for (int i = 0; i < ms.length; i++)
        {
            Method m = ms[i];
            if (m.getName().equals(methodName))
                return m;
        }
        return null;
    }

    /**
     * find subclass by name
     * 
     * @param clazz
     * @param className
     * @return
     */
    public static Class<?> findSubClassByName(Class<?> clazz, String className)
    {
        Class<?>[] cs = clazz.getDeclaredClasses();
        for (Class<?> c : cs)
        {
            if (c.getName().endsWith(className))
                return c;
        }
        return null;
    }

    /**
     * Try to invoke a method with args
     * 
     * @param object
     * @param method
     * @param args
     * @return
     */
    public static Object invoke(Object object, Method method, Object... args)
    {
        try
        {
            method.setAccessible(true);
            return method.invoke(object, args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Try to invoke a method by methodName & args
     * 
     * @param object
     * @param methodName
     * @param args
     * @return
     */
    public static Object invoke(Object object, String methodName,
            Object... args)
    {
        try
        {
            Method method = findMethodByName(object.getClass(), methodName);
            return invoke(object, method, args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a field value
     * 
     * @param object
     * @param fieldName
     * @return
     */
    public static Object getFieldValue(Object object, String fieldName)
    {
        try
        {
            Class<?> clazz = object.getClass();
            Field field = findFieldByName(clazz, fieldName);
            field.setAccessible(true);
            return field.get(object);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find field by name
     * 
     * @param clazz
     * @param fieldName
     * @return
     */
    public static Field findFieldByName(Class<?> clazz, String fieldName)
    {
        Field[] fs = clazz.getDeclaredFields();
        for (Field f : fs)
        {
            if (f.getName().equals(fieldName))
                return f;
        }
        return null;
    }
}
