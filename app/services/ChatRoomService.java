package services;

import actions.Authorized;
import exceptions.RequestException;
import models.ChatRoom;
import models.User;
import org.bson.types.ObjectId;
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

    public CompletableFuture<List<ChatRoom>> allChannels(User user){
        return CompletableFuture.supplyAsync(() -> {
            List<ChatRoom> publicChannels = publicChannels();
            List<ChatRoom> privateChannels = privateChannels(user);
            publicChannels.addAll(privateChannels);
            return publicChannels;
        });
    }

    public CompletableFuture<ChatRoom> createChannel(User user,ChatRoom chatRoom){
        return CompletableFuture.supplyAsync(() -> {
                chatRoom.setGroupAdmin(user.getId());
                if(chatRoom.getChannelType().name().equals("PRIVATE")){
                  List<String> writeACL = new ArrayList<>();
                  writeACL.add(user.getId().toHexString());
                  writeACL.addAll(chatRoom.getGroupMembers());
                  chatRoom.setWriteACL(writeACL);
                }
                return save(chatRoom, "Channels", ChatRoom.class);
        });
    }

    public CompletableFuture<ChatRoom> updateChannel(User user,String roomId,ChatRoom chatRoom){
        return CompletableFuture.supplyAsync(()->{
            try {
                if (!accessibilityUtil.withACL(user, roomId, "Channels", ChatRoom.class, UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED,"You are not authorized to edit this channel");
                }
                return update(chatRoom,roomId,"Channels",ChatRoom.class);
            }catch (RequestException e){
                throw new CompletionException(e);
            }catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
            }
        });
    }

    public CompletableFuture<ChatRoom> deleteChannel (User user,String roomId){
        return CompletableFuture.supplyAsync(() ->{
            try{
                if(!accessibilityUtil.isGroupAdmin(user,roomId)){
                    throw new RequestException(Http.Status.UNAUTHORIZED,"You are not group admin.");
                }
                return delete(roomId,"Channels",ChatRoom.class);
            }catch (RequestException e){
                throw new CompletionException(e);
            }catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
            }
        });
    }

    public CompletableFuture<ChatRoom> findById(User user,String roomId){
       return CompletableFuture.supplyAsync(() ->{
           try{
               if(!accessibilityUtil.withACL(user,roomId,"Channels",ChatRoom.class,UserACL.WRITE)){
                   throw new RequestException(Http.Status.UNAUTHORIZED,"There is no grupchat with this Id or it's a private group in which you don't have accesss");
               }
               return findById(roomId,"Channels",ChatRoom.class);
           }catch (RequestException e){
               throw new CompletionException(e);
           }catch (Exception e){
               throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
           }
       });
    }

    public CompletableFuture<Void> leaveTheChannel(User user,String roomId){
        return CompletableFuture.supplyAsync(() ->{
            ChatRoom chatRoom = findById(user,roomId).join();
            chatRoom.getGroupMembers().remove(user);
            return null;
        });
    }

    public CompletableFuture<Void> inviteUsers(List<String> list, String roomId, User user){
        return CompletableFuture.supplyAsync(() -> {
            try {
                ChatRoom chatRoom = findById(user, roomId).join();
                if (chatRoom == null) {
                    throw new RequestException(Http.Status.BAD_REQUEST, "There is no channel with this id : " + roomId);
                }
                chatRoom.getGroupMembers().addAll(list);
                chatRoom.getWriteACL().addAll(list);
                return null;
            }catch (RequestException e){
                throw new CompletionException(e);
            }catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
            }
        });
    }

    private List<ChatRoom> publicChannels(){
        return getCollection("Channels", ChatRoom.class)
                .find(and(eq("channelType", "PUBLIC"),size("writeACL",0)))
                .into(new ArrayList<>());
    }

    private List<ChatRoom> privateChannels(User user){
        return getCollection("Channels",ChatRoom.class)
                .find(and(eq("channelType","PRIVATE"),in("groupMembers",user.getId())))
                .into(new ArrayList<>());
    }
}
