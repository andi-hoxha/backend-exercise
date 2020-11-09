package mongo;

import akka.Done;
import akka.actor.CoordinatedShutdown;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import play.Logger;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public abstract class MongoDriver implements IMongoDB {

    protected final Config config;
    protected MongoClient client;
    private MongoDatabase database;

    protected MongoDriver(Config config, CoordinatedShutdown coordinatedShutdown) {
        this.config = config;

        coordinatedShutdown.addTask(CoordinatedShutdown.PhaseServiceStop(),"shutting-down-mongo-connections",()-> {
            Logger.of(this.getClass()).debug("Shutting down mongo connections");
            close();
            return CompletableFuture.completedFuture(Done.done());
        });
    }

    public synchronized MongoDatabase getMongoDatabase(){
        if(database == null){
            database = this.connect();
        }

        CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
                .conventions(Collections.singletonList(Conventions.ANNOTATION_CONVENTION))
                .register("models")
                .automatic(true)
                .build();

        final CodecRegistry customEnumCodecs = CodecRegistries.fromCodecs();
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                customEnumCodecs,
                CodecRegistries.fromProviders(pojoCodecProvider)
        );
        return database.withCodecRegistry(pojoCodecRegistry);
    }

    protected abstract MongoDatabase connect();

    protected abstract void disconnect();

    public MongoClient getMongoClient(){ return client;}

    private void close(){
        if(database !=null){
            database = null;
        }
        disconnect();
    }
}
