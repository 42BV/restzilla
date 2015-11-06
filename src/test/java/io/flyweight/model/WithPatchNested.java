/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight.model;

import javax.persistence.Embeddable;

@Embeddable
public class WithPatchNested {
    
    private String nestedName;
    
    private String nestedOther;

    public String getNestedName() {
        return nestedName;
    }

    public void setNestedName(String nestedName) {
        this.nestedName = nestedName;
    }

    public String getNestedOther() {
        return nestedOther;
    }
    
    public void setNestedOther(String nestedOther) {
        this.nestedOther = nestedOther;
    }

}
