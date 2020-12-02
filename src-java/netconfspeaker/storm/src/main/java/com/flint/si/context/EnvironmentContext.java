package com.flint.si.context;

import java.util.Properties;

public class EnvironmentContext {
    private static EnvironmentContext context;
    private Properties prop;
    private EnvironmentContext(){
        //TODO load config to establish communication with other system components
        prop = new Properties();
        prop.setProperty("db.address", "127.0.0.1");
        prop.setProperty("db.port", "2022");
        prop.setProperty("kafka.bootstrapServers", "127.0.0.1:9092");
        
    }
    
    public static EnvironmentContext getInstance() {
        if (context == null){
            context = new EnvironmentContext();
        }
        return context;
	}
    
    public String getProperty(String key){
        return prop.getProperty(key);
    }

}
