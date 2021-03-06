package io.codearte.gradle.nexus.functional.e2e

import groovy.transform.PackageScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.invoke.MethodHandles

//@CompileStatic    //as getNexus*AT() is called from static context, but unfortunately making them static results in:
                    //MissingMethodException: No signature of method: static io.codearte.gradle.nexus.E2EFunctionalTestHelperTrait.getNexusUsernameAT()
@PackageScope
trait E2ESpecHelperTrait implements E2ESpecConstants {

    private static final Logger logT = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String GRADLE_ENVIRONMENT_VARIABLE_PREFIX = 'ORG_GRADLE_PROJECT_'
    private static final String NEXUS_USERNAME_AT_ENVIRONMENT_VARIABLE_NAME = 'nexusUsernameAT'
    private static final String NEXUS_PASSWORD_AT_ENVIRONMENT_VARIABLE_NAME = 'nexusPasswordAT'

    static String nexusUsernameAT
    static String nexusPasswordAT

    void setupSpec() {
        nexusUsernameAT = readNexusUsernameAT()
        nexusPasswordAT = tryToReadNexusPasswordAT()
    }

    //Cannot be private due to limitation of trait & Spock combination
    String readNexusUsernameAT() {
        return System.getenv(NEXUS_USERNAME_AT_ENVIRONMENT_VARIABLE_NAME) ?:
            tryToReadPropertyFromGradleEnvironmentVariable(NEXUS_USERNAME_AT_ENVIRONMENT_VARIABLE_NAME) ?:
                tryToReadPropertyFromGradlePropertiesFile(NEXUS_USERNAME_AT_ENVIRONMENT_VARIABLE_NAME) ?:
                    'nexus-at'
    }

    String tryToReadNexusPasswordAT() {
        //Will not work with empty password. However, support for it would complicate '?;' statement
        return System.getenv(NEXUS_PASSWORD_AT_ENVIRONMENT_VARIABLE_NAME) ?:
            tryToReadPropertyFromGradleEnvironmentVariable(NEXUS_PASSWORD_AT_ENVIRONMENT_VARIABLE_NAME) ?:
                tryToReadPropertyFromGradlePropertiesFile(NEXUS_PASSWORD_AT_ENVIRONMENT_VARIABLE_NAME) ?:
                    { throw new RuntimeException("Nexus password for AT tests is not set in 'gradle.properties' nor system variable " +
                        "'$NEXUS_PASSWORD_AT_ENVIRONMENT_VARIABLE_NAME' although E2E tests execution is enabled") }()
    }

    //TODO: It would be good to not reimplement Gradle logic
    private String tryToReadPropertyFromGradleEnvironmentVariable(String propertyName) {
        return System.getenv("$GRADLE_ENVIRONMENT_VARIABLE_PREFIX$propertyName")
    }

    private String tryToReadPropertyFromGradlePropertiesFile(String propertyName) {
        Properties props = new Properties()
        File gradlePropertiesFile = new File(new File(System.getProperty('user.home'), '.gradle'), 'gradle.properties')
        if (!gradlePropertiesFile.exists()) {
            logT.warn("$gradlePropertiesFile does not exist while reading '$propertyName' value")
            return null
        }
        gradlePropertiesFile.withInputStream { props.load(it) }
        return props.getProperty(propertyName)
    }
}
