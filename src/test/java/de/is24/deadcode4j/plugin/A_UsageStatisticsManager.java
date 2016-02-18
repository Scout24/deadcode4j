package de.is24.deadcode4j.plugin;

import de.is24.deadcode4j.junit.LoggingRule;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.runtime.MavenProjectProperties;
import org.apache.maven.shared.runtime.MavenRuntime;
import org.apache.maven.shared.runtime.MavenRuntimeException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.collect.Lists.newArrayList;
import static de.is24.deadcode4j.plugin.UsageStatisticsManager.DeadCodeStatistics;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.*;

public final class A_UsageStatisticsManager {

    private Log log;
    private UsageStatisticsManager objectUnderTest;
    private HttpURLConnection urlConnectionMock;
    private ByteArrayOutputStream outputStream;

    @Rule
    public LoggingRule enableLogging() {
        log = LoggingRule.createMock();
        return new LoggingRule(log);
    }

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new UsageStatisticsManager() {
            @Override
            protected HttpURLConnection openUrlConnection() throws IOException {
                super.openUrlConnection(); // cover this ;)
                return urlConnectionMock;
            }
        };
        MavenRuntime mavenRuntimeMock = mock(MavenRuntime.class);
        when(mavenRuntimeMock.getProjectProperties(any(Class.class))).thenReturn(
                new MavenProjectProperties("de.is24", "junit", "42.23"));
        setVariableValueInObject(objectUnderTest, "mavenRuntime", mavenRuntimeMock);

        Prompter prompterMock = mock(Prompter.class);
        when(prompterMock.prompt(anyString(), anyList(), anyString())).thenReturn("N");
        setVariableValueInObject(objectUnderTest, "prompter", prompterMock);

