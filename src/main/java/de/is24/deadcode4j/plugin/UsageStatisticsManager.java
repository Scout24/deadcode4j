package de.is24.deadcode4j.plugin;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.shared.runtime.MavenProjectProperties;
import org.apache.maven.shared.runtime.MavenRuntime;
import org.apache.maven.shared.runtime.MavenRuntimeException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static de.is24.deadcode4j.Utils.nullIfEmpty;
import static java.util.Arrays.asList;

/**
 * Sends usage statistics (via Google Forms).
 *
 * @since 1.6
 */
@Component(role = UsageStatisticsManager.class)
public class UsageStatisticsManager {

    @Requirement
    private LegacySupport legacySupport;

    @Requirement
    private MavenRuntime mavenRuntime;

    @Requirement
    private Prompter prompter;

    /**
     * This is around for testing.
     *
     * @since 1.6
     */
    protected HttpURLConnection openUrlConnection() throws IOException {
        URL url = new URL("https://docs.google.com/forms/d/1-XZeeAyHrucUMREQLHZEnZ5mhywYZi5Dk9nfEv7U2GU/formResponse");
        return HttpURLConnection.class.cast(url.openConnection());
    }

    public void sendUsageStatistics(DeadCodeStatistics deadCodeStatistics) {
        final Logger logger = getLogger();
        if (Boolean.TRUE.equals(deadCodeStatistics.getSkipSendingUsageStatistics())) {
            logger.debug("Configuration wants to me to skip sending usage statistics.");
            return;
        }
        if (legacySupport.getSession().isOffline()) {
            logger.info("Running in offline mode; skipping sending of usage statistics.");
            return;
        }
        SystemProperties systemProperties = SystemProperties.from(legacySupport, mavenRuntime);
        if (Boolean.FALSE.equals(deadCodeStatistics.getSkipSendingUsageStatistics())) {
            logger.debug("Configured to send usage statistics.");
        } else {
            if (!legacySupport.getSession().getRequest().isInteractiveMode()) {
                logger.info("Running in non-interactive mode; skipping sending of usage statistics.");
                return;
            }
            if (!askForPermissionAndComment(deadCodeStatistics, systemProperties)) {
                return;
            }
        }

        Map<String, String> parameters = getParameters(deadCodeStatistics, systemProperties);
        sendUsageStatistics(parameters);
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    private void sendUsageStatistics(Map<String, String> parameters) {
        final Logger logger = getLogger();
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = openUrlConnection();
            urlConnection.setAllowUserInteraction(false);
            urlConnection.setConnectTimeout(2000);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            urlConnection.connect();

            writeParameters(parameters, urlConnection);
            processResponse(urlConnection);
        } catch (IOException e) {
            logger.debug("Failed to send statistics!", e);
            logger.info("Failed sending usage statistics.");
        } finally {
            IOUtils.close(urlConnection);
        }
    }

