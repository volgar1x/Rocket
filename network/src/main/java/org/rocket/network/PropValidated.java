package org.rocket.network;

import java.lang.reflect.Method;

public interface PropValidated {
    /**
     * Describe location of a validated action.
     */
    String describeLocation();

    public static PropValidated forString(String s) {
        return () -> s;
    }

    public static PropValidated forMethod(Method m) {
        // Apache BCEL can give more information
        return forString(m.toString());
    }

    public static PropValidated here() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        StackTraceElement here = trace[2];
        return forString(here.getClassName() + "." + here.getMethodName() +
                "(" + here.getFileName() + ":" + here.getLineNumber() + ")");
    }
}
