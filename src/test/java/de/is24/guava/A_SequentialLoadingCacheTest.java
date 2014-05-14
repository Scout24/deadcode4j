package de.is24.guava;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;

import static com.google.common.base.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class A_SequentialLoadingCacheTest {

    private SequenceFunction sequenceFunction;
    private SequentialLoadingCache<Object, Integer> objectUnderTest;

    @Before
    public void setUp() throws Exception {
        sequenceFunction = new SequenceFunction();
        objectUnderTest = new SequentialLoadingCache<Object, Integer>(sequenceFunction);
    }

    @Test
    public void usesFunctionToLoadValues() {
        Optional<Integer> result = objectUnderTest.getUnchecked("foo");

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(0));
    }

    @Test
    public void cachesValues() {
        objectUnderTest.getUnchecked("foo");
        Optional<Integer> result = objectUnderTest.getUnchecked("foo");

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(0));
    }

    @Test
    public void usesKeysToCache() {
        objectUnderTest.getUnchecked("foo");
        Optional<Integer> result = objectUnderTest.getUnchecked("bar");

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(1));
    }

    @Test
    public void usesCacheOnlyIfRequested() {
        Optional<Integer> result = objectUnderTest.getIfPresent("foo");

        assertThat(result.isPresent(), is(false));
        assertThat(sequenceFunction.sequence, is(0));
    }

    @Test
    public void cachesOnlyOneValue() {
        objectUnderTest = SequentialLoadingCache.createSingleValueCache(sequenceFunction);

        objectUnderTest.getUnchecked("foo");
        objectUnderTest.getUnchecked("foo");
        objectUnderTest.getUnchecked("bar");
        objectUnderTest.getUnchecked("bar");
        Optional<Integer> result = objectUnderTest.getUnchecked("foo");

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(2));
    }

    private static class SequenceFunction implements Function<Object, Optional<Integer>> {
        public int sequence = 0;
        @Nullable
        @Override
        public Optional<Integer> apply(@Nullable Object input) {
            return of(sequence++);
        }
    }

}