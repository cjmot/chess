package server;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public interface Handler {
    default boolean notValidJson(String json) {
        try {
            JsonParser.parseString(json);
            return false;
        } catch (JsonSyntaxException e) {
            return true;
        }
    }
}
