import models.content.BaseContent;
import models.content.EmailContent;
import models.content.ImageContent;
import models.content.TextContent;
import models.requests.AuthRequestModel;
import mongo.InMemoryMongoDB;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import static org.junit.Assert.*;
import static play.mvc.Results.*;

public class ContentController extends WithApplication {

    InMemoryMongoDB mongoDB;
    private String accessToken;
    private String unatuhorizedUserToken;

    @Before
    @Override
    public void startPlay() {
        super.startPlay();
        mongoDB = app.injector().instanceOf(InMemoryMongoDB.class);
        AuthRequestModel login = new AuthRequestModel("AndfiOxa","qov12345");
        Result result = Helper.authenticate(app,login);
        this.accessToken = Helper.getAccessToken(result);
        AuthRequestModel userLogin = new AuthRequestModel("BuleronSejdiu","buleroniG12");
        Result authResult = Helper.authenticate(app,userLogin);
        this.unatuhorizedUserToken = Helper.getAccessToken(authResult);
    }

    @Test
    public void createContent(){
        BaseContent baseContent = new EmailContent("Hi there","Test info","andih576@gmail.com");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/dashboard/5fc414feea83860932998d6c/content").header("Authorization","Bearer " + accessToken).bodyJson(Json.toJson(baseContent));
        final Result result = Helpers.route(app,request);
        assertEquals(unauthorized().status(),result.status());
    }

    @Test
    public void createTextContent(){
        BaseContent baseContent = new TextContent("Test");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/dashboard/5fc3c3698136fa7ded94943a/content").header("Authorization","Bearer " + accessToken).bodyJson(Json.toJson(baseContent));
        final Result result = Helpers.route(app,request);
        assertEquals(ok().status(),result.status());
    }

    @Test
    public void createImageContent(){
        BaseContent baseContent = new ImageContent("https://imgs.6sqft.com/wp-content/uploads/2020/06/26105451/NYC-sunset-Lower-Manhattan.jpg");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/dashboard/5fc3c3698136fa7ded94943a/content").header("Authorization","Bearer " + accessToken).bodyJson(Json.toJson(baseContent));
        final Result result = Helpers.route(app,request);
        assertEquals(ok().status(),result.status());
    }

}
