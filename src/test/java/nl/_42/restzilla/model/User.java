/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.model;

import nl._42.restzilla.RestResource;
import nl._42.restzilla.RestConfig;
import nl._42.restzilla.model.dto.CreateUserModel;
import nl._42.restzilla.model.dto.UpdateUserModel;
import nl._42.restzilla.model.dto.UserModel;

import jakarta.persistence.Entity;

/**
 * Represents a user.
 *
 * @since Mar 11, 2015
 */
@Entity
@RestResource(
    inputType = CreateUserModel.class,
    resultType = UserModel.class,
    create = @RestConfig(resultType = Long.class),
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
