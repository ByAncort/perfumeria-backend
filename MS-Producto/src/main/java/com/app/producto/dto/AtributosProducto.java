package com.app.producto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtributosProducto {
    private String tipo; // "hoddie", "polo", "jeans", etc.
    private List<String> tallas; // ["S", "M", "L", "XL"]
    private List<String> colores; // ["Negro", "Azul", "Rojo"]
    private String material; // "Algodón", "Poliéster", etc.
    private String marca;
    private String temporada; // "Verano", "Invierno", "Otoño"
    private Map<String, String> especificaciones; // {"manga": "larga", "cuello": "redondo"}
}