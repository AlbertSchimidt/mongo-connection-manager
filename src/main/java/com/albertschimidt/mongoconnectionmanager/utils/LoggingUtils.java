package com.albertschimidt.mongoconnectionmanager.utils;

import com.albertschimidt.mongoconnectionmanager.constants.MongoConstants;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LoggingUtils {

    public void log(String message) {
        if (message == null) {
            return;
        }

        System.out.println(MongoConstants.MONGO_CONNECTION_MANAGER_LOG + message);
    }

}
