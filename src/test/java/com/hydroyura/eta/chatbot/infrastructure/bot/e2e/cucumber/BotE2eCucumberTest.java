package com.hydroyura.eta.chatbot.infrastructure.bot.e2e.cucumber;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Запускатор Cucumber-сценариев: JUnit Platform Suite с движком {@code cucumber} и выборкой
 * {@code features/*.feature} из classpath. Имя класса соответствует surefire-паттерну
 * {@code **\/*Test.java}, поэтому сценарии выполняются штатным {@code ./mvnw test}/{@code verify}.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.hydroyura.eta.chatbot.infrastructure.bot.e2e.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "json:target/cucumber-reports/cucumber.json,html:target/cucumber-reports/cucumber.html")
public class BotE2eCucumberTest {
}
