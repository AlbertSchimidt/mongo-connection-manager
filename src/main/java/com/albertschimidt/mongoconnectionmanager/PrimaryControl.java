package com.albertschimidt.mongoconnectionmanager;

import lombok.Data;

@Data
public class PrimaryControl {

    private boolean mongoPropertiesPrimarySet = false;
    private boolean mongoClientPrimarySet = false;
    private boolean mongoFactoryPrimarySet = false;
    private boolean mongoTemplatePrimarySet = false;

}
