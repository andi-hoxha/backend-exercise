import models.ChatRoom;
import models.requests.AuthRequestModel;
import mongo.InMemoryMongoDB;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import types.ChannelType;
import java.util.Collections;
import static org.junit.Assert.*;
import static play.mvc.Results.*;

public class ChatController extends WithApplication {

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
    public void crateChannel(){
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName("Prime BI");
        chatRoom.setChannelType(ChannelType.PUBLIC);
        chatRoom.setGroupAdmin(new ObjectId("5fb660761565bb4366788884"));
        chatRoom.setGroupMembers(Collections.emptyList());
        final Http.RequestBuilder request = new Http.RequestBuilder().method("POST").uri("/api/channel").header("Authorization","Bearer " + accessToken).bodyJson(Json.toJson(chatRoom));
        final Result result = Helpers.route(app,request);
        assertEquals(ok().status(),result.status());
    }

}
