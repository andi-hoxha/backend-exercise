
import models.requests.AuthRequestModel;
import mongo.InMemoryMongoDB;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;


import static org.junit.Assert.assertEquals;
import static play.mvc.Results.*;

public class AuthController extends WithApplication {

    InMemoryMongoDB mongoDB;

    @Before
    @Override
    public void startPlay() {
        super.startPlay();
        mongoDB = app.injector().instanceOf(InMemoryMongoDB.class);
    }

    @Test
    public void loginTest(){
        AuthRequestModel authRequest = new AuthRequestModel("AndfiOxa","qov12345");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/authenticate").bodyJson(Json.toJson(authRequest));
        final Result result = Helpers.route(app,request);
        assertEquals(ok().status(),result.status());
    }

    @Test
    public void unsuccessfulLogin(){
        AuthRequestModel authRequest = new AuthRequestModel("","");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/authenticate").bodyJson(Json.toJson(authRequest));
        final Result result = Helpers.route(app,request);
        assertEquals(badRequest().status(),result.status());
    }

    @Test
    public void unsuccessfulLogin2(){
        AuthRequestModel authRequest = new AuthRequestModel("test","test");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/authenticate").bodyJson(Json.toJson(authRequest));
        final Result result = Helpers.route(app,request);
        assertEquals(notFound().status(),result.status());
    }

}
