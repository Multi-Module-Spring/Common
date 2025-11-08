package com.wis.main.util.core_util.mapper;

import com.wis.main.util.core_util.drools.model.Field;

import java.io.*;
import java.lang.invoke.*;
import java.lang.reflect.Method;

public class LambdaUtil {

    public static <T, R> String extractFieldName(Field<T, R> getter) {
        try {
            SerializedLambda serialized = getSerializedLambda(getter);
            String methodName = serialized.getImplMethodName();

            if (methodName.startsWith("get")) {
                String field = methodName.substring(3);
                return Character.toLowerCase(field.charAt(0)) + field.substring(1);
            }
            return methodName;
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract field name", e);
        }
    }

    public static SerializedLambda getSerializedLambda(Serializable lambda)
            throws Exception {
        Method m = lambda.getClass().getDeclaredMethod("writeReplace");
        m.setAccessible(true);
        return (SerializedLambda) m.invoke(lambda);
    }

    public static String extractMethodCall(Serializable lambda) {
        try {
            SerializedLambda l = getSerializedLambda(lambda);
            String className = l.getImplClass().replace('/', '.');
            String methodName = l.getImplMethodName();
            return className + "::" + methodName;
        } catch (Exception e) {
            throw new RuntimeException("Cannot extract lambda info", e);
        }
    }

    public static <T, R> String extractMethodName(Serializable fn) {
        try {
            SerializedLambda lambda = getSerializedLambda(fn);
            return lambda.getImplMethodName(); // ví dụ: getPostcode
        } catch (Exception e) {
            throw new RuntimeException("Cannot extract method name", e);
        }
    }

    public static String extractLambdaName(String lambda) {
        int idx = lambda.lastIndexOf("::");
        return idx > 0 ? lambda.substring(idx + 2) : "Unknown";
    }

}

