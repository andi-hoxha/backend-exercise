package modules;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import models.User;
import mongo.IMongoDB;

@Singleton
public class MongoDbProvider {

    @Inject
    public MongoDbProvider(IMongoDB mongoDB) {
        IndexOptions indexOptions = new IndexOptions().unique(true);
        mongoDB.getMongoDatabase().getCollection("User", User.class).createIndex(Indexes.ascending("username","email"),indexOptions);
    }
}
