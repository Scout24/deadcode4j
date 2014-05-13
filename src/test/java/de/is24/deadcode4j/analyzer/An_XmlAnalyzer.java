package de.is24.deadcode4j.analyzer;

import de.is24.deadcode4j.CodeContext;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class An_XmlAnalyzer extends AnAnalyzer<XmlAnalyzer> {

    @Test
    public void parsesMatchingFile() {
        final AtomicBoolean fileIsParsed = new AtomicBoolean(false);

        objectUnderTest = new XmlAnalyzer(".xml") {
            @Nonnull
            @Override
            protected DefaultHandler createHandlerFor(@Nonnull CodeContext codeContext) {
                fileIsParsed.set(true);
                return new DefaultHandler();
            }
        };

        analyzeFile("de/is24/deadcode4j/analyzer/empty.xml");

        assertThat("Should have analyzed the XML file!", fileIsParsed.get(), is(true));
    }

    @Test
    public void doesNotParseNonMatchingFile() {
        objectUnderTest = new XmlAnalyzer(".foo") {
            @Nonnull
            @Override
            protected DefaultHandler createHandlerFor(@Nonnull CodeContext codeContext) {
                Assert.fail("Should NOT have analyzed the XML file!");
                return new DefaultHandler();
            }
        };

        analyzeFile("de/is24/deadcode4j/analyzer/empty.xml");
    }

}
