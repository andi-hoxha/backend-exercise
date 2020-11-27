package controllers;

import actions.Authorize;
import actions.Authorized;
import actors.ChatActor;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import constants.CollectionNames;
import models.ChatRoom;
import models.User;
import play.libs.F;
import play.libs.streams.ActorFlow;
import play.mvc.*;
import services.ChatRoomService;
import services.SerializationService;
import services.UserService;
import utils.DatabaseUtil;
import utils.ServiceUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Singleton
public class ChatController extends Controller {

    @Inject
    private ActorSystem actorSystem;
    @Inject
    private Materializer materializer;
    @Inject
    UserService userService;
    @Inject
    private ChatRoomService chatRoomService;
    @Inject
    private SerializationService serializationService;

    public WebSocket chat(String roomId,String token){
        User user = ServiceUtil.getUser(userService,token);
        return WebSocket.Text.acceptOrResult((req) -> {
           if(user == null){
               return CompletableFuture.completedFuture(F.Either.Left(forbidden("You are not logged in")));
           }
            if(chatRoomService.findById(roomId, CollectionNames.CHANNEL,ChatRoom.class) == null){
                return CompletableFuture.completedFuture(F.Either.Left(forbidden("Cannot find any channel with this id : " + roomId)));
            }
           if(!ServiceUtil.hasAccess(chatRoomService,roomId,user)){
               return CompletableFuture.completedFuture(F.Either.Left(forbidden("You don't have access in this room")));
           }
          return CompletableFuture.completedFuture(
                  F.Either.Right(ActorFlow.actorRef(out -> ChatActor.props(out,roomId,user),actorSystem,materializer))
          );
       });
    }

    @Authorized
    public CompletableFuture<Result> myChannels(Http.Request request){
        User user = request.attrs().get(Authorize.Attrs.USER);
        return chatRoomService.allChannels(user)
                .thenCompose(result -> serializationService.toJsonNode(result))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtil::throwableToResult);
    }

    @Authorized
    public CompletableFuture<Result> createChannel(Http.Request request){
        User user = request.attrs().get(Authorize.Attrs.USER);
        return serializationService.parseBodyOfType(request, ChatRoom.class)
                .thenCompose(channel -> chatRoomService.createChannel(user,channel))
                .thenCompose(result -> serializationService.toJsonNode(result))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtil::throwableToResult);
    }

    @Authorized
    public CompletableFuture<Result> updateChannel(Http.Request request,String roomId){
        User user = request.attrs().get(Authorize.Attrs.USER);
        return serializationService.parseBodyOfType(request,ChatRoom.class)
                .thenCompose(channel -> chatRoomService.updateChannel(user,roomId,channel))
                .thenCompose(result -> serializationService.toJsonNode(result))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtil::throwableToResult);
    }

    @Authorized
    public CompletableFuture<Result> deleteChannel(Http.Request request,String roomId){
        User user = request.attrs().get(Authorize.Attrs.USER);
        return chatRoomService.deleteChannel(user,roomId)
                .thenCompose(result -> serializationService.toJsonNode(result))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtil::throwableToResult);
    }

}
