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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static de.is24.deadcode4j.Utils.nullIfEmpty;
import static java.util.Arrays.asList;

@Component(role = UsageStatisticsManager.class)
public class UsageStatisticsManager {

    @Requirement
    private LegacySupport legacySupport;

    @Requirement
    private MavenRuntime mavenRuntime;

    @Requirement
    private Prompter prompter;

    public void sendUsageStatistics(Boolean skipSendingUsageStatistics, DeadCodeStatistics deadCodeStatistics) {
        final Logger logger = getLogger();
        if (Boolean.FALSE.equals(skipSendingUsageStatistics)) {
            logger.debug("Configuration wants to me to skip sending usage statistics.");
            return;
        }
        if (legacySupport.getSession().isOffline()) {
            logger.info("Running in offline mode; skipping sending of usage statistics.");
            return;
        }
        SystemProperties systemProperties = SystemProperties.from(legacySupport, mavenRuntime);
        String comment = null;
        if (Boolean.TRUE.equals(skipSendingUsageStatistics)) {
            logger.debug("Configured to send usage statistics.");
        } else {
            if (!legacySupport.getSession().getRequest().isInteractiveMode()) {
                logger.info("Running in non-interactive mode; skipping sending of usage statistics.");
                return;
            }
            StringBuilder buffy = listStatistics(deadCodeStatistics, systemProperties);
            try {
                buffy.append("\nMay I report those usage statistics?");
                String answer = prompter.prompt(buffy.toString(), asList("Y", "N"), "Y");
                if ("N".equals(answer)) {
                    logger.info("Sending usage statistics is aborted.");
                    logger.info("You may configure deadcode4j to permanently disable sending usage statistics.");
                    return;
                }
                comment = nullIfEmpty(prompter.prompt("Awesome! Would you like to state a testimonial or give a comment? Here you can"));
            } catch (PrompterException e) {
                logger.debug("Prompter failed!", e);
                logger.info("Failed to interact with the user!");
                return;
            }
        }

        final URL url;
        try {
            url = new URL(null, "https://docs.google.com/forms/d/1-XZeeAyHrucUMREQLHZEnZ5mhywYZi5Dk9nfEv7U2GU/formResponse?" +
                    "entry.1472283741=1.6&" +
                    "entry.131773189=3.1&" +
                    "entry.344342021=1.7_55");
        } catch (MalformedURLException e) {
            logger.debug("Failed to create form URL!", e);
            logger.info("Failed preparing usage statistics.");
            return;
        }
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.setAllowUserInteraction(false);
            urlConnection.setConnectTimeout(2000);
            urlConnection.setReadTimeout(5000);
            urlConnection.connect();
            List<String> answer = IOUtils.readLines(urlConnection.getInputStream());
            for (String line : answer) {
                System.out.println(line);
            }
        } catch (IOException e) {
            logger.debug("Failed to send statistics!", e);
            logger.info("Failed sending usage statistics.");
        }
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    private StringBuilder listStatistics(DeadCodeStatistics deadCodeStatistics, SystemProperties systemProperties) {
        StringBuilder buffy = new StringBuilder("I gathered the following system properties:");
        for (String key : new TreeSet<String>(SystemProperties.KEYS.keySet())) {
            buffy.append("\n  ").append(key).append(": ").append(systemProperties.values.get(key));
        }

        buffy.append("\nand extracted this from your configuration: ");
        buffy.append("\n  value for ignoreMainClasses: ").
                append(deadCodeStatistics.config_ignoreMainClasses);
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

        return buffy;
    }

    public static class DeadCodeStatistics {
        public int numberOfAnalyzedModules;
        public int numberOfAnalyzedClasses;
        public int numberOfDeadClassesFound;
        public boolean config_ignoreMainClasses;
        public int config_numberOfClassesToIgnore;
        public int config_numberOfCustomAnnotations;
        public int config_numberOfCustomInterfaces;
        public int config_numberOfCustomSuperclasses;
        public int config_numberOfCustomXmlDefinitions;
        public int config_numberOfModulesToSkip;
        public boolean config_skipUpdateCheck;
    }

    private static class SystemProperties {
        private static final Map<String, String> KEYS = newHashMap();

        static {
            KEYS.put("deadcode4j.version", null);
            KEYS.put("java.class.version", null);
            KEYS.put("java.runtime.name", null);
            KEYS.put("java.runtime.version", null);
            KEYS.put("java.specification.version", null);
            KEYS.put("java.version", null);
            KEYS.put("java.vm.specification.version", null);
            KEYS.put("maven.build.version", null);
            KEYS.put("maven.version", null);
            KEYS.put("os.name", null);
            KEYS.put("os.version", null);
            KEYS.put("user.country", null);
            KEYS.put("user.language", null);
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
    }

}
