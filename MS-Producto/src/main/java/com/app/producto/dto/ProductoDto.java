package com.app.producto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {
    private Long id;
    private String codigoSku;
    private String nombre;
    private String imagePrimary;
    private String imageSecondary;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal costo;
    private Integer stock;
    private Long categoriaId;
    private String catalogo;
    private String serial;
    private Long proveedorId;

    private String tipo;
    private List<String> tallas;
    private List<String> colores;
    private String material;
    private String marca;
    private String temporada;
    private Map<String, String> especificaciones;
}
