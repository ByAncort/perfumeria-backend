package com.app.producto.controller;

import com.app.producto.dto.ProductoDto;
import com.app.producto.domain.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "API para gestión de productos")
public class ProductoController {

    private final ProductoService productoService;

    @Operation(
            summary = "Crear nuevo producto",
            description = "Registra un nuevo producto en el sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductoDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PostMapping("create")
    public ResponseEntity<?> crearProducto(
            @Parameter(description = "Datos del producto a crear", required = true)
            @Valid @RequestBody ProductoDto dto) {
        ServiceResult<ProductoDto> result = productoService.crearProducto(dto);
        return handleResult(result, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Listar productos",
            description = "Obtiene la lista completa de productos registrados"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de productos obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductoDto[].class)))
    })
    @GetMapping("list")
    public ResponseEntity<?> listar() {
        ServiceResult<List<ProductoDto>> result = productoService.listarProductos();
        return handleResult(result, HttpStatus.OK);
    }

    @Operation(
            summary = "Obtener producto por ID",
            description = "Recupera la información de un producto específico"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductoDto.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @GetMapping("get/{id}")
    public ResponseEntity<?> obtener(
            @Parameter(description = "ID del producto", required = true, example = "1")
            @PathVariable Long id) {
        ServiceResult<ProductoDto> result = productoService.obtenerProducto(id);
        return handleResult(result, HttpStatus.OK);
    }

    @Operation(
            summary = "Actualizar producto",
            description = "Actualiza la información de un producto existente"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductoDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PutMapping("update/{id}")
    public ResponseEntity<?> actualizar(
            @Parameter(description = "ID del producto a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Datos actualizados del producto", required = true)
            @Valid @RequestBody ProductoDto request) {
        ServiceResult<ProductoDto> result = productoService.actualizarProducto(id, request);
        return handleResult(result, HttpStatus.OK);
    }

    @Operation(
            summary = "Eliminar producto",
            description = "Elimina un producto del sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Producto eliminado exitosamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al eliminar el producto",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> eliminar(
            @Parameter(description = "ID del producto a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        ServiceResult<Void> result = productoService.eliminarProducto(id);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> handleResult(ServiceResult<?> result, HttpStatus successStatus) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.status(successStatus).body(result.getData());
    }
}