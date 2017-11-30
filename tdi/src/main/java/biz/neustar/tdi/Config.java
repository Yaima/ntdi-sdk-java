package biz.neustar.tdi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Collections;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Config {
    private static final Logger log = LoggerFactory.getLogger(Config.class);

    public Map<String, Object> map = Collections.emptyMap();

    public <T> T get(String key) {
        return this.<T>get(key, null);
    }

    public <T> T get(String key, T defaultValue) {
        T ret = null;
        Map<String, Object> curMap = this.map;

        for (String next: key.split("\\.")) {
            Object val = curMap.get(next);

            if (val == null) {
                return defaultValue;
            }
            else if (val instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nextMap = (Map<String, Object>)val;
                curMap = nextMap;
            }
            else {
                curMap = Collections.emptyMap();  // if we still having looping to do, want to return defaultValue
            }
            @SuppressWarnings("unchecked")
            T newRet = (T)val;
            ret = newRet;
        }
        return ret;
    }

    public Config(String configPath) throws IOException {
        log.debug("loading config {}", configPath);
        
        File file = new File(configPath);
        FileInputStream inStream = new FileInputStream(file);
        this.map = new ObjectMapper().readValue(inStream, new TypeReference<Map<String, Object>>() {
        });
    }
}
