package modules;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.typesafe.config.Config;
import models.User;
import mongo.IMongoDB;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ApplicationStartProvider {


    @Inject
    public ApplicationStartProvider(Config config, IMongoDB mongoDB,ActorSystem system){
        IndexOptions indexOptions = new IndexOptions().unique(true);
        mongoDB.getMongoDatabase().getCollection("User",User.class).createIndex(Indexes.ascending("username","email"),indexOptions);
        String mode = config.getString("mode");
        Logger.of(this.getClass()).debug("RUNNING IN MODE -----> " + mode.toUpperCase());

        AkkaManagement.get(system).start();

        ClusterBootstrap.get(system).start();

        final Cluster cluster = Cluster.get(system);
        cluster.registerOnMemberUp(()->{
            Logger.of(this.getClass()).debug("MEMBER IS UP");
        });
    }
}
