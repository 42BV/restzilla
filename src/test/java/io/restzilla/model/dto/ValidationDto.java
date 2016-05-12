package io.restzilla.model.dto;

import javax.validation.constraints.NotNull;

public class ValidationDto {

    public String name;

    @NotNull
    public String street;

}
