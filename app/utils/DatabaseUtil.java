package utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.RequestException;
import play.libs.Json;
import play.mvc.Result;

import static play.mvc.Results.status;

public class DatabaseUtil {

    public static Result throwableToResult (Throwable error) {
        Result status = DatabaseUtil.statusFromThrowable(error);
        if (status != null) {
            return status;
        }
        ObjectNode result = Json.newObject();
        result.put("status", 501);
        result.put("message", error.getLocalizedMessage());
        return status(501, result);
    }

    public static Result statusFromThrowable (Throwable error) {
        if (error instanceof RequestException) {
            return statusFromThrowable((RequestException) error);
        }
        if (error.getCause() == null) {
            return null;
        }
        return statusFromThrowable(error.getCause());
    }

    public static Result statusFromThrowable (RequestException error) {
        return status(error.getStatusCode(), objectNodeFromError(error.getStatusCode(), error.getMessage()));
    }

    public static ObjectNode objectNodeFromError (int status, String message) {
        ObjectNode result = Json.newObject();
        result.put("status", status);
        result.put("message", message);
        return result;
    }
}
