package com.app.producto.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String nombre;

    private String descripcion;

    @OneToMany(mappedBy = "categoria")
    @ToString.Exclude
    private List<Producto> productos = new ArrayList<>();
}
