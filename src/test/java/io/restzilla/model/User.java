/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.model;

import io.restzilla.RestResource;
import io.restzilla.RestConfig;
import io.restzilla.model.dto.CreateUserModel;
import io.restzilla.model.dto.UpdateUserModel;
import io.restzilla.model.dto.UserModel;

import javax.persistence.Entity;

/**
 * Represents a user.
 *
 * @since Mar 11, 2015
 */
@Entity
@RestResource(
    resultType = UserModel.class,
    create = @RestConfig(inputType = CreateUserModel.class, resultType = Long.class),
    update = @RestConfig(inputType = UpdateUserModel.class),
    delete = @RestConfig(resultType = Void.class)
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
