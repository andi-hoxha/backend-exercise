import lombok.Data;
import models.Dashboard;
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

@Data
public class DashboardController extends WithApplication {

    InMemoryMongoDB mongoDB;
    private String accessToken;

    @Before
    @Override
    public void startPlay() {
        super.startPlay();
        mongoDB = app.injector().instanceOf(InMemoryMongoDB.class);
        AuthRequestModel login = new AuthRequestModel("AndfiOxa","qov12345");
        Result result = Helper.authenticate(app,login);
        this.accessToken = Helper.getAccessToken(result);
    }

    @Test
    public void CreateDashboardTestWithoutAuthorization(){
        Dashboard dashboard = new Dashboard();
        dashboard.setName("NYC");
        dashboard.setDescription("The Hamptons rich area");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/dashboard").header("Authorization"," " + accessToken).bodyJson(Json.toJson(dashboard));
        final Result result = Helpers.route(app,request);
        assertEquals(unauthorized().status(),result.status());
    }
    @Test
    public void CreateDashboardTestWithAuthorization(){
        Dashboard dashboard = new Dashboard();
        dashboard.setName("Prishtina");
        dashboard.setDescription("City that never sleeps");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/dashboard").header("Authorization","Bearer " + accessToken).bodyJson(Json.toJson(dashboard));
        final Result result = Helpers.route(app,request);
        assertEquals(ok().status(),result.status());
    }

    @Test
    public void getAllDashboards(){
        final Http.RequestBuilder request = new Http.RequestBuilder().method("GET").uri("/api/dashboard").header("Authorization","Bearer " + accessToken);
        final Result result = Helpers.route(app,request);
        assertEquals(ok().status(),result.status());
    }

    @Test
    public void updateDashboardWithoutWriteACL(){
        Dashboard dashboard = new Dashboard();
        dashboard.setName("Test");
        dashboard.setDescription("Fake");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("PUT").uri("/api/dashboard/5fc11dc802eeb60c76f0564d").header("Authorization","Bearer ").bodyJson(Json.toJson(dashboard));
        final Result result = Helpers.route(app,request);
        assertEquals(unauthorized().status(),result.status());
    }
}
