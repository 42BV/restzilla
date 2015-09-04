/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.service;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Default implementation of the CrudService, performs transactions by template.
 *
 * @author Jeroen van Schagen
 * @since Aug 22, 2015
 */
public class TransactionalCrudService<T, ID extends Serializable> extends AbstractCrudService<T, ID> {
    
    /**
     * Template that handles transactions.
     */
    private TransactionTemplate transactionTemplate;

    /**
     * Create a new transactional CRUD service.
     * 
     * @param repository the repository
     * @param entityClass the entity class
     */
    public TransactionalCrudService(PagingAndSortingRepository<T, ID> repository, Class<T> entityClass) {
        super(repository, entityClass);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends T> S save(final S entity) {
        return transactionTemplate.execute(new TransactionCallback<S>() {
            
            @Override
            public S doInTransaction(TransactionStatus status) {
                return performSave(entity);
            }
            
        });
    }
    
    // Defined in separate method because cannot call super in annonymous inner class
    private <S extends T> S performSave(S entity) {
        return super.save(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final ID id) {
        transactionTemplate.execute(new TransactionCallback<Void>() {
            
            @Override
            public Void doInTransaction(TransactionStatus status) {
                performDelete(id);
                return null;
            }
            
        });
    }
    
    // Defined in separate method because cannot call super in annonymous inner class
    private void performDelete(ID id) {
        super.delete(id);
    }

    /**
     * Retrieve the transaction template
     * 
     * @return the transaction template
     */
    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }
    
    /**
     * Configure the transaction manager.
     * 
     * @param transactionManager the transaction manager
     */
    @Autowired
    public void setPlatformTransactionManager(PlatformTransactionManager transactionManager) {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

}
