package dto;

import spark.Request;

public class ClearRequest extends Request {
    Request req;
    public ClearRequest(Request res) {
        this.req = res;
    }
}