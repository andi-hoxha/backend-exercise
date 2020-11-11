package services;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import exceptions.NotFoundException;
import mongo.IMongoDB;
import org.bson.BsonValue;
import org.bson.types.ObjectId;
import play.Logger;
import repositories.BaseRepository;


import java.util.ArrayList;
import java.util.List;

public class BaseService<T> implements BaseRepository<T> {

    @Inject
    IMongoDB mongoDB;


    @Override
    public List<T> getAll(String collectionName, Class<T> tClass) {
        return getCollection(collectionName,tClass).find().into(new ArrayList<>());
    }

    @Override
    public T save(T t, String collectionName, Class<T> tClass) {
        if(t == null){
            throw new IllegalArgumentException("Object cannot be null");
        }
        if(Strings.isNullOrEmpty(collectionName)){
            throw new IllegalArgumentException("Collection Name cannot be empty or null");
        }
        BsonValue id = getCollection(collectionName,tClass).insertOne(t).getInsertedId();
        return getCollection(collectionName,tClass).find(Filters.eq("_id",id)).first();
    }

    @Override
    public T update(T t, String id, String collectionName, Class<T> tClass) {
        T updated = null;
        try{
            getCollection(collectionName,tClass).replaceOne(Filters.eq("_id",new ObjectId(id)),t);
            updated = getCollection(collectionName,tClass).findOneAndReplace(Filters.eq("_id",new ObjectId(id)),t);
            if(updated == null){
                throw new NotFoundException("No records has been found or updated!");
            }
        }catch (NotFoundException e){
            Logger.of(this.getClass()).debug("EXCEPTION CAUGHT ===> " + e.getMessage());
        }
        return updated;
    }

    @Override
    public T delete(String id, String collectionName, Class<T> tClass) {
        T deletedRecord = null;
        try{
            if(!ObjectId.isValid(id)){
                throw new IllegalArgumentException("Invalid Object Id found ---> ID : " + id);
            }
           deletedRecord = getCollection(collectionName,tClass).findOneAndDelete(Filters.eq("_id",new ObjectId(id)));
           if(deletedRecord == null){
               throw new NotFoundException("Record has not been found or deleted");
           }
        }catch (IllegalArgumentException | NotFoundException e){
            Logger.of(this.getClass()).debug("EXCEPTIONS CAUGHT ======> " + e.getMessage());
        }
        return deletedRecord;
    }

    @Override
    public T findById(String id, String collectionName, Class<T> tClass){
        T found = null;
        try {
            if (Strings.isNullOrEmpty(id) || Strings.isNullOrEmpty(collectionName)) {
                throw new IllegalArgumentException("Id or Collection Name cannot be empty!");
            }
            if (!ObjectId.isValid(id)) {
                throw new IllegalArgumentException("Invalid Object Id");
            }
             found = getCollection(collectionName, tClass).find(Filters.eq("_id", new ObjectId(id))).first();
            if (found == null) {
                throw new NotFoundException("Cannot find any record having this ID: " + id);
            }
        }catch (IllegalArgumentException | NotFoundException e){
            Logger.of(this.getClass()).debug("EXCEPTION CAUGHT ====> " + e.getMessage());
        }
        return found;
    }

    private MongoCollection<T> getCollection(String collectionName,Class<T> tClass){
        return mongoDB.getMongoDatabase().getCollection(collectionName,tClass);
    }

}
