import models.content.BaseContent;
import models.content.EmailContent;
import models.content.ImageContent;
import models.content.TextContent;
import models.requests.AuthRequestModel;
import mongo.InMemoryMongoDB;
import org.bson.types.ObjectId;
import org.junit.After;
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

    @After
    @Override
    public void stopPlay(){
        if(app !=null){
            Helpers.stop(app);
            app = null;
        }
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

    @Test
    public void updateNonExistingContent(){
        BaseContent baseContent = new ImageContent("https://imgs.6sqft.com/wp-content/uploads/2020/06/26105451/NYC-sunset-Lower-Manhattan.jpg");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("PUT").uri("/api/dashboard/5fc3c3698136fa7ded94943a/content/someId").header("Authorization","Bearer " + accessToken).bodyJson(Json.toJson(baseContent));
        final Result result = Helpers.route(app,request);
        assertEquals(badRequest().status(),result.status());
    }

    @Test
    public void updateNonAuthorizedContent(){
        BaseContent baseContent = new ImageContent("https://imgs.6sqft.com/wp-content/uploads/2020/06/26105451/NYC-sunset-Lower-Manhattan.jpg");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("PUT").uri("/api/dashboard/5fc3c3698136fa7ded94943a/content/5fc4b619ca2f150e05a9fdf6").header("Authorization","Bearer " + unatuhorizedUserToken).bodyJson(Json.toJson(baseContent));
        final Result result = Helpers.route(app,request);
        assertEquals(unauthorized().status(),result.status());
    }

    @Test
    public void updateContent(){
        BaseContent baseContent = new ImageContent("https://imgs.6sqft.com/wp-content/uploads/2020/06/26105451/NYC-sunset-Lower-Manhattan.jpg");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("PUT").uri("/api/dashboard/5fc3c3698136fa7ded94943a/content/5fc4bf01c7715923896908b1").header("Authorization","Bearer " + accessToken).bodyJson(Json.toJson(baseContent));
        final Result result = Helpers.route(app,request);
        assertEquals(ok().status(),result.status());
    }

    @Test
    public void createContent2(){
        BaseContent baseContent = new EmailContent("Hi there","Test info","andih576@gmail.com");
        baseContent.setId(new ObjectId("5fc4bf52d7c8332499e84c2a"));
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/dashboard/5fc3c3698136fa7ded94943a/content").header("Authorization","Bearer " + accessToken).bodyJson(Json.toJson(baseContent));
        final Result result = Helpers.route(app,request);
        assertEquals(ok().status(),result.status());
    }

    @Test
    public void deleteContent(){
        final Http.RequestBuilder request = new Http.RequestBuilder().method("DELETE").uri("/api/dashboard/5fc3c3698136fa7ded94943a/content/5fc4bf52d7c8332499e84c2a").header("Authorization","Bearer " + accessToken);
        final Result result = Helpers.route(app,request);
        assertEquals(ok().status(),result.status());
    }
}
