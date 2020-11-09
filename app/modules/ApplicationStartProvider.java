package modules;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ApplicationStartProvider {

    @Inject
    public ApplicationStartProvider(Config config){
        String mode = config.getString("mode");
        Logger.of(this.getClass()).debug("RUNNING IN MODE -----> " + mode.toUpperCase());
    }
}
