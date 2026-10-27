package dev.kozhevnikov.autotests.web.extension;

import io.qameta.atlas.core.api.MethodExtension;
import io.qameta.atlas.core.context.TargetContext;
import io.qameta.atlas.core.internal.Configuration;
import io.qameta.atlas.core.util.MethodInfo;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ISelect;
import org.openqa.selenium.support.ui.Select;
import dev.kozhevnikov.autotests.exception.AutotestException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SelectExtension implements MethodExtension {

    @Override
    public boolean test(Method method) {
        try {
            ISelect.class.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

    @Override
    public Object invoke(Object o, MethodInfo methodInfo, Configuration configuration) throws Throwable {
        WebElement webElement = (WebElement) configuration.getContext(TargetContext.class)
                .orElseThrow(() -> new AutotestException("Не удалось получить вэб-элемент: целевой контекст пуст"))
                .getValue()
                .instance();
        Select select = new Select(webElement);
        try {
            return ISelect.class.getMethod(methodInfo.getMethod().getName(), methodInfo.getMethod().getParameterTypes())
                    .invoke(select, methodInfo.getArgs());
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
