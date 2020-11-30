package modules;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import models.Dashboard;
import mongo.IMongoDB;

public class MongoDbModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MongoDbProvider.class).asEagerSingleton();
    }
}
