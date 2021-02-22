/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.abstracttest.matcher;

import java.util.function.Function;

import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsNot;

public class Matchers {

    private Matchers() {
        // avoid default constructor for utility class
    }

    public static Matcher<MessageList> hasMessageCode(final String msgCode) {
        return new MessageCodeMatcher(msgCode, true);
    }

    public static Matcher<MessageList> lacksMessageCode(final String msgCode) {
        return new MessageCodeMatcher(msgCode, false);
    }

    public static Matcher<MessageList> hasSize(int size) {
        return new MessageListSizeMatcher(size);
    }

    public static Matcher<MessageList> isEmpty() {
        return new EmptyMessageListMatcher();
    }

    public static Matcher<MessageList> containsMessages() {
        return new IsNot<MessageList>(new EmptyMessageListMatcher());
    }

    public static Matcher<Message> hasInvalidObject(Object invalidObject) {
        return new MessageInvalidObjectMatcher(invalidObject);
    }

    public static Matcher<Message> hasInvalidObject(Object invalidObject, String propertyName) {
        return new MessageInvalidObjectMatcher(invalidObject, propertyName);
    }

    public static Matcher<Message> hasSeverity(int severity) {
        return new MessageSevertiyMatcher(severity);
    }

    public static Matcher<MessageList> containsErrorMsg() {
        return new ContainsErrorMatcher();
    }

    /**
     * Similar to {@link CoreMatchers#allOf(Matcher...)}, but with better mismatch description.
     */
    @SafeVarargs
    public static <T> Matcher<T> allOf(Matcher<T>... matchers) {
        return AllMatcher.allOf(matchers);
    }

    /**
     * A {@link Matcher Matcher&lt;T&gt;} that uses a wrapped {@link Matcher Matcher&lt;P&gt;} on a
     * property of type &lt;P&gt; of the matched object of type &lt;T&gt;.
     * 
     * @param <T> the type of the matched object
     * @param <P> the type of the object's property matched by the wrapped matcher
     */
    public static <T, P> Matcher<T> hasProperty(Function<T, P> propertyGetter,
            String propertyDescription,
            Matcher<P> propertyMatcher) {
        return new PropertyMatcher<>(propertyGetter, propertyDescription, propertyMatcher);
    }

    /**
     * A {@link Matcher Matcher&lt;T&gt;} that uses a wrapped {@link Matcher Matcher&lt;P&gt;} on a
     * property of type &lt;P&gt; of the matched object of type &lt;T&gt; and referenced object of
     * that same type.
     *
     * @param <T> the type of the compared objects
     * @param <P> the type of the property
     */
    public static <T, P> Matcher<T> hasSame(String propertyDescription,
            Function<T, P> propertyGetter,
            T objectToMatch) {
        return SamePropertyMatcher.same(propertyGetter, propertyDescription, objectToMatch);
    }

    /**
     * A {@link Matcher Matcher&lt;T&gt;} that uses a wrapped {@link Matcher Matcher&lt;byte[]&gt;}
     * on a byte[]-typed property of the matched object of type &lt;T&gt; and referenced object of
     * that same type.
     *
     * @param <T> the type of the compared objects
     */
    public static <T> Matcher<T> hasSameByteArray(String propertyDescription,
            Function<T, byte[]> propertyGetter,
            T objectToMatch) {
        return SameByteArrayPropertyMatcher.sameByteArray(propertyGetter, propertyDescription, objectToMatch);
    }

}
