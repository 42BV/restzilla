/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.model;

import io.restzilla.RestQuery;
import io.restzilla.RestResource;

import javax.persistence.Entity;

@Entity
@RestResource(readOnly = true, queries = @RestQuery(parameters = "age", method = "findAllByAge"))
public class WithProxyService extends BaseEntity {
    
    private int age;
    
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
