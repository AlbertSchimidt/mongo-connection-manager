package com.albertschimidt.mongoconnectionmanager.helpers;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.internal.MongoClientImpl;
import com.mongodb.internal.connection.ServerAddressHelper;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.mongodb.autoconfigure.MongoProperties;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.Collections;
import java.util.Objects;

@UtilityClass
public class MongoConfigHelper {

    private boolean isUri(MongoProperties mongoProperties) {
        return StringUtils.isNotBlank(mongoProperties.getUri());
    }

    public MongoCredential newMongoCredential(MongoProperties mongoProperties) {
        return MongoCredential.createCredential(mongoProperties.getUsername(),
                mongoProperties.getDatabase(),
                mongoProperties.getPassword());
    }

    public MongoClient newMongoClient(MongoProperties mongoProperties) {
        if (isUri(mongoProperties)) {
            return MongoClients.create(mongoProperties.getUri());
        }

        return MongoClients.create(
                MongoClientSettings.builder().applyToClusterSettings(
                        builder -> builder.hosts(
                                Collections.singletonList(
                                        ServerAddressHelper.createServerAddress(mongoProperties.getHost(),
                                                mongoProperties.getPort())
                                )
                        )
                ).credential(newMongoCredential(mongoProperties)).build());
    }

    public MongoDatabaseFactory newMongoDatabaseFactory(MongoClient mongoClient, MongoProperties mongoProperties) {
        if (isUri(mongoProperties)) {
            MongoCredential mongoCredential = ((MongoClientImpl) mongoClient).getSettings().getCredential();

            if (Objects.nonNull(mongoCredential)) {
                return new SimpleMongoClientDatabaseFactory(mongoClient,
                        ((MongoClientImpl) mongoClient).getSettings().getCredential().getSource());
            }

            return new SimpleMongoClientDatabaseFactory(mongoProperties.getUri());
        }

        return new SimpleMongoClientDatabaseFactory(mongoClient, mongoProperties.getDatabase());
    }

}
