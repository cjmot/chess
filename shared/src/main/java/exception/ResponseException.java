package exception;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ResponseException extends Exception {
    public ResponseException(String message) {
        super(message);
    }

    public static ResponseException fromJson(InputStream stream) {
        var map = new Gson().fromJson(new InputStreamReader(stream), HashMap.class);
        String message = map.get("message").toString();
        return new ResponseException(message);
    }
}
