package com.app.producto;
import com.app.producto.dto.CategoriaDto;
import com.app.producto.domain.model.Categoria;
import com.app.producto.repository.CategoriaRepository;
import com.app.producto.domain.service.CategoriaService;
import org.app.dto.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private CategoriaDto categoriaDto;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoriaDto = CategoriaDto.builder()
                .nombre("Electrónicos")
                .descripcion("Productos electrónicos")
                .build();

        categoria = Categoria.builder()
                .id(1L)
                .nombre("Electrónicos")
                .descripcion("Productos electrónicos")
                .build();
    }

    @Test
    void crearCategoria_deberiaRetornarCategoriaCuandoNoExiste() {
        when(categoriaRepository.existsByNombre(anyString())).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        ServiceResult<CategoriaDto> result = categoriaService.crearCategoria(categoriaDto);


        assertFalse(result.hasErrors());
        assertEquals("Electrónicos", result.getData().getNombre());
        verify(categoriaRepository, times(1)).existsByNombre(anyString());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }
    @Test
    void crearCategoria_deberiaRetornarErrorCuandoYaExiste() {
        when(categoriaRepository.existsByNombre(anyString())).thenReturn(true);
        ServiceResult<CategoriaDto> result = categoriaService.crearCategoria(categoriaDto);
        assertTrue(result.hasErrors());
        assertEquals("La categoría ya existe", result.getErrors().get(0));
        verify(categoriaRepository, times(1)).existsByNombre(anyString());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }


    @Test
    void eliminarCategoria_deberiaEliminarCuandoExiste() {
        
        Long id = 1L;
        when(categoriaRepository.existsById(id)).thenReturn(true);

        
        ServiceResult<Void> result = categoriaService.eliminarCategoria(id);

        assertFalse(result.hasErrors());
        verify(categoriaRepository, times(1)).existsById(id);
        verify(categoriaRepository, times(1)).deleteById(id);
    }

    @Test
    void eliminarCategoria_deberiaRetornarErrorCuandoNoExiste() {
        
        Long id = 99L;
        when(categoriaRepository.existsById(id)).thenReturn(false);

        
        ServiceResult<Void> result = categoriaService.eliminarCategoria(id);

        
        assertTrue(result.hasErrors());
        assertEquals("La categoría con ID 99 no existe.", result.getErrors().get(0));
        verify(categoriaRepository, times(1)).existsById(id);
        verify(categoriaRepository, never()).deleteById(anyLong());
    }

    @Test
    void actualizarCategoria_deberiaActualizarCuandoExiste() {
        
        Long id = 1L;
        CategoriaDto dtoActualizado = CategoriaDto.builder()
                .nombre("Electrónicos Actualizado")
                .descripcion("Descripción actualizada")
                .build();

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombre(anyString())).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        
        ServiceResult<CategoriaDto> result = categoriaService.actualizarCategoria(id, dtoActualizado);

        
        assertFalse(result.hasErrors());
        assertEquals("Electrónicos Actualizado", result.getData().getNombre());
        verify(categoriaRepository, times(1)).findById(id);
        verify(categoriaRepository, times(1)).existsByNombre(anyString());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void actualizarCategoria_deberiaRetornarErrorCuandoNoExiste() {
        
        Long id = 99L;
        when(categoriaRepository.findById(id)).thenReturn(Optional.empty());

        
        ServiceResult<CategoriaDto> result = categoriaService.actualizarCategoria(id, categoriaDto);

        
        assertTrue(result.hasErrors());
        assertEquals("Categoría no encontrada con ID 99", result.getErrors().get(0));
        verify(categoriaRepository, times(1)).findById(id);
        verify(categoriaRepository, never()).existsByNombre(anyString());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void actualizarCategoria_deberiaRetornarErrorCuandoNombreYaExiste() {
        
        Long id = 1L;
        CategoriaDto dtoConNombreExistente = CategoriaDto.builder()
                .nombre("Nombre Existente")
                .descripcion("Descripción")
                .build();

        when(categoriaRepository.findById(id)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombre("Nombre Existente")).thenReturn(true);

        
        ServiceResult<CategoriaDto> result = categoriaService.actualizarCategoria(id, dtoConNombreExistente);

        
        assertTrue(result.hasErrors());
        assertEquals("El nombre de categoría ya está en uso.", result.getErrors().get(0));
        verify(categoriaRepository, times(1)).findById(id);
        verify(categoriaRepository, times(1)).existsByNombre("Nombre Existente");
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }
}
