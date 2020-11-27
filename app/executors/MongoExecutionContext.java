package executors;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;

public class MongoExecutionContext extends CustomExecutionContext {

    @Inject
    public MongoExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "mongo-executor");
    }
}