        givenHttpTransferResultsIn(200);
    }

    @After
    public void resetLog() {
        reset(log);
    }

    @Test
    public void shouldDoNothingIfSoConfigured() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.INTERACTIVE);

        objectUnderTest.sendUsageStatistics(new DeadCodeStatistics(TRUE, null));

        assertThatStatisticsWereNotSent();
    }

    @Test
    public void shouldDoNothingIfInOfflineMode() throws Exception {
        givenModes(NetworkModes.OFFLINE, InteractivityModes.INTERACTIVE);

        objectUnderTest.sendUsageStatistics(new DeadCodeStatistics(FALSE, null));

        assertThatStatisticsWereNotSent();
    }

    @Test
    public void shouldSimplySendStatisticsIfSoConfigured() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.INTERACTIVE);

        objectUnderTest.sendUsageStatistics(new DeadCodeStatistics(FALSE, null));

        assertThatStatisticsWereSent();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void shouldSendStatisticsValues() throws Exception {
        List<Object> expectedValues = newArrayList();
        DeadCodeStatistics deadCodeStatistics = new DeadCodeStatistics(FALSE, "Greetings from JUnit!");
        expectedValues.add("Greetings from JUnit!");
        deadCodeStatistics.numberOfAnalyzedClasses = 1;
        expectedValues.add(deadCodeStatistics.numberOfAnalyzedClasses);
        deadCodeStatistics.numberOfAnalyzedModules = 22;
        expectedValues.add(deadCodeStatistics.numberOfAnalyzedModules);
        deadCodeStatistics.numberOfDeadClassesFound = 333;
        expectedValues.add(deadCodeStatistics.numberOfDeadClassesFound);
        deadCodeStatistics.config_numberOfClassesToIgnore = 4444;
        expectedValues.add(deadCodeStatistics.config_numberOfClassesToIgnore);
        deadCodeStatistics.config_numberOfCustomAnnotations = 55555;
        expectedValues.add(deadCodeStatistics.config_numberOfCustomAnnotations);
        deadCodeStatistics.config_numberOfCustomInterfaces = 6666;
        expectedValues.add(deadCodeStatistics.config_numberOfCustomInterfaces);
        deadCodeStatistics.config_numberOfCustomSuperclasses = 777;
        expectedValues.add(deadCodeStatistics.config_numberOfCustomSuperclasses);
        deadCodeStatistics.config_numberOfCustomXmlDefinitions = 88;
        expectedValues.add(deadCodeStatistics.config_numberOfCustomXmlDefinitions);
        deadCodeStatistics.config_numberOfModulesToSkip = 9;
        expectedValues.add(deadCodeStatistics.config_numberOfModulesToSkip);
        deadCodeStatistics.config_ignoreMainClasses = true;
        expectedValues.add(deadCodeStatistics.config_ignoreMainClasses);
        deadCodeStatistics.config_skipUpdateCheck = false;
        expectedValues.add(deadCodeStatistics.config_skipUpdateCheck);
        for (String key : UsageStatisticsManager.SystemProperties.KEYS.keySet()) {
            String value = emptyToNull(System.getProperties().getProperty(key));
            if (value == null) {
                value = UUID.randomUUID().toString();
                System.getProperties().setProperty(key, value);
            }
            expectedValues.add(value);
        }
        givenModes(NetworkModes.ONLINE, InteractivityModes.INTERACTIVE);

        objectUnderTest.sendUsageStatistics(deadCodeStatistics);

        assertThatStatisticsWereSent(expectedValues.toArray());
    }

    @Test
    public void shouldHandleNon200ErrorCode() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.NON_INTERACTIVE);
        givenHttpTransferResultsIn(503);

        objectUnderTest.sendUsageStatistics(new DeadCodeStatistics(FALSE, null));

        assertThatStatisticsWereSent();
    }

    @Test
    public void shouldHandleFailureInHttpTransfer() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.NON_INTERACTIVE);
        givenHttpConnectionFails();

        objectUnderTest.sendUsageStatistics(new DeadCodeStatistics(FALSE, null));

        verify(log).info(and(contains("Fail"), contains("usage statistics")));
    }

    @Test
    public void shouldHandleFailureOfMavenRuntime() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.NON_INTERACTIVE);
        givenProjectPropertiesCannotBeDetermined();

        objectUnderTest.sendUsageStatistics(new DeadCodeStatistics(FALSE, null));

        assertThatStatisticsWereSent();
    }

    @Test
    public void shouldAbortIfInNonInteractiveModeAndNonConfigured() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.NON_INTERACTIVE);

        objectUnderTest.sendUsageStatistics(new DeadCodeStatistics(null, null));

        assertThatStatisticsWereNotSent();
    }

    @Test
    public void shouldAbortIfRequestedByUser() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.INTERACTIVE);

        objectUnderTest.sendUsageStatistics(new DeadCodeStatistics(null, null));

        assertThatStatisticsWereNotSent();
    }

    @Test
    public void shouldSendStatisticsWithGivenCommentIfUserAgrees() throws Exception {
        String comment = "Just do it!";
        givenModes(NetworkModes.ONLINE, InteractivityModes.INTERACTIVE);
        givenUserAgreesToSendStatistics(comment);

        objectUnderTest.sendUsageStatistics(new DeadCodeStatistics(null, null));

        assertThatStatisticsWereSent(comment);
    }

    @Test
    public void shouldSendStatisticsWithConfiguredCommentIfUserAgrees() throws Exception {
        String comment = "Just do it!";
        givenModes(NetworkModes.ONLINE, InteractivityModes.INTERACTIVE);
        givenUserAgreesToSendStatistics();

        objectUnderTest.sendUsageStatistics(new DeadCodeStatistics(null, comment));

        assertThatStatisticsWereSent(comment);
    }

    @Test
    public void abortsIfPromptingFails() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.INTERACTIVE);
        givenPrompterFails();

        objectUnderTest.sendUsageStatistics(new DeadCodeStatistics(null, null));

        assertThatStatisticsWereNotSent();
    }

    @SuppressWarnings("deprecation") // there's no non-deprecated constructor for MavenSession :|
    private void givenModes(NetworkModes networkMode, InteractivityModes interactivity) throws IllegalAccessException {
        DefaultMavenExecutionRequest mavenExecutionRequest = new DefaultMavenExecutionRequest();
        mavenExecutionRequest.setOffline(NetworkModes.OFFLINE == networkMode);
        mavenExecutionRequest.setInteractiveMode(InteractivityModes.INTERACTIVE == interactivity);
        mavenExecutionRequest.setSystemProperties(System.getProperties());

        LegacySupport legacySupport = mock(LegacySupport.class);
        when(legacySupport.getSession()).thenReturn(new MavenSession(null, null, mavenExecutionRequest, null));
        setVariableValueInObject(objectUnderTest, "legacySupport", legacySupport);
    }

    private void givenHttpTransferResultsIn(int responseCode) throws Exception {
        InputStream inputMock = mock(InputStream.class);
        when(inputMock.read(any(byte[].class), anyInt(), anyInt())).then(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                byte[] buffer = (byte[]) invocation.getArguments()[0];
                int offset = (Integer) invocation.getArguments()[1];
                buffer[offset] = 'F';
                buffer[1 + offset] = 'A';
                buffer[2 + offset] = 'I';
                buffer[3 + offset] = 'L';
                return 4;
            }
        }).thenReturn(-1);
        urlConnectionMock = mock(HttpURLConnection.class);
        when(urlConnectionMock.getInputStream()).thenReturn(inputMock);
        outputStream = new ByteArrayOutputStream();
        when(urlConnectionMock.getOutputStream()).thenReturn(outputStream);
        when(urlConnectionMock.getResponseCode()).thenReturn(responseCode);
    }

    private void givenHttpConnectionFails() throws IOException {
        urlConnectionMock = mock(HttpURLConnection.class);
        doThrow(new IOException("I/O You!")).when(urlConnectionMock).connect();
    }

    private void givenProjectPropertiesCannotBeDetermined() throws MavenRuntimeException, IllegalAccessException {
        MavenRuntime mock = mock(MavenRuntime.class);
        when(mock.getProjectProperties(any(Class.class))).thenThrow(new MavenRuntimeException("Yack Fou!"));
        setVariableValueInObject(objectUnderTest, "mavenRuntime", mock);
    }

    private void givenUserAgreesToSendStatistics(String comment) throws IllegalAccessException, PrompterException {
        Prompter mock = mock(Prompter.class);
        when(mock.prompt(anyString(), anyList(), anyString())).thenReturn("Y");
        if (comment != null) {
            when(mock.prompt(anyString())).thenReturn(comment);
        }
        setVariableValueInObject(objectUnderTest, "prompter", mock);
    }

    private void givenUserAgreesToSendStatistics() throws IllegalAccessException, PrompterException {
        givenUserAgreesToSendStatistics(null);
    }

    private void givenPrompterFails() throws IllegalAccessException, PrompterException {
        Prompter mock = mock(Prompter.class);
        when(mock.prompt(anyString(), anyList(), anyString())).thenThrow(new PrompterException("Prompt You!"));
        setVariableValueInObject(objectUnderTest, "prompter", mock);
    }

    private void assertThatStatisticsWereNotSent() throws Exception {
        verifyZeroInteractions(urlConnectionMock);
    }

    private void assertThatStatisticsWereSent() throws Exception {
        verify(urlConnectionMock).getResponseCode();
    }

    private void assertThatStatisticsWereSent(Object... values) throws UnsupportedEncodingException {
        String sendData = outputStream.toString("UTF-8");
        for (Object value : values) {
            assertThat(sendData, Matchers.containsString(URLEncoder.encode(String.valueOf(value), "UTF-8")));
        }
    }

    private enum NetworkModes {
        ONLINE, OFFLINE


    }

    private enum InteractivityModes {
        INTERACTIVE, NON_INTERACTIVE
    }

}