package repositories;

import exceptions.NotFoundException;

import java.util.List;

public interface BaseRepository<T> {
    List<T> getAll(String collectionName,Class<T> tClass);
    T save (T t,String collectionName,Class<T> tClass);
    T update(T t,String id,String collectionName,Class<T> tClass);
    T delete (String id,String collectionName,Class<T> tClass);
    T findById(String id,String collectionName,Class<T> tClass) throws NotFoundException;
}
