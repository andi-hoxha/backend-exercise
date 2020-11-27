package modules;

import akka.Done;
import akka.actor.CoordinatedShutdown;
import net.sf.ehcache.CacheManager;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Singleton
public class StartupProvider {

    @Inject
    public StartupProvider(CoordinatedShutdown coordinatedShutdown) {
        coordinatedShutdown.addTask(CoordinatedShutdown.PhaseBeforeActorSystemTerminate(), "shutting-down-cache-manager", () -> {
            Logger.of(this.getClass()).debug("Shutting down cache manager!");
            CacheManager.getInstance().shutdown();
            return CompletableFuture.completedFuture(Done.done());
        });
    }
}
