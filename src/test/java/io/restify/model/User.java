/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restify.model;

import io.restify.CrudConfig;
import io.restify.EnableRest;
import io.restify.model.dto.CreateUserModel;
import io.restify.model.dto.UpdateUserModel;
import io.restify.model.dto.UserModel;

import javax.persistence.Entity;

/**
 * Represents a user.
 *
 * @since Mar 11, 2015
 */
@Entity
@EnableRest(
    resultType = UserModel.class,
        create = @CrudConfig(inputType = CreateUserModel.class, resultType = Long.class, roles = "ADMIN"),
        update = @CrudConfig(inputType = UpdateUserModel.class),
        delete = @CrudConfig(resultType = Void.class)
)
public class User extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
