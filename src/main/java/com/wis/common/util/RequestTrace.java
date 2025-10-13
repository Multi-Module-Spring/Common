package com.wis.common.util;

import java.util.ArrayList;
import java.util.List;

public class RequestTrace {
    private static final ThreadLocal<List<String>> threadLocal = ThreadLocal.withInitial(ArrayList::new);

    public static void add(String info) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stack.length > 2 ? stack[2] : null;
        String location = caller != null
                ? caller.getClassName() + "#" + caller.getMethodName() + "(line " + caller.getLineNumber() + ")"
                : "Unknown Source";

        threadLocal.get().add(info + " [called from: " + location + "]");
    }

    public static List<String> get() {
        return new ArrayList<>(threadLocal.get());
    }

    public static void clear() {
        threadLocal.get().clear();
    }
}
