package mongo;

import akka.actor.CoordinatedShutdown;
import com.google.common.base.Strings;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public final class MongoDB extends MongoDriver {

    @Inject
    public MongoDB(CoordinatedShutdown coordinatedShutdown, Config config){
        super(config, coordinatedShutdown);
    }

    @Override
    protected MongoDatabase connect() {
        String host = config.getString("mongo.host");
        String port = config.getString("mongo.port");
        String DB = config.getString("mongo.database");
        String username = config.getString("mongo.user");
        String password = config.getString("mongo.password");
        String userAuthenticationDatabase = config.getString("mongo.auth_database");

        String[] hosts = new String[0];
        if(!Strings.isNullOrEmpty(host)){
            hosts = host.split(",");
        }
        String[] ports = new String[0];
        if(!Strings.isNullOrEmpty(port)){
            ports = port.split(",");
        }

        List<ServerAddress> addresses = new ArrayList<>();
        for(int i=0;i<hosts.length;i++){
            addresses.add(new ServerAddress(hosts[i],Integer.parseInt(ports[i])));
        }

        MongoClientSettings.Builder builder = MongoClientSettings.builder()
                .applyToClusterSettings(which -> which.hosts(addresses));
        if(!Strings.isNullOrEmpty(username)){
            builder.credential(
                    MongoCredential.createCredential(username,userAuthenticationDatabase,password.toCharArray())
            );
        }

        MongoClientSettings options = builder.build();
        client = MongoClients.create(options);

        return client.getDatabase(DB);
    }

    @Override
    protected void disconnect() {
        if(client == null){
            return;
        }
        client.close();
    }
}
