package mongo;

import akka.actor.CoordinatedShutdown;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.Network;


import javax.inject.Inject;
import java.io.IOException;

public final class InMemoryMongoDB  extends MongoDriver{
    private  static MongodExecutable mongoEx;

    @Inject InMemoryMongoDB(CoordinatedShutdown coordinatedShutdown, Config config){
        super(config,coordinatedShutdown);
    }


    @Override
    protected MongoDatabase connect() {
        IRuntimeConfig builder = new RuntimeConfigBuilder().defaults(Command.MongoD)
                .processOutput(ProcessOutput.getDefaultInstanceSilent())
                .build();
        MongodStarter starter = MongodStarter.getInstance(builder);
        try{
            mongoEx = starter.prepare(new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net("localhost",12345, Network.localhostIsIPv6()))
            .build());
            mongoEx.start();
            client = MongoClients.create("mongodb://localhost:27017");
            return client.getDatabase("test");
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void disconnect() {
        closeMongoClient();
        closeMongoProcess();
    }

    private void closeMongoProcess(){
        if(mongoEx == null){
            return ;
        }
        client.close();
    }

    private void closeMongoClient(){
        if(client == null){
            return;
        }
        client.close();
    }

}
