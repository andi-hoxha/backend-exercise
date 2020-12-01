package services;

import actions.Authorized;
import constants.CollectionNames;
import exceptions.RequestException;
import executors.MongoExecutionContext;
import models.ChatRoom;
import models.User;
import org.bson.conversions.Bson;
import play.mvc.Http;
import types.ChannelType;
import types.UserACL;
import utils.AccessibilityUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.mongodb.client.model.Filters.*;

@Authorized
public class ChatRoomService extends BaseService<ChatRoom> {

    @Inject
    AccessibilityUtil accessibilityUtil;

    @Inject
    MongoExecutionContext ec;

    public CompletableFuture<List<ChatRoom>> allChannels(User user){
        return CompletableFuture.supplyAsync(() -> {
            try {
                if(user == null){
                    throw new RequestException(Http.Status.BAD_REQUEST,"User cannot be null");
                }
                Bson filter = or(
                        (and(eq("channelType", "PUBLIC"),size("writeACL",0))),
                        (and(eq("channelType","PRIVATE"),in("groupMembers",user.getId())))
                );
                return findMany(CollectionNames.CHANNEL,filter,ChatRoom.class);
            }catch (RequestException e){
                throw new CompletionException(e);
            }
        },ec.current());
    }

    public CompletableFuture<ChatRoom> createChannel(User user,ChatRoom chatRoom){
        return CompletableFuture.supplyAsync(() -> {
            try {
                if(user == null || chatRoom == null){
                    throw new RequestException(Http.Status.BAD_REQUEST,"User cannot be null");
                }
                chatRoom.setGroupAdmin(user.getId());
                if (chatRoom.getChannelType().name().equals(ChannelType.PRIVATE.name())) {
                    List<String> writeACL = new ArrayList<>();
                    writeACL.add(user.getId().toHexString());
                    writeACL.addAll(chatRoom.getGroupMembers());
                    chatRoom.setWriteACL(writeACL);
                }
                return save(chatRoom, CollectionNames.CHANNEL, ChatRoom.class);
            }catch (RequestException e){
                throw new CompletionException(e);
            }catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
            }
        },ec.current());
    }

    public CompletableFuture<ChatRoom> updateChannel(User user,String roomId,ChatRoom chatRoom){
        return CompletableFuture.supplyAsync(()->{
            try {
                if (!accessibilityUtil.withACL(user, roomId, CollectionNames.CHANNEL, ChatRoom.class, UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED,"You are not authorized to edit this channel");
                }
                return update(chatRoom,roomId,CollectionNames.CHANNEL,ChatRoom.class);
            }catch (RequestException e){
                throw new CompletionException(e);
            }catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
            }
        },ec.current());
    }

    public CompletableFuture<ChatRoom> deleteChannel (User user,String roomId){
        return CompletableFuture.supplyAsync(() ->{
            try{
                if(!accessibilityUtil.isGroupAdmin(user,roomId)){
                    throw new RequestException(Http.Status.UNAUTHORIZED,"You are not group admin.");
                }
                return delete(roomId, CollectionNames.CHANNEL,ChatRoom.class);
            }catch (RequestException e){
                throw new CompletionException(e);
            }catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
            }
        },ec.current());
    }

    public CompletableFuture<ChatRoom> findById(User user,String roomId){
       return CompletableFuture.supplyAsync(() ->{
           try{
               if(!accessibilityUtil.withACL(user,roomId, CollectionNames.CHANNEL,ChatRoom.class,UserACL.WRITE)){
                   throw new RequestException(Http.Status.UNAUTHORIZED,"There is no channel with this Id or it's a private group in which you don't have access");
               }
               return findById(roomId,CollectionNames.CHANNEL,ChatRoom.class);
           }catch (RequestException e){
               throw new CompletionException(e);
           }catch (Exception e){
               throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
           }
       },ec.current());
    }

    public CompletableFuture<Boolean> leaveTheChannel(User user,String roomId){
        return findById(user,roomId).thenCompose(channel -> CompletableFuture.completedFuture(channel.getGroupMembers().remove(user.getId().toHexString())));
    }

    public CompletableFuture<Boolean> inviteUsers(List<String> list, String roomId, User user){
        return findById(user,roomId)
                .thenCompose(channel -> CompletableFuture.supplyAsync(() -> {
                    if(channel == null){
                        try {
                            throw new RequestException(Http.Status.BAD_REQUEST,"There is no channel with this id: " + roomId);
                        } catch (RequestException e) {
                            throw new CompletionException(e);
                        }
                    }
                    channel.getGroupMembers().addAll(list);
                    channel.getWriteACL().addAll(list);
                    return channel;
                }).thenCompose(chatRoom -> updateChannel(user,roomId,chatRoom)))
                   .thenCompose(result -> CompletableFuture.completedFuture(result != null));
    }

    private List<ChatRoom> publicChannels(){
        return getCollection(CollectionNames.CHANNEL, ChatRoom.class)
                .find(and(eq("channelType", "PUBLIC"),size("writeACL",0)))
                .into(new ArrayList<>());
    }

    private List<ChatRoom> privateChannels(User user){
        return getCollection(CollectionNames.CHANNEL,ChatRoom.class)
                .find(and(eq("channelType","PRIVATE"),in("groupMembers",user.getId())))
                .into(new ArrayList<>());
    }
}
