/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.model;

import nl._42.restzilla.RestResource;

import javax.persistence.Entity;

@Entity
@RestResource(readOnly = true)
public class WithProxyService extends BaseEntity {
    
    private int age;
    
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
