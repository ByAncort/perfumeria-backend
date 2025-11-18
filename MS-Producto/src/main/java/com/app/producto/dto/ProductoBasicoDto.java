package com.app.producto.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoBasicoDto {
    private Long id;
    private String codigoSku;
    private String nombre;
}

