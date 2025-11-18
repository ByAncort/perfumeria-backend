package com.app.producto.service;

import com.app.producto.dto.ProductoDto;
import com.app.producto.dto.ProveedorResponse;
import com.app.producto.domain.model.Categoria;
import com.app.producto.domain.model.Producto;
import com.app.producto.repository.CategoriaRepository;
import com.app.producto.repository.ProductoRepository;
import com.app.producto.shared.client.MicroserviceClient;
import com.app.producto.shared.security.TokenContext;
import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cache.annotation.Cacheable;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {
    @Value("${auth.url.provMicro}")
    private String AUTH_SERVICE_URL;
    private final MicroserviceClient microserviceClient;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Cacheable("proveedores")
    @CircuitBreaker(name = "proveedorService", fallbackMethod = "fallbackProveedor")
    public ProveedorResponse consultarProveedor(Long proveedorId) {
        String token = TokenContext.getToken();
        String url = AUTH_SERVICE_URL + "/api/ms-inventario/proveedor/" + proveedorId;

        ResponseEntity<ProveedorResponse> response = microserviceClient.enviarConToken(
                url,
                HttpMethod.GET,
                null,
                ProveedorResponse.class,
                token
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al obtener proveedor");
        }

        return response.getBody();
    }
    public ProveedorResponse fallbackProveedor(Long proveedorId, Throwable t) {
        System.out.println("Fallback ejecutado para proveedor " + proveedorId + " por error: " + t.getMessage());
        return null;
    }

    public ServiceResult<ProductoDto> crearProducto(ProductoDto dto) {
        List<String> errors = new ArrayList<>();
        try {
            if(dto.getCodigoSku() == null || dto.getCodigoSku().isBlank()) {
                errors.add("El SKU es obligatorio");
            }

            if(productoRepository.existsByCodigoSku(dto.getCodigoSku())) {
                errors.add("El SKU ya existe");
            }

            if(dto.getSerial() != null && productoRepository.existsBySerial(dto.getSerial())) {
                errors.add("El serial ya está registrado");
            }

            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

            if(!errors.isEmpty()) {
                return new ServiceResult<>(errors);
            }



            Producto producto = toEntity(dto);
            producto.setCategoria(categoria);
            productoRepository.save(producto);
            return new ServiceResult<>(toDto(producto));

        } catch(Exception e) {
            errors.add("Error: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }
    public ServiceResult<List<ProductoDto>> listarProductos() {
        List<String> errors = new ArrayList<>();
        try {
            List<ProductoDto> dtoList = productoRepository.findAll().stream()
                    .map(this::toDto)
                    .toList();
            return new ServiceResult<>(dtoList);

        } catch (Exception e) {
            errors.add("Error al listar productos: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }
    public ServiceResult<ProductoDto> obtenerProducto(Long id) {
        List<String> errors = new ArrayList<>();
        try {
            Producto producto = productoRepository.findById(id).orElse(null);
            if (producto == null) {
                errors.add("Producto no encontrado con ID " + id);
                return new ServiceResult<>(errors);
            }
            return new ServiceResult<>(toDto(producto));

        } catch (Exception e) {
            errors.add("Error al obtener producto: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<ProductoDto> actualizarProducto(Long id, ProductoDto dto) {
        List<String> errors = new ArrayList<>();
        try {
            Producto producto = productoRepository.findById(id).orElse(null);
            if (producto == null) {
                errors.add("Producto no encontrado con ID " + id);
                return new ServiceResult<>(errors);
            }


            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElse(null);
            if (categoria == null) {
                errors.add("Categoría no encontrada con ID " + dto.getCategoriaId());
            }
            if (!errors.isEmpty()) return new ServiceResult<>(errors);

            ProveedorResponse prov = consultarProveedor(dto.getProveedorId());
            if (prov == null) {
                errors.add("Proveedor con ID " + dto.getProveedorId() + " no existe");
                return new ServiceResult<>(errors);
            }


//            producto.setCodigoSku(dto.getCodigoSku());
            producto.setNombre(dto.getNombre());
            producto.setDescripcion(dto.getDescripcion());
            producto.setPrecio(dto.getPrecio());
            producto.setCosto(dto.getCosto());
            producto.setCatalogo(dto.getCatalogo());
//            producto.setSerial(dto.getSerial());
            producto.setProveedoresId(prov.getId());
            producto.setCategoria(categoria);

            productoRepository.save(producto);
            return new ServiceResult<>(toDto(producto));

        } catch (Exception e) {
            errors.add("Error al actualizar producto: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }
    public ServiceResult<Void> eliminarProducto(Long id) {
        List<String> errors = new ArrayList<>();
        try {
            if (!productoRepository.existsById(id)) {
                errors.add("Producto con ID " + id + " no existe");
                return new ServiceResult<>(errors);
            }
            productoRepository.deleteById(id);
        } catch (Exception e) {
            errors.add("Error al eliminar producto: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
        return new ServiceResult<>(errors);
    }
    public Producto toEntity(ProductoDto dto) throws Exception {
        if (dto == null) return null;
        ProveedorResponse prov=consultarProveedor(dto.getProveedorId());
        //si exite dejalo pasar
        if (prov == null) {
            throw new Exception("Proveedor con ID " + dto.getProveedorId() + " no existe");
        }
        return Producto.builder()
                .id(dto.getId())
                .codigoSku(dto.getCodigoSku())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .costo(dto.getCosto())
                .catalogo(dto.getCatalogo())
                .serial(dto.getSerial())
                .proveedoresId(dto.getProveedorId())
                .build();
    }
    public ProductoDto toDto(Producto producto) {
        if (producto == null) return null;

        return ProductoDto.builder()
                .id(producto.getId())
                .codigoSku(producto.getCodigoSku())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .costo(producto.getCosto())
                .catalogo(producto.getCatalogo())
                .serial(producto.getSerial())
                .stock(producto.getStock())
                .categoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null)
                .build();
    }




}
