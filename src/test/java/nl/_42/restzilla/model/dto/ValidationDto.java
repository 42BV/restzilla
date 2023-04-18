package nl._42.restzilla.model.dto;

import jakarta.validation.constraints.NotNull;

public class ValidationDto {

    public String name;

    @NotNull
    public String street;

}
