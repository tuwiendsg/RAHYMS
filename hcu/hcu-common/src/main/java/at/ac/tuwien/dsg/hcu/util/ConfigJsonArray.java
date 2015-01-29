package at.ac.tuwien.dsg.hcu.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

public class ConfigJsonArray {

    protected JSONTokener tokener = null;
    protected JSONArray root = null;    

    public ConfigJsonArray(String file) throws FileNotFoundException, IOException,
            JSONException {
        tokener = new JSONTokener(new FileInputStream(file));
        root = new JSONArray(tokener);
    }

    public JSONArray getRoot() {
        return root;
    }

}
