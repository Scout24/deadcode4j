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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Component(role = UsageStatisticsManager.class)
public class UsageStatisticsManager {

    @Requirement
    private LegacySupport legacySupport;

    @Requirement
    private MavenRuntime mavenRuntime;

    @Requirement
    private Prompter prompter;

    public void sendUsageStatistics() {
        final Logger logger = LoggerFactory.getLogger(getClass());
        if (!legacySupport.getSession().getRequest().isInteractiveMode()) {
            logger.info("Running in non-interactive mode; skipping sending of usage statistics.");
        }
        try {
            String answer = prompter.prompt("May I send the aforementioned usage statistics?", Arrays.asList("Y", "N"), "Y");
            if ("N".equals(answer)) {
                logger.info("Sending usage statistics is aborted.");
                return;
            }
        } catch (PrompterException e) {
            logger.debug("Prompter failed!", e);
            logger.info("Failed to interact with the user!");
            return;
        }

        SystemProperties systemProperties = SystemProperties.from(legacySupport, mavenRuntime);

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
        public String deadcode4jVersion = "1.6";
        public String javaClassVersion;
        public String javaRuntimeName;
        public String javaRuntimeVersion;
        public String javaSpecificationVersion;
        public String javaVersion;
        public String javaVmSpecificationVersion;
        public String mavenBuildVersion;
        public String mavenVersion;
        public String osName;
        public String osVersion;
        public String userCountry;
        public String userLanguage;

        public static SystemProperties from(LegacySupport legacySupport, MavenRuntime mavenRuntime) {
            SystemProperties systemProperties = new SystemProperties();
            try {
                MavenProjectProperties projectProperties = mavenRuntime.getProjectProperties(SystemProperties.class);
                systemProperties.deadcode4jVersion = projectProperties.getVersion();
            } catch (MavenRuntimeException e) {
                LoggerFactory.getLogger(SystemProperties.class).debug("Failed to determine MavenRuntime.", e);
            }
            Properties properties = legacySupport.getSession().getRequest().getSystemProperties();
            systemProperties.javaClassVersion = properties.getProperty("java.class.version");
            systemProperties.javaRuntimeName = properties.getProperty("java.runtime.name");
            systemProperties.javaRuntimeVersion = properties.getProperty("java.runtime.version");
            systemProperties.javaSpecificationVersion = properties.getProperty("java.specification.version");
            systemProperties.javaVersion = properties.getProperty("java.version");
            systemProperties.javaVmSpecificationVersion = properties.getProperty("java.vm.specification.version");
            systemProperties.mavenBuildVersion = properties.getProperty("maven.build.version");
            systemProperties.mavenVersion = properties.getProperty("maven.version");
            systemProperties.osName = properties.getProperty("os.name");
            systemProperties.osVersion = properties.getProperty("os.version");
            systemProperties.userCountry = properties.getProperty("user.country");
            systemProperties.userLanguage = properties.getProperty("user.language");
            return systemProperties;
        }
    }

}
