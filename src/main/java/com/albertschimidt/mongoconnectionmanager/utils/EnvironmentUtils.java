package com.albertschimidt.mongoconnectionmanager.utils;

import lombok.experimental.UtilityClass;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.stream.Collectors;

@UtilityClass
public class EnvironmentUtils {

    public boolean hasPropertyPrefix(Environment environment, String prefix) {
        return ((ConfigurableEnvironment) environment).getPropertySources().stream()
                .filter(EnumerablePropertySource.class::isInstance)
                .flatMap(ps -> Arrays.stream(((EnumerablePropertySource<?>) ps).getPropertyNames()))
                .anyMatch(p -> p.startsWith(prefix));
    }

    public String getPropertyByName(Environment environment, String propertyName) {
        return environment.getProperty(propertyName);
    }

    public String[] getAllPropertiesBySuffix(Environment environment, String suffix) {
        return ((ConfigurableEnvironment) environment).getPropertySources().stream()
                .filter(EnumerablePropertySource.class::isInstance)
                .flatMap(ps -> Arrays.stream(((EnumerablePropertySource<?>) ps).getPropertyNames()))
                .filter(name -> name.endsWith(suffix))
                .toList().toArray(String[]::new);
    }

}