    private void processResponse(HttpURLConnection urlConnection) throws IOException {
        final Logger logger = getLogger();
        int responseCode = urlConnection.getResponseCode();
        if (responseCode / 100 == 2) {
            logger.info("Usage statistics have been transferred.");
            return;
        }
        logger.info("Could not transfer usage statistics: {}/{}", responseCode, urlConnection.getResponseMessage());
        if (logger.isDebugEnabled()) {
            InputStream inputStream = urlConnection.getInputStream();
            try {
                List<String> response = IOUtils.readLines(inputStream);
                for (String line : response) {
                    logger.debug(line);
                }
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    private void writeParameters(Map<String, String> parameters, HttpURLConnection urlConnection) throws IOException {
        StringBuilder buffy = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            buffy.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), "UTF-8")).append('&');
        }
        OutputStream outputStream = urlConnection.getOutputStream();
        try {
            outputStream.write(buffy.toString().getBytes("UTF-8"));
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private Map<String, String> getParameters(DeadCodeStatistics deadCodeStatistics,
                                              SystemProperties systemProperties) {
        HashMap<String, String> parameters = newHashMap();
        deadCodeStatistics.addRequestParameters(parameters);
        systemProperties.addRequestParameters(parameters);
        return parameters;
    }

    /**
     * @return {@code null} if sending statistics should be aborted
     */
    private boolean askForPermissionAndComment(DeadCodeStatistics deadCodeStatistics, SystemProperties systemProperties) {
        final Logger logger = getLogger();
        StringBuilder buffy = listStatistics(deadCodeStatistics, systemProperties);
        try {
            buffy.append("\nMay I report those usage statistics (via HTTPS)?");
            String answer = prompter.prompt(buffy.toString(), asList("Y", "N"), "Y");
            if ("N".equals(answer)) {
                logger.info("Sending usage statistics is aborted.");
                logger.info("You may configure deadcode4j to permanently disable sending usage statistics.");
                return false;
            }
            if (deadCodeStatistics.getUsageStatisticsComment() == null) {
                deadCodeStatistics.setUsageStatisticsComment(prompter.prompt(
                        "Awesome! Would you like to state a testimonial or give a comment? Here you can"));
            }
            return true;
        } catch (PrompterException e) {
            logger.debug("Prompter failed!", e);
            logger.info("Failed to interact with the user!");
            return false;
        }
    }

    private StringBuilder listStatistics(DeadCodeStatistics deadCodeStatistics, SystemProperties systemProperties) {
        StringBuilder buffy = new StringBuilder("I gathered the following system properties:");
        for (String key : new TreeSet<String>(SystemProperties.KEYS.keySet())) {
            buffy.append("\n  ").append(key).append(": ").append(systemProperties.values.get(key));
        }

        buffy.append("\nextracted this from your configuration: ");
        buffy.append("\n  value for ignoreMainClasses: ").
                append(deadCodeStatistics.config_ignoreMainClasses);
        if (Boolean.TRUE.equals(deadCodeStatistics.getSkipSendingUsageStatistics())) {
            buffy.append("\n  value for skipSendingUsageStatistics: ").
                    append(deadCodeStatistics.getSkipSendingUsageStatistics());
        }
        if (deadCodeStatistics.getUsageStatisticsComment() != null) {
            buffy.append("\n  your comment to deadcode4j: ").
                    append(deadCodeStatistics.getUsageStatisticsComment());
        }
        buffy.append("\n  value for skipUpdateCheck: ").
                append(deadCodeStatistics.config_skipUpdateCheck);
        buffy.append("\n  number of classes to ignore: ").
                append(deadCodeStatistics.config_numberOfClassesToIgnore);
        buffy.append("\n  number of custom annotations: ").
                append(deadCodeStatistics.config_numberOfCustomAnnotations);
        buffy.append("\n  number of custom interfaces: ").
                append(deadCodeStatistics.config_numberOfCustomInterfaces);
        buffy.append("\n  number of custom superclasses: ").
                append(deadCodeStatistics.config_numberOfCustomSuperclasses);
        buffy.append("\n  number of custom XML definitions: ").
                append(deadCodeStatistics.config_numberOfCustomXmlDefinitions);
        buffy.append("\n  number of modules to skip: ").
                append(deadCodeStatistics.config_numberOfModulesToSkip);

        buffy.append("\nand gathered those results: ");
        buffy.append("\n  analyzed classes: ").append(deadCodeStatistics.numberOfAnalyzedClasses);
        buffy.append("\n  analyzed modules: ").append(deadCodeStatistics.numberOfAnalyzedModules);
        buffy.append("\n  found dead classes: ").append(deadCodeStatistics.numberOfDeadClassesFound);

        return buffy;
    }

    public static class DeadCodeStatistics {
        private final Boolean skipSendingUsageStatistics;
        private String usageStatisticsComment;
        public int numberOfAnalyzedClasses;
        public int numberOfAnalyzedModules;
        public int numberOfDeadClassesFound;
        public boolean config_ignoreMainClasses;
        public int config_numberOfClassesToIgnore;
        public int config_numberOfCustomAnnotations;
        public int config_numberOfCustomInterfaces;
        public int config_numberOfCustomSuperclasses;
        public int config_numberOfCustomXmlDefinitions;
        public int config_numberOfModulesToSkip;
        public boolean config_skipUpdateCheck;

        /**
         * Creates a new instance of {@code DeadCodeStatistics}.
         *
         * @param skipSendingUsageStatistics the configuration value indicating if statistics should be sent or not
         * @param usageStatisticsComment the configured comment
         */
        public DeadCodeStatistics(Boolean skipSendingUsageStatistics, String usageStatisticsComment) {
            this.skipSendingUsageStatistics = skipSendingUsageStatistics;
            setUsageStatisticsComment(usageStatisticsComment);
        }

        public Boolean getSkipSendingUsageStatistics() {
            return skipSendingUsageStatistics;
        }

        public String getUsageStatisticsComment() {
            return usageStatisticsComment;
        }

        public void setUsageStatisticsComment(String usageStatisticsComment) {
            this.usageStatisticsComment = nullIfEmpty(usageStatisticsComment);
        }

        public void addRequestParameters(HashMap<String, String> parameters) {
            parameters.put("entry.1074756797", String.valueOf(numberOfAnalyzedClasses));
            parameters.put("entry.1318897553", String.valueOf(numberOfAnalyzedModules));
            parameters.put("entry.582394579", String.valueOf(numberOfDeadClassesFound));
            parameters.put("entry.2113716156", String.valueOf(config_ignoreMainClasses));
            parameters.put("entry.1255607340", String.valueOf(config_numberOfClassesToIgnore));
            parameters.put("entry.837156809", String.valueOf(config_numberOfCustomAnnotations));
            parameters.put("entry.1900438860", String.valueOf(config_numberOfCustomInterfaces));
            parameters.put("entry.2138491452", String.valueOf(config_numberOfCustomSuperclasses));
            parameters.put("entry.1308824804", String.valueOf(config_numberOfCustomXmlDefinitions));
            parameters.put("entry.1094908901", String.valueOf(config_numberOfModulesToSkip));
            parameters.put("entry.1760639029", String.valueOf(config_skipUpdateCheck));
            if (this.skipSendingUsageStatistics != null) {
                parameters.put("entry.1975817511", String.valueOf(skipSendingUsageStatistics));
            }
            if (this.usageStatisticsComment != null) {
                parameters.put("entry.2135548690", this.usageStatisticsComment);
            }
        }

    }

    private static class SystemProperties {
        private static final Map<String, String> KEYS = newHashMap();

        static {
            KEYS.put("deadcode4j.version", "entry.1472283741");
            KEYS.put("java.class.version", "entry.1951632658");
            KEYS.put("java.runtime.name", "entry.890615248");
            KEYS.put("java.runtime.version", "entry.120214478");
            KEYS.put("java.specification.version", "entry.1933178438");
            KEYS.put("java.version", "entry.344342021");
            KEYS.put("java.vm.specification.version", "entry.1718484204");
            KEYS.put("maven.build.version", "entry.1702626216");
            KEYS.put("maven.version", "entry.131773189");
            KEYS.put("os.name", "entry.1484769972");
            KEYS.put("os.version", "entry.1546424580");
            KEYS.put("user.country", "entry.1667669021");
            KEYS.put("user.language", "entry.1213472042");
        }

        private final Map<String, String> values = newHashMapWithExpectedSize(KEYS.size());

        private SystemProperties() {
            values.put("deadcode4j.version", "1.6");
        }

        public static SystemProperties from(LegacySupport legacySupport, MavenRuntime mavenRuntime) {
            SystemProperties systemProperties = new SystemProperties();
            try {
                MavenProjectProperties projectProperties = mavenRuntime.getProjectProperties(SystemProperties.class);
                systemProperties.values.put("deadcode4j.version", projectProperties.getVersion());
            } catch (MavenRuntimeException e) {
                LoggerFactory.getLogger(SystemProperties.class).debug("Failed to determine MavenRuntime.", e);
            }
            Properties properties = legacySupport.getSession().getRequest().getSystemProperties();
            for (String key : KEYS.keySet()) {
                String property = nullIfEmpty(properties.getProperty(key));
                if (property != null) {
                    systemProperties.values.put(key, property);
                }
            }
            return systemProperties;
        }

        public void addRequestParameters(HashMap<String, String> parameters) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                parameters.put(KEYS.get(entry.getKey()), entry.getValue());
            }
        }

    }

}
