package com.albertschimidt.mongoconnectionmanager.configs;

import com.albertschimidt.mongoconnectionmanager.utils.LoggingUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@AutoConfiguration
@AutoConfigureBefore({
        MongoAutoConfiguration.class,
        DataMongoAutoConfiguration.class,
        DataMongoRepositoriesAutoConfiguration.class
})
public class MongoConnectionInitializer {

    public MongoConnectionInitializer() {
        LoggingUtils.log("Initializing lib");
    }

    @Bean
    @Primary
    public MongoConnectionRegister mongoConnectionRegister(GenericApplicationContext context,
                                                           ConfigurableEnvironment environment) {
        return new MongoConnectionRegister(context, environment);
    }

}
