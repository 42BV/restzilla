/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.rest.model;

import javax.persistence.Entity;

import nl.mad.rest.EnableRest;
import nl.mad.rest.model.dto.CreateUserModel;
import nl.mad.rest.model.dto.UpdateUserModel;
import nl.mad.rest.model.dto.UserModel;

/**
 * Represents a user.
 *
 * @since Mar 11, 2015
 */
@Entity
@EnableRest(resultType = UserModel.class,
            createType = CreateUserModel.class,
            updateType = UpdateUserModel.class)
public class User extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
