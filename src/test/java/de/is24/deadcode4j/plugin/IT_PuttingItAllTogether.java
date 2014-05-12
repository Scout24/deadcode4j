package de.is24.deadcode4j.plugin;

import com.google.common.base.Strings;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class IT_PuttingItAllTogether {
    @Rule
    public final MojoRule mojoSetUp = new MojoRule();

    private Log logMock;
    private FindDeadCodeMojo findDeadCodeMojo;

    @Before
    public void setUp() throws Exception {
        URL pomUrl = getClass().getClassLoader().getResource("de/is24/deadcode4j/plugin/projects/pom.xml");
        assertNotNull(pomUrl);
        File pom = new File(pomUrl.toURI());
        assertTrue(pom.exists());

        findDeadCodeMojo = (FindDeadCodeMojo) mojoSetUp.lookupMojo("find", pom);
        assertNotNull(findDeadCodeMojo);

        logMock = mock(Log.class);
        when(logMock.isDebugEnabled()).thenReturn(true);
        doAnswer(new LogAnswer("DEBUG")).when(logMock).debug(any(CharSequence.class));
        when(logMock.isInfoEnabled()).thenReturn(true);
        doAnswer(new LogAnswer("INFO")).when(logMock).info(any(CharSequence.class));
        when(logMock.isWarnEnabled()).thenReturn(true);
        doAnswer(new LogAnswer("WARN")).when(logMock).warn(any(CharSequence.class));
        when(logMock.isErrorEnabled()).thenReturn(true);
        doAnswer(new LogAnswer("ERROR")).when(logMock).error(any(CharSequence.class));
        findDeadCodeMojo.setLog(logMock);
    }

    @Test
    public void test() throws Exception {
        findDeadCodeMojo.execute();

        verify(logMock).info("Analyzed 25 class(es).");
        verify(logMock).info("Ignoring 1 class(es) which seem(s) to be unused.");
        verify(logMock).warn("Class [com.acme.Foo] should be ignored, but is not dead. You should remove the configuration entry.");
        verify(logMock).warn("Found 3 unused class(es):");
        verify(logMock).warn("  ClassWithTypeArgument");
        verify(logMock).warn("  DeadServlet");
        verify(logMock).warn("  SomeServletInitializer"); // this doesn't work because the classpath is faked
    }

    private static class LogAnswer implements Answer<Void> {

        private final String level;

        public LogAnswer(String level) {
            this.level = level;
        }

        @Override
        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            System.out.println("[" + Strings.padEnd(level, 5, ' ') + "] " + invocationOnMock.getArguments()[0]);
            return null;
        }

    }

}
