/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.service.impl;

import io.restzilla.service.Lazy;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Persistable;
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
public final class TransactionalCrudService<T extends Persistable<ID>, ID extends Serializable> extends DefaultCrudService<T, ID> {
    
    /**
     * Template that handles transactions.
     */
    private TransactionTemplate transactionTemplate;

    /**
     * Create a new transactional CRUD service.
     */
    public TransactionalCrudService() {
        super();
    }
    
    /**
     * Create a new transactional CRUD service.
     * 
     * @param repository the repository
     * @param entityClass the entity class
     */
    public TransactionalCrudService(PagingAndSortingRepository<T, ID> repository, Class<T> entityClass) {
        super(entityClass, repository);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends T> S save(S entity) {
        return transactionTemplate.execute(new SaveCallback<S>(entity));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends T> S save(Lazy<S> entity) {
        return transactionTemplate.execute(new SaveLazyCallback<S>(entity));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(ID id) {
        transactionTemplate.execute(new DeleteByIdCallback(id));
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
    
    private class SaveCallback<S extends T> implements TransactionCallback<S> {
        
        private final S entity;
        
        public SaveCallback(S entity) {
            this.entity = entity;
        }
        
        @Override
        public S doInTransaction(TransactionStatus status) {
            return TransactionalCrudService.super.save(entity);
        }
        
    }
    
    private class SaveLazyCallback<S extends T> implements TransactionCallback<S> {
        
        private final Lazy<S> entity;
        
        public SaveLazyCallback(Lazy<S> entity) {
            this.entity = entity;
        }
        
        @Override
        public S doInTransaction(TransactionStatus status) {
            return TransactionalCrudService.super.save(entity);
        }
        
    }

    private class DeleteByIdCallback implements TransactionCallback<Void> {
        
        private final ID id;
        
        public DeleteByIdCallback(ID id) {
            this.id = id;
        }
        
        @Override
        public Void doInTransaction(TransactionStatus status) {
            TransactionalCrudService.super.delete(id);
            return null;
        }
        
    }

}
