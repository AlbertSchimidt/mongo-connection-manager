package com.albertschimidt.mongoconnectionmanager.configs;

import com.albertschimidt.mongoconnectionmanager.PrimaryControl;
import com.albertschimidt.mongoconnectionmanager.constants.MongoConstants;
import com.albertschimidt.mongoconnectionmanager.exceptions.MongoConnectionManagerException;
import com.albertschimidt.mongoconnectionmanager.helpers.MongoConfigHelper;
import com.albertschimidt.mongoconnectionmanager.utils.EnvironmentUtils;
import com.albertschimidt.mongoconnectionmanager.utils.LoggingUtils;
import com.mongodb.client.MongoClient;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.mongodb.autoconfigure.MongoProperties;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
public class MongoConnectionRegister implements BeanDefinitionRegistryPostProcessor {

    private final GenericApplicationContext context;
    private final ConfigurableEnvironment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        LoggingUtils.log("Initializing register");

        try {
            checkIfConfigurationPresent(environment);

            String connectionsToUse = EnvironmentUtils.getPropertyByName(environment, MongoConstants.MONGO_CONNECTIONS_TO_USE_PROPERTY_NAME);

            PrimaryControl primaryControl = new PrimaryControl();

            String[] connections = StringUtils.split(connectionsToUse, ",");
            String[] mongoURIList = EnvironmentUtils.getAllPropertiesBySuffix(environment, MongoConstants.SUFFIX_MONGO_PROPERTIES_URI);

            Arrays.stream(connections).toList().forEach(connection -> {
                configure(registry, environment, connection, mongoURIList, primaryControl);
            });
        } catch (Exception e) {
            throw new MongoConnectionManagerException("Failed to initialize mongo connections: " + e.getMessage(), e);
        }
    }


    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        LoggingUtils.log("MongoDB connection beans registered successfully");
    }

    private void checkIfConfigurationPresent(ConfigurableEnvironment environment) {
        if (!EnvironmentUtils.hasPropertyPrefix(environment, MongoConstants.MONGO_CONNECTIONS_TO_USE_PROPERTY_NAME)) {
            throw new MongoConnectionManagerException(StringUtils.join("[", MongoConstants.MONGO_CONNECTIONS_TO_USE_PROPERTY_NAME, "]", " not found. Verify the application.properties"));
        }
    }

    private void configure(BeanDefinitionRegistry registry, ConfigurableEnvironment environment, String connection, String[] mongoURIList, PrimaryControl primaryControl) {
        if (Arrays.stream(mongoURIList).noneMatch(m -> m.contains(connection))) {
            throw new MongoConnectionManagerException(StringUtils.join("URI configuration property for ", connection, " not found. Check mongo-connection-manager application.properties"));
        }

        try {
            registerMongoBeans(registry, environment, connection, StringUtils.join(connection, MongoConstants.SUFFIX_MONGO_PROPERTIES), primaryControl);
        } catch (Exception e) {
            throw new MongoConnectionManagerException("Failed to configure mongo connection for: " + connection, e);
        }
    }

    private void registerMongoBeans(BeanDefinitionRegistry registry, ConfigurableEnvironment environment, String connection, String prefix, PrimaryControl primaryControl) {
        Function<String, MongoProperties> mongoPropertiesSupplier = ps -> Binder.get(environment)
                .bind(prefix, MongoProperties.class)
                .orElseThrow(() -> new IllegalStateException("Missing properties for: " + prefix));

        UnaryOperator<String> generateBeanName = suffix -> connection + suffix;
        MongoProperties properties = mongoPropertiesSupplier.apply(prefix);

        String propertiesBeanName = generateBeanName.apply(MongoConstants.SUFFIX_MONGO_PROPERTIES);
        registry.registerBeanDefinition(propertiesBeanName,
                createPropertiesBeanDefinition(() -> properties, primaryControl));

        String clientBeanName = generateBeanName.apply(MongoConstants.SUFFIX_MONGO_CLIENT);
        registry.registerBeanDefinition(clientBeanName,
                createClientBeanDefinition(() -> MongoConfigHelper.newMongoClient(properties), primaryControl));

        String factoryBeanName = generateBeanName.apply(MongoConstants.SUFFIX_MONGO_FACTORY);
        registry.registerBeanDefinition(factoryBeanName,
                createFactoryBeanDefinition(clientBeanName, properties, primaryControl));

        String templateBeanName = generateBeanName.apply(MongoConstants.SUFFIX_MONGO_TEMPLATE);
        registry.registerBeanDefinition(templateBeanName,
                createTemplateBeanDefinition(factoryBeanName, primaryControl));

        LoggingUtils.log(StringUtils.join("Registered MongoDB beans for: ", connection, "(client: ", clientBeanName, ", factory: ", factoryBeanName, ", template: ", templateBeanName, ")"));
    }

    private <T> BeanDefinition createPropertiesBeanDefinition(Supplier<T> instanceSupplier, PrimaryControl primaryControl) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MongoProperties.class);
        beanDefinition.setInstanceSupplier(instanceSupplier);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        if (!primaryControl.isMongoPropertiesPrimarySet()) {
            beanDefinition.setPrimary(true);
            primaryControl.setMongoPropertiesPrimarySet(true);
        }

        return beanDefinition;
    }

    private <T> BeanDefinition createClientBeanDefinition(Supplier<T> instanceSupplier, PrimaryControl primaryControl) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MongoClient.class);
        beanDefinition.setInstanceSupplier(instanceSupplier);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        if (!primaryControl.isMongoClientPrimarySet()) {
            beanDefinition.setPrimary(true);
            primaryControl.setMongoClientPrimarySet(true);
        }

        return beanDefinition;
    }

    private BeanDefinition createFactoryBeanDefinition(String clientBeanName, MongoProperties properties, PrimaryControl primaryControl) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MongoDatabaseFactory.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        if (!primaryControl.isMongoFactoryPrimarySet()) {
            beanDefinition.setPrimary(true);
            primaryControl.setMongoFactoryPrimarySet(true);
        }

        beanDefinition.setInstanceSupplier(() -> {
            MongoClient client = context.getBean(clientBeanName, MongoClient.class);
            return MongoConfigHelper.newMongoDatabaseFactory(client, properties);
        });

        return beanDefinition;
    }

    private BeanDefinition createTemplateBeanDefinition(String factoryBeanName, PrimaryControl primaryControl) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MongoTemplate.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        if (!primaryControl.isMongoTemplatePrimarySet()) {
            beanDefinition.setPrimary(true);
            primaryControl.setMongoTemplatePrimarySet(true);
        }

        beanDefinition.setInstanceSupplier(() -> {
            MongoDatabaseFactory factory = context.getBean(factoryBeanName, MongoDatabaseFactory.class);
            return new MongoTemplate(factory);
        });

        return beanDefinition;
    }


}
