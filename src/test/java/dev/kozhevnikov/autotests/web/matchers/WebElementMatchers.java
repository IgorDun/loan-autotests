package dev.kozhevnikov.autotests.web.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.openqa.selenium.WebElement;

/**
 * Матчеры состояния элемента для should() и waitUntil() в Atlas.
 */
public final class WebElementMatchers {

    private WebElementMatchers() {
    }

    public static Matcher<WebElement> displayed() {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(WebElement element) {
                return element.isDisplayed();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("элемент отображается");
            }
        };
    }

    public static Matcher<WebElement> enabled() {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(WebElement element) {
                return element.isEnabled();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("элемент активен");
            }
        };
    }

    public static Matcher<WebElement> hasText(Matcher<String> textMatcher) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(WebElement element) {
                return textMatcher.matches(element.getText());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("текст элемента ").appendDescriptionOf(textMatcher);
            }

            @Override
            protected void describeMismatchSafely(WebElement element, Description mismatch) {
                mismatch.appendText("текст был ").appendValue(element.getText());
            }
        };
    }
}
