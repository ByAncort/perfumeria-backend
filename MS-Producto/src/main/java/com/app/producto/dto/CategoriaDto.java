package com.app.producto.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDto {
    private Long id;
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String descripcion;
}