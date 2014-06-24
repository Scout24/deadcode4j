package de.is24.deadcode4j.plugin;

import de.is24.deadcode4j.junit.LoggingRule;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.shared.runtime.MavenProjectProperties;
import org.apache.maven.shared.runtime.MavenRuntime;
import org.apache.maven.shared.runtime.MavenRuntimeException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.ReflectionUtils;
import org.eclipse.aether.RepositorySystemSession;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static de.is24.deadcode4j.plugin.UsageStatisticsManager.DeadCodeStatistics;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UsageStatisticsManager.class)
public final class A_UsageStatisticsManager {

    @Rule
    public LoggingRule enableLogging = new LoggingRule();

    private UsageStatisticsManager objectUnderTest;
    private MavenSession mavenSession;
    private URL urlMock;
    private MavenRuntime mavenRuntimeMock;
    private Prompter prompterMock;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new UsageStatisticsManager();
        ReflectionUtils.setVariableValueInObject(objectUnderTest, "legacySupport", new LegacySupport() {
            @Override
            public MavenSession getSession() {
                return mavenSession;
            }

            @Override
            public void setSession(MavenSession session) {
                throw new UnsupportedOperationException();
            }

            @Override
            public RepositorySystemSession getRepositorySession() {
                throw new UnsupportedOperationException();
            }
        });
        mavenRuntimeMock = Mockito.mock(MavenRuntime.class);
        Mockito.when(mavenRuntimeMock.getProjectProperties(Mockito.any(Class.class))).thenReturn(
                new MavenProjectProperties("de.is24", "junit", "42.23"));
        ReflectionUtils.setVariableValueInObject(objectUnderTest, "mavenRuntime", mavenRuntimeMock);

        prompterMock = Mockito.mock(Prompter.class);
        Mockito.when(prompterMock.prompt(anyString(), anyList(), anyString())).thenReturn("N");
        ReflectionUtils.setVariableValueInObject(objectUnderTest, "prompter", prompterMock);

        givenHttpResultsIn(200);
    }

    @Test
    public void shouldDoNothingIfSoConfigured() throws Exception {
        objectUnderTest.sendUsageStatistics(TRUE, new DeadCodeStatistics());

        assertThatStatisticsWereNotSent();
    }

    @Test
    public void shouldDoNothingIfInOfflineMode() throws Exception {
        givenModes(NetworkModes.OFFLINE, InteractivityModes.INTERACTIVE);

        objectUnderTest.sendUsageStatistics(FALSE, new DeadCodeStatistics());

        assertThatStatisticsWereNotSent();
    }

    @Test
    public void shouldSimplySendStatisticsIfSoConfigured() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.NON_INTERACTIVE);

        objectUnderTest.sendUsageStatistics(FALSE, new DeadCodeStatistics());

        assertThatStatisticsWereSent();
    }

    @Test
    public void shouldHandleNon200ErrorCode() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.NON_INTERACTIVE);
        givenHttpResultsIn(400);

        DeadCodeStatistics deadCodeStatistics = new DeadCodeStatistics();
        deadCodeStatistics.config_skipSendingUsageStatistics = FALSE; // code coverage
        objectUnderTest.sendUsageStatistics(FALSE, deadCodeStatistics);

        assertThatStatisticsWereSent();
    }

    @Test
    public void shouldHandleFailureWithMavenRuntime() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.NON_INTERACTIVE);
        givenProjectPropertiesCannotBeDetermined();

        objectUnderTest.sendUsageStatistics(FALSE, new DeadCodeStatistics());

        assertThatStatisticsWereSent();
    }

    @Test
    public void shouldAbortIfInNonInteractiveModeAndNonConfigured() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.NON_INTERACTIVE);

        objectUnderTest.sendUsageStatistics(null, new DeadCodeStatistics());

        assertThatStatisticsWereNotSent();
    }

    @Test
    public void shouldAbortIfRequestedByUser() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.INTERACTIVE);

        objectUnderTest.sendUsageStatistics(null, new DeadCodeStatistics());

        assertThatStatisticsWereNotSent();
    }

    @Test
    public void shouldSendStatisticsIfUserAgrees() throws Exception {
        givenModes(NetworkModes.ONLINE, InteractivityModes.INTERACTIVE);
        givenUserAgreesToSendStatistics("Just do it!");

        objectUnderTest.sendUsageStatistics(null, new DeadCodeStatistics());

        assertThatStatisticsWereSent();
    }

    private void givenModes(NetworkModes networkMode, InteractivityModes interactivity) {
        DefaultMavenExecutionRequest mavenExecutionRequest = new DefaultMavenExecutionRequest();
        mavenExecutionRequest.setOffline(NetworkModes.OFFLINE == networkMode);
        mavenExecutionRequest.setInteractiveMode(InteractivityModes.INTERACTIVE == interactivity);
        this.mavenSession = new MavenSession(null, null, mavenExecutionRequest, null);
    }

    private void givenHttpResultsIn(int responseCode) throws Exception {
        InputStream inputMock = mock(InputStream.class);
        when(inputMock.read(Mockito.any(byte[].class), Matchers.anyInt(), Mockito.anyInt())).then(new Answer<Integer>() {
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
        HttpURLConnection connectionMock = mock(HttpURLConnection.class);
        when(connectionMock.getInputStream()).thenReturn(inputMock);
        when(connectionMock.getOutputStream()).thenReturn(mock(OutputStream.class));
        when(connectionMock.getResponseCode()).thenReturn(responseCode);
        urlMock = mock(URL.class);
        when(urlMock.openConnection()).thenReturn(connectionMock);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(urlMock);
    }

    private void givenProjectPropertiesCannotBeDetermined() throws MavenRuntimeException {
        Mockito.reset(mavenRuntimeMock);
        Mockito.when(mavenRuntimeMock.getProjectProperties(Mockito.any(Class.class))).thenThrow(new MavenRuntimeException("Yack Fou"));
    }

    private void givenUserAgreesToSendStatistics(String comment) throws PrompterException {
        Mockito.reset(prompterMock);
        prompterMock = Mockito.mock(Prompter.class);
        Mockito.when(prompterMock.prompt(anyString(), anyList(), anyString())).thenReturn("Y");
        Mockito.when(prompterMock.prompt(anyString(), anyString())).thenReturn(comment);
    }

    private void assertThatStatisticsWereNotSent() throws Exception {
        verifyNew(URL.class, Mockito.never());
    }

    private void assertThatStatisticsWereSent() throws Exception {
        verifyNew(URL.class);
    }

    private static enum NetworkModes {
        ONLINE, OFFLINE
    }

    private static enum InteractivityModes {
        INTERACTIVE, NON_INTERACTIVE
    }

}