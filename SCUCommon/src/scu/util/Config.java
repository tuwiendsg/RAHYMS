package scu.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private Properties properties = null;

    public Config(String file) throws FileNotFoundException, IOException {
        properties = new Properties(); 
        properties.load(new FileInputStream(file));  
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

}
