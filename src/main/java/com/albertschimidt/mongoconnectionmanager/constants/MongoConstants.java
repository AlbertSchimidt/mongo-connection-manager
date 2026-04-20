package com.albertschimidt.mongoconnectionmanager.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MongoConstants {

    public final String MONGO_CONNECTION_MANAGER = "mongo-connection-manager";
    public final String MONGO_CONNECTION_MANAGER_LOG = "[" + MONGO_CONNECTION_MANAGER + "] ";

    public final String SUFFIX_MONGO_PROPERTIES_URI = ".mongo.uri";

    public final String MONGO_CONNECTIONS_TO_USE_PROPERTY_NAME = "mongo.connections.to.use";

    public final String SUFFIX_MONGO_PROPERTIES = "MongoProperties";
    public final String SUFFIX_MONGO_CLIENT = "MongoClient";
    public final String SUFFIX_MONGO_FACTORY = "MongoFactory";
    public final String SUFFIX_MONGO_TEMPLATE = "MongoTemplate";

}
