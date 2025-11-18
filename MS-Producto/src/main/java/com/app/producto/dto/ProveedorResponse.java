package com.app.producto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorResponse {
    private Long id;
    private String nombre;
    private String rut;
    private String direccion;
    private String telefono;
    private String email;
    private boolean activo;
    private List<Long> productos;
}