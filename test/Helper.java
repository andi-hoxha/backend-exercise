import models.requests.AuthRequestModel;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

public class Helper {

    public static Result authenticate(Application app, AuthRequestModel authRequestModel){
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/authenticate").bodyJson(Json.toJson(authRequestModel));
        return Helpers.route(app,request);
    }

    public static String getAccessToken(Result result){
        return play.test.Helpers.contentAsString(result);
    }
}
