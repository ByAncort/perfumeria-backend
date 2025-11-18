package com.app.producto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {
    private Long id;
    private String codigoSku;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal costo;
    private Integer stock;
    private Long categoriaId;
    private String catalogo;
    private String serial;
    private Long proveedorId;
}
