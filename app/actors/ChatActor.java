package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import models.User;

public class ChatActor extends AbstractActor {

    private static final String PING = "PING";
    private static final String PONG = "PONG";


    /**
     * Mediator
     */
    private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    private String roomId;

    private User user;

    private ActorRef out;

    public static Props props(ActorRef out, String roomId,User user){
        return Props.create(ChatActor.class,()-> new ChatActor(out,roomId,user));
    }

    private ChatActor(ActorRef out,String roomId,User user){
        this.roomId = roomId;
        this.out = out;
        this.user = user;
        mediator.tell(new DistributedPubSubMediator.Subscribe(roomId,getSelf()),getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class,this::onMessageReceived)
                .match(ChatActorProtocol.ChatMessage.class,this::onChatMessageReceived)
                .match(DistributedPubSubMediator.SubscribeAck.class,this::onSubscribe)
                .match(DistributedPubSubMediator.UnsubscribeAck.class,this::onUnsubscribe)
                .build();
    }

    public void onMessageReceived(String message){
        if(message.equals(PING)){
            out.tell(PONG,getSelf());
            return;
        }
        broadcast(message);
    }

    public void onChatMessageReceived(ChatActorProtocol.ChatMessage what){
        if(getSender().equals(getSelf())){
            return;
        }
        String message = what.getMessage();
        out.tell(message,getSelf());
    }

    public void onSubscribe(DistributedPubSubMediator.SubscribeAck message){
        this.joinTheRoom();
    }

    public void onUnsubscribe(DistributedPubSubMediator.UnsubscribeAck message){
        this.leaveTheRoom();
    }

    @Override
    public void postStop() throws Exception {
        this.leaveTheRoom();
    }

    private void leaveTheRoom(){
        this.broadcast(user.getUsername() + " has left the room");
    }

    private void joinTheRoom(){
        this.broadcast(user.getUsername() + " has joined the room");
    }

    private void broadcast(String message){
        mediator.tell(
                new DistributedPubSubMediator.Publish(roomId,new ChatActorProtocol.ChatMessage(user.getUsername() + ": " + message)),getSelf()
        );
    }
}
