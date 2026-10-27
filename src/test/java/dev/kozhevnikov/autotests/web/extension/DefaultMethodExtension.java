package dev.kozhevnikov.autotests.web.extension;

import io.qameta.atlas.core.api.MethodExtension;
import io.qameta.atlas.core.internal.Configuration;
import io.qameta.atlas.core.util.MethodInfo;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class DefaultMethodExtension implements MethodExtension {

    @Override
    public boolean test(Method method) {
        return method.isDefault();
    }

    @Override
    public Object invoke(Object proxy, MethodInfo methodInfo, Configuration config) throws Throwable {
        Object[] args = methodInfo.getArgs();
        if (args == null) {
            args = new Object[0];
        }

        return MethodHandles.lookup()
                .findSpecial(
                        methodInfo.getMethod().getDeclaringClass(),
                        methodInfo.getMethod().getName(),
                        MethodType.methodType(
                                methodInfo.getMethod().getReturnType(),
                                methodInfo.getMethod().getParameterTypes()),
                        methodInfo.getMethod().getDeclaringClass()
                )
                .bindTo(proxy)
                .invokeWithArguments(args);
    }

}
