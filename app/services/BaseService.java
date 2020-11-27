package services;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import mongo.IMongoDB;
import org.bson.BsonValue;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import repositories.BaseRepository;


import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

public class BaseService<T> implements BaseRepository<T> {

    @Inject
    IMongoDB mongoDB;


    @Override
    public List<T> getAll(String collectionName, Class<T> tClass) {
        return getCollection(collectionName,tClass).find().into(new ArrayList<>());
    }

    @Override
    public T save(T t, String collectionName, Class<T> tClass) {
            BsonValue id = getCollection(collectionName, tClass).insertOne(t).getInsertedId();
            return getCollection(collectionName, tClass).find(eq("_id", id)).first();
    }

    @Override
    public T update(T t, String id, String collectionName, Class<T> tClass) {
            FindOneAndReplaceOptions option = new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER);
            T updated = getCollection(collectionName,tClass).findOneAndReplace(eq("_id",new ObjectId(id)),t,option);
            return updated;
    }

    @Override
    public T delete(String id, String collectionName, Class<T> tClass) {
           T deletedRecord = getCollection(collectionName,tClass).findOneAndDelete(eq("_id",new ObjectId(id)));
           return deletedRecord;
    }

    @Override
    public T findById(String id, String collectionName, Class<T> tClass){
            T found = getCollection(collectionName, tClass).find(eq("_id", new ObjectId(id))).first();
            return found;
    }

    @Override
    public void saveAll(List<T> items, String collectionName, Class<T> tClass) {
        getCollection(collectionName,tClass).insertMany(items);
    }

    public MongoCollection<T> getCollection(String collectionName,Class<T> tClass){
        return mongoDB.getMongoDatabase().getCollection(collectionName,tClass);
    }

    @Override
    public T findOne(String collectionName, Bson filters,Class<T> objectClass){
        return getCollection(collectionName,objectClass).find(filters).first();
    }
    @Override
    public List<T> findMany(String collectionName,Bson filters, Class<T> objectClass){
        return getCollection(collectionName,objectClass).find(filters).into(new ArrayList<>());
    }

}
