package de.is24.deadcode4j;

import java.util.Collections;

public final class CodeContextBuilder {
    private CodeContextBuilder() {
    }

    public static CodeContext givenCodeContext(Module module) {
        return new CodeContext(module, Collections.<Object, IntermediateResult>emptyMap());
    }

}
