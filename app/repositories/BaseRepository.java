package repositories;

import exceptions.NotFoundException;
import org.bson.conversions.Bson;

import java.util.List;

public interface BaseRepository<T> {
    List<T> getAll(String collectionName,Class<T> tClass);
    T save (T t,String collectionName,Class<T> tClass);
    T update(T t,String id,String collectionName,Class<T> tClass);
    T delete (String id,String collectionName,Class<T> tClass);
    T findById(String id,String collectionName,Class<T> tClass);
    void saveAll(List<T> items,String collectionName,Class<T> tClass);
    T findOne(String collectionName, Bson filters, Class<T> tClass);
    List<T> findMany(String collectionName,Bson filters,Class<T> tClass);
}
