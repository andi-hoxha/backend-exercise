
import models.requests.AuthRequestModel;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class AuthController extends WithApplication {
    @Test
    public void loginTest(){
        AuthRequestModel authRequest = new AuthRequestModel("AndfiOxa","qov12345");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/authenticate").bodyJson(Json.toJson(authRequest));
        final Result result = Helpers.route(app,request);
        assertEquals(ok().status(),result.status());
    }

    @Test
    public void testLoginWith(){
        AuthRequestModel authRequest = new AuthRequestModel("","");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/authenticate").bodyJson(Json.toJson(authRequest));
        final Result result = Helpers.route(app,request);
        assertEquals(badRequest().status(),result.status());
    }
}
