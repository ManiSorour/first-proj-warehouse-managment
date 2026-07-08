package database.configuration;

import java.util.List;

public interface Dao <T>{

    T findById(int id );

    List<T> findAll();

    void save(T entity);

    void update(T entity);

    void delete(int id);




}
