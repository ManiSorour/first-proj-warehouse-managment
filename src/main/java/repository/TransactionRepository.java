package repository;

import model.transaction.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    List<Transaction> findAll();

    void save( Transaction transaction);







}
