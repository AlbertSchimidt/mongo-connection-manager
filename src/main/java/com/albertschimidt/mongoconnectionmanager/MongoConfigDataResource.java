package com.albertschimidt.mongoconnectionmanager;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.boot.context.config.ConfigDataResource;

@EqualsAndHashCode(callSuper = true)
@Value
public class MongoConfigDataResource extends ConfigDataResource {

    String configUri;
    String configName;

    public MongoConfigDataResource(String configUri, String configName) {
        super();
        this.configUri = configUri;
        this.configName = configName;
    }

    @Override
    public String toString() {
        return "MongoConfigDataResource{" +
                "configUri='" + configUri + '\'' +
                ", configName='" + configName + '\'' +
                '}';
    }

}
