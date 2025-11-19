package com.app.producto.controller;

import com.app.producto.dto.*;
import com.app.producto.domain.service.CategoriaService;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "API para gestión de categorías de productos")
public class CategoriaController {
    private final CategoriaService categoriaService;

    @Operation(
            summary = "Crear categoría",
            description = "Registra una nueva categoría de productos en el sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoría creada exitosamente",
                    content = @Content(schema = @Schema(implementation = CategoriaDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PostMapping
    public ResponseEntity<?> createCategoria(
            @Parameter(description = "Datos de la categoría a crear", required = true)
            @Valid @RequestBody CategoriaDto request) {
        ServiceResult<CategoriaDto> result = categoriaService.crearCategoria(request);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        } else {
            CategoriaDto categoria = result.getData();
            EntityModel<CategoriaDto> resource = EntityModel.of(categoria);
            resource.add(WebMvcLinkBuilder.linkTo(methodOn(CategoriaController.class).createCategoria(request)).withSelfRel());
            resource.add(WebMvcLinkBuilder.linkTo(methodOn(CategoriaController.class).actualizar(1L, categoria)).withRel("update"));
            resource.add(WebMvcLinkBuilder.linkTo(methodOn(CategoriaController.class).eliminar(1L)).withRel("delete"));
            resource.add(WebMvcLinkBuilder.linkTo(methodOn(CategoriaController.class).listar()).withRel("all-categories"));

            return ResponseEntity.status(HttpStatus.CREATED).body(resource);
        }
    }

    @Operation(
            summary = "Listar categorías",
            description = "Obtiene todas las categorías de productos registradas"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de categorías obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = CategoriaDto[].class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al obtener las categorías",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @GetMapping
    public ResponseEntity<?> listar() {
        ServiceResult<List<CategoriaDto>> result = categoriaService.listarCategorias();
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        } else {
            List<EntityModel<CategoriaDto>> categorias = result.getData().stream()
                    .map(categoria -> {
                        EntityModel<CategoriaDto> resource = EntityModel.of(categoria);
                        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CategoriaController.class).actualizar(1L, categoria)).withRel("update"));
                        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CategoriaController.class).eliminar(1L)).withRel("delete"));
                        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CategoriaController.class).listar()).withSelfRel());
                        return resource;
                    })
                    .collect(Collectors.toList());

            Link selfLink = WebMvcLinkBuilder.linkTo(methodOn(CategoriaController.class).listar()).withSelfRel();
            Link createLink = WebMvcLinkBuilder.linkTo(methodOn(CategoriaController.class).createCategoria(null)).withRel("create");

            CollectionModel<EntityModel<CategoriaDto>> resources = CollectionModel.of(categorias, selfLink, createLink);

            return ResponseEntity.ok().body(resources);
        }
    }

    @Operation(
            summary = "Actualizar categoría",
            description = "Actualiza los datos de una categoría existente"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = CategoriaDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @Parameter(description = "ID de la categoría a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Datos actualizados de la categoría", required = true)
            @Valid @RequestBody CategoriaDto request) {
        ServiceResult<CategoriaDto> result = categoriaService.actualizarCategoria(id, request);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        CategoriaDto categoria = result.getData();
        EntityModel<CategoriaDto> resource = EntityModel.of(categoria);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CategoriaController.class).actualizar(id, request)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CategoriaController.class).eliminar(id)).withRel("delete"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CategoriaController.class).listar()).withRel("all-categories"));

        return ResponseEntity.ok().body(resource);
    }

    @Operation(
            summary = "Eliminar categoría",
            description = "Elimina una categoría del sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Categoría eliminada exitosamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al eliminar la categoría",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @Parameter(description = "ID de la categoría a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        ServiceResult<Void> result = categoriaService.eliminarCategoria(id);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.noContent().build();
    }
}