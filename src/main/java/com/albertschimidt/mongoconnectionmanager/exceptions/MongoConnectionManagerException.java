package com.albertschimidt.mongoconnectionmanager.exceptions;

import com.albertschimidt.mongoconnectionmanager.constants.MongoConstants;
import org.apache.commons.lang3.StringUtils;

public class MongoConnectionManagerException extends RuntimeException {

    public MongoConnectionManagerException() {
        super();
    }

    public MongoConnectionManagerException(String message) {
        super(StringUtils.join(MongoConstants.MONGO_CONNECTION_MANAGER_LOG, message));
    }

    public MongoConnectionManagerException(String message, Throwable cause) {
        super(StringUtils.join(MongoConstants.MONGO_CONNECTION_MANAGER_LOG, message), cause);
    }

    public MongoConnectionManagerException(Throwable cause) {
        super(cause);
    }

    protected MongoConnectionManagerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(StringUtils.join(MongoConstants.MONGO_CONNECTION_MANAGER_LOG, message), cause, enableSuppression, writableStackTrace);
    }

}
