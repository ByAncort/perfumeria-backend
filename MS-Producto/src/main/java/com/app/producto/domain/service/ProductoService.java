package com.app.producto.domain.service;

import com.app.producto.dto.AtributosProducto;
import com.app.producto.dto.ProductoDto;
import com.app.producto.dto.ProveedorResponse;
import com.app.producto.domain.model.Categoria;
import com.app.producto.domain.model.Producto;
import com.app.producto.repository.CategoriaRepository;
import com.app.producto.repository.ProductoRepository;
import com.app.producto.shared.client.MicroserviceClient;
import com.app.producto.shared.security.TokenContext;
import com.app.producto.shared.util.JsonAttributeHelper;
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
import java.util.Map;

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

            // Actualizar atributos
            String atributosJson = buildAtributosJson(dto);
            producto.setAtributos(atributosJson);

            producto.setNombre(dto.getNombre());
            producto.setImagePrimary(dto.getImagePrimary());
            producto.setImageSecondary(dto.getImageSecondary());
            producto.setDescripcion(dto.getDescripcion());
            producto.setPrecio(dto.getPrecio());
            producto.setCosto(dto.getCosto());
            producto.setCatalogo(dto.getCatalogo());
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
        ProveedorResponse prov = consultarProveedor(dto.getProveedorId());
        if (prov == null) {
            throw new Exception("Proveedor con ID " + dto.getProveedorId() + " no existe");
        }

        // Construir JSON de atributos
        String atributosJson = buildAtributosJson(dto);

        return Producto.builder()
                .id(dto.getId())
                .codigoSku(dto.getCodigoSku())
                .nombre(dto.getNombre())
                .imagePrimary(dto.getImagePrimary())
                .imageSecondary(dto.getImageSecondary())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .costo(dto.getCosto())
                .catalogo(dto.getCatalogo())
                .serial(dto.getSerial())
                .proveedoresId(dto.getProveedorId())
                .atributos(atributosJson) // Agregar atributos
                .build();
    }

    public ProductoDto toDto(Producto producto) {
        if (producto == null) return null;

        ProductoDto.ProductoDtoBuilder builder = ProductoDto.builder()
                .id(producto.getId())
                .codigoSku(producto.getCodigoSku())
                .nombre(producto.getNombre())
                .imagePrimary(producto.getImagePrimary())
                .imageSecondary(producto.getImageSecondary())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .costo(producto.getCosto())
                .catalogo(producto.getCatalogo())
                .serial(producto.getSerial())
                .stock(producto.getStock())
                .categoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null);

        // Si hay atributos, parsearlos y agregarlos al DTO
        if (producto.getAtributos() != null && !producto.getAtributos().trim().isEmpty()) {
            Map<String, Object> atributosMap = JsonAttributeHelper.jsonToMap(producto.getAtributos());
            populateDtoFromAtributos(builder, atributosMap);
        }

        return builder.build();
    }

    // Método auxiliar para construir JSON de atributos
    private String buildAtributosJson(ProductoDto dto) {
        try {
            AtributosProducto atributos = AtributosProducto.builder()
                    .tipo(dto.getTipo())
                    .tallas(dto.getTallas())
                    .colores(dto.getColores())
                    .material(dto.getMaterial())
                    .marca(dto.getMarca())
                    .temporada(dto.getTemporada())
                    .especificaciones(dto.getEspecificaciones())
                    .build();

            return JsonAttributeHelper.atributosToJson(atributos);
        } catch (Exception e) {
            // En caso de error, retornar JSON básico
            return "{\"tipo\":\"" + dto.getTipo() + "\"}";
        }
    }

    // Método auxiliar para poblar DTO desde atributos JSON
    private void populateDtoFromAtributos(ProductoDto.ProductoDtoBuilder builder, Map<String, Object> atributosMap) {
        if (atributosMap.containsKey("tipo")) {
            builder.tipo((String) atributosMap.get("tipo"));
        }
        if (atributosMap.containsKey("tallas")) {
            builder.tallas((List<String>) atributosMap.get("tallas"));
        }
        if (atributosMap.containsKey("colores")) {
            builder.colores((List<String>) atributosMap.get("colores"));
        }
        if (atributosMap.containsKey("material")) {
            builder.material((String) atributosMap.get("material"));
        }
        if (atributosMap.containsKey("marca")) {
            builder.marca((String) atributosMap.get("marca"));
        }
        if (atributosMap.containsKey("temporada")) {
            builder.temporada((String) atributosMap.get("temporada"));
        }
        if (atributosMap.containsKey("especificaciones")) {
            builder.especificaciones((Map<String, String>) atributosMap.get("especificaciones"));
        }
    }
}