import lombok.Data;
import models.Dashboard;
import models.requests.AuthRequestModel;
import mongo.InMemoryMongoDB;
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

@Data
public class DashboardController extends WithApplication {

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
        dashboard.setName("Fake Dashboard");
        dashboard.setDescription("Fake");
        final Http.RequestBuilder request = new Http.RequestBuilder().method("PUT").uri("/api/dashboard/5fc3c3698136fa7ded94943a").header("Authorization","Bearer " + unatuhorizedUserToken).bodyJson(Json.toJson(dashboard));
        final Result result = Helpers.route(app,request);
        assertEquals(unauthorized().status(),result.status());
    }

    @Test
    public void deleteDashboardWithBadRequest(){
        final Http.RequestBuilder request = new Http.RequestBuilder().method("DELETE").uri("/api/dashboard/someId").header("Authorization","Bearer " + accessToken);
        final Result result = Helpers.route(app,request);
        assertEquals(badRequest().status(),result.status());
    }

    @Test
    public void deleteDashboard(){
        final Http.RequestBuilder request = new Http.RequestBuilder().method("DELETE").uri("/api/dashboard/5fc414feea83860922998d6c").header("Authorization","Bearer " + accessToken);
        final Result result = Helpers.route(app,request);
        assertEquals(ok().status(),result.status());
    }
}
