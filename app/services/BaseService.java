package services;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import exceptions.NotFoundException;
import exceptions.RequestException;
import mongo.IMongoDB;
import org.bson.BsonValue;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.mvc.Http;
import repositories.BaseRepository;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

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
        try {
            if (t == null) {
                throw new IllegalArgumentException("Object cannot be null");
            }
            if (Strings.isNullOrEmpty(collectionName) || tClass == null) {
                throw new IllegalArgumentException("Collection Name or Object class cannot be null or empty");
            }
            BsonValue id = getCollection(collectionName, tClass).insertOne(t).getInsertedId();
            return getCollection(collectionName, tClass).find(eq("_id", id)).first();
        }catch (IllegalArgumentException e){
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"Found null"));
        }
    }

    @Override
    public T update(T t, String id, String collectionName, Class<T> tClass) {
        try{
            if(t == null){
                throw new IllegalArgumentException("Object cannot be empty");
            }
            FindOneAndReplaceOptions option = new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER);
            T updated = getCollection(collectionName,tClass).findOneAndReplace(eq("_id",new ObjectId(id)),t,option);
            if(updated == null){
                throw new NotFoundException("No records has been found or updated!");
            }
            return updated;
        }catch (IllegalArgumentException e){
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"Found null"));
        }catch (NotFoundException e){
            throw new CompletionException(new RequestException(Http.Status.NOT_FOUND,"Record not found"));
        }
    }

    @Override
    public T delete(String id, String collectionName, Class<T> tClass) {
        try{
            if(!ObjectId.isValid(id)){
                throw new IllegalArgumentException("Invalid Object Id found ---> ID : " + id);
            }
           T deletedRecord = getCollection(collectionName,tClass).findOneAndDelete(eq("_id",new ObjectId(id)));
           if(deletedRecord == null){
               throw new NotFoundException("Record has not been found or deleted");
           }
           return deletedRecord;
        }catch (IllegalArgumentException e){
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"Found null"));
        }catch (NotFoundException e){
            throw new CompletionException(new RequestException(Http.Status.NOT_FOUND,"Record not found"));
        }
    }

    @Override
    public T findById(String id, String collectionName, Class<T> tClass){

        try {
            if (Strings.isNullOrEmpty(id) || Strings.isNullOrEmpty(collectionName)) {
                throw new IllegalArgumentException("Id or Collection Name cannot be empty!");
            }
            if (!ObjectId.isValid(id)) {
                throw new IllegalArgumentException("Invalid Object Id");
            }
            T found = getCollection(collectionName, tClass).find(eq("_id", new ObjectId(id))).first();
            if (found == null) {
                throw new NotFoundException("Cannot find any record having this ID: " + id);
            }
            return found;
        }catch (IllegalArgumentException e){
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"Found null"));
        }catch (NotFoundException e){
            throw new CompletionException(new RequestException(Http.Status.NOT_FOUND,"Record not found"));
        }

    }

    @Override
    public void saveAll(List<T> items, String collectionName, Class<T> tClass) {
        try{
            if(items == null){
                throw new RequestException(Http.Status.BAD_REQUEST,"Objects that are trying to be stored cannot be empty");
            }
            getCollection(collectionName,tClass).insertMany(items);
        }catch (RequestException e){
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"Found null"));
        }
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
