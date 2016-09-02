package at.ac.tuwien.dsg.hcu.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ConfigJson {

    protected JSONTokener tokener = null;
    protected JSONObject root = null;    
    protected String path;

    public ConfigJson(String file) throws FileNotFoundException, IOException,
            JSONException {
        tokener = new JSONTokener(new FileInputStream(file));
        root = new JSONObject(tokener);
        path = file;
    }

    public JSONObject getRoot() {
        return root;
    }

    public String getPath() {
        return path;
    }
}
