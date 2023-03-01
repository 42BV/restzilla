/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.restzilla.model;

import nl._42.restzilla.RestResource;

import jakarta.persistence.Entity;

@Entity
@RestResource(basePath = "/mybase/path/")
public class WithNestedBasePath extends BaseEntity {

}
