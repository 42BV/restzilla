package nl.mad.rest.model;

import javax.persistence.Entity;

import nl.mad.rest.EnableRest;

@Entity
@EnableRest
public class WithRepository extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

}
