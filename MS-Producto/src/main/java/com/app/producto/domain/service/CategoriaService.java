package com.app.producto.domain.service;


import com.app.producto.dto.*;
import com.app.producto.domain.model.Categoria;
import com.app.producto.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public ServiceResult<CategoriaDto> crearCategoria(CategoriaDto dto) {
        List<String> errors = new ArrayList<>();
        try {
            if(categoriaRepository.existsByNombre(dto.getNombre())) {
                errors.add("La categoría ya existe");
                return new ServiceResult<>(errors);
            }

            Categoria categoria = Categoria.builder()

                    .nombre(dto.getNombre())
                    .descripcion(dto.getDescripcion())
                    .build();
            categoriaRepository.save(categoria);
            return new ServiceResult<>(dto);

        } catch(Exception e) {
            errors.add("Error: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }
    public ServiceResult<List<CategoriaDto>> listarCategorias() {
        List<String> errors = new ArrayList<>();
        try {
            List<Categoria> categorias = categoriaRepository.findAll();

            List<CategoriaDto> dtoList = categorias.stream()
                    .map(cat -> CategoriaDto.builder()
                            .id(cat.getId())
                            .nombre(cat.getNombre())
                            .descripcion(cat.getDescripcion())
                            .build())
                    .collect(Collectors.toList());

            return new ServiceResult<>(dtoList);

        } catch (Exception e) {
            errors.add("Error al listar categorías: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }
    public ServiceResult<Void> eliminarCategoria(Long id) {
        List<String> errors = new ArrayList<>();
        try {
            if (!categoriaRepository.existsById(id)) {
                errors.add("La categoría con ID " + id + " no existe.");
                return new ServiceResult<>(errors);
            }

            categoriaRepository.deleteById(id);


        } catch (Exception e) {
            errors.add("Error al eliminar categoría: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
        return new ServiceResult<>(errors);
    }

    public ServiceResult<CategoriaDto> actualizarCategoria(Long id, CategoriaDto dto) {
        List<String> errors = new ArrayList<>();
        try {
            Categoria categoria = categoriaRepository.findById(id).orElse(null);

            if (categoria == null) {
                errors.add("Categoría no encontrada con ID " + id);
                return new ServiceResult<>(errors);
            }

            if (!categoria.getNombre().equals(dto.getNombre()) &&
                    categoriaRepository.existsByNombre(dto.getNombre())) {
                errors.add("El nombre de categoría ya está en uso.");
                return new ServiceResult<>(errors);
            }

            categoria.setNombre(dto.getNombre());
            categoria.setDescripcion(dto.getDescripcion());

            categoriaRepository.save(categoria);

            return new ServiceResult<>(dto);

        } catch (Exception e) {
            errors.add("Error al actualizar categoría: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }


}
