package executors;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

public class MongoExecutionContext extends CustomExecutionContext {

    public MongoExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "mongo-executor");
    }
}
