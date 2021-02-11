package utils;

import constants.CollectionNames;
import models.BaseModel;
import models.ChatRoom;
import models.Dashboard;
import models.User;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import services.ChatRoomService;
import services.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;


public class ServiceUtil {

    public static User getUser(UserService service, String accessToken){
        Bson filters = eq("accessToken",accessToken);
        return service.findOne(CollectionNames.USER,filters,User.class);
    }

    public static boolean hasAccess(ChatRoomService service, String roomId, User user){
        Bson filters = and(eq("_id",new ObjectId(roomId)),
                        in("writeACL",user.getId().toHexString()));
        ChatRoom chatRoom = service.findOne(CollectionNames.CHANNEL,filters,ChatRoom.class);
        return chatRoom != null;
    }

    public static List<String> userRoles(User user){
        List<String> roles =  user.getRoles().stream().map(BaseModel::getId).map(ObjectId::toHexString).collect(Collectors.toList());
        roles.add(user.getId().toHexString());
        return roles;
    }
    public static CompletableFuture<List<Dashboard>> hierarchy(List<Dashboard> input) {
        return CompletableFuture.supplyAsync(()-> {
            if(input == null){
                return new ArrayList<>();
            }
            return input.stream()
                    .filter(x -> x.getParentId() == null)
                    .map(next -> hierarchy(next, input))
                    .collect(Collectors.toList());
        });
    }

    public static Dashboard hierarchy(Dashboard dashboard, List<Dashboard> list) {
        List<Dashboard> children = list.stream()
                .filter(el -> dashboard.getId().equals(el.getParentId()))
                .map(next -> hierarchy(next, list))
                .collect(Collectors.toList());
        dashboard.setChildren(children);
        return dashboard;
    }
}
