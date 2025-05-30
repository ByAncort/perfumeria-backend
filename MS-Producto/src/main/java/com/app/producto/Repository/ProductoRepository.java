package com.app.producto.Repository;

import com.app.producto.Models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByCodigoSku(String codigoSku);

    Optional<Producto> findBySerial(String serial);

    boolean existsBySerial(String serial);

    List<Producto> findByCatalogo(String catalogo);
}
