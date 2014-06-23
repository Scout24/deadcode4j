package de.is24.guava;

import com.google.common.base.Optional;
import de.is24.deadcode4j.junit.AUtilityClass;
import org.junit.Test;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

public class A_NonNullFunctions extends AUtilityClass {

    @Test
    @SuppressWarnings("unchecked")
    public void callsEachFunction() {
        Object expectedReturnValue = new Object();
        NonNullFunction<Object, Optional<Object>> first = mock(NonNullFunction.class, "firstFunction");
        when(first.apply(anyObject())).thenReturn(absent());
        NonNullFunction<Object, Optional<Object>> second = mock(NonNullFunction.class, "secondFunction");
        when(second.apply(anyObject())).thenReturn(of(expectedReturnValue));

        Optional<Object> result = NonNullFunctions.or(first, second).apply(new Object());

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(expectedReturnValue));
        verify(first).apply(anyObject());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void stopsAfterResultIsPresent() {
        Object expectedReturnValue = new Object();
        NonNullFunction<Object, Optional<Object>> first = mock(NonNullFunction.class, "firstFunction");
        when(first.apply(anyObject())).thenReturn(of(expectedReturnValue));
        NonNullFunction<Object, Optional<Object>> second = mock(NonNullFunction.class, "secondFunction");
        when(second.apply(anyObject())).thenReturn(absent());

        Optional<Object> result = NonNullFunctions.or(first, second).apply(new Object());

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(expectedReturnValue));
        verify(second, never()).apply(anyObject());
    }

    @Override
    protected Class<?> getType() {
        return NonNullFunctions.class;
    }

}
