/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.rest.service;

import nl.mad.rest.model.Product;
import nl.mad.rest.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService extends AbstractCrudService<Product, Long> {
    
    @Autowired
    public ProductService(ProductRepository productRepository) {
        super(Product.class, productRepository);
    }
    
    @Override
    public <S extends Product> S save(S product) {
        product.setName(product.getName() + "!");
        return super.save(product);
    }

}
