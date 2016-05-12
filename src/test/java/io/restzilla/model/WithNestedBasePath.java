/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.model;

import io.restzilla.RestResource;

import javax.persistence.Entity;

@Entity
@RestResource(basePath = "/mybase/path/")
public class WithNestedBasePath extends BaseEntity {

}
