package com.app.proveedores.Service;


import com.app.proveedores.Dto.ProveedorDto;
import com.app.proveedores.Dto.ServiceResult;
import com.app.proveedores.Models.Proveedor;
import com.app.proveedores.Repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProveedorService {
    private final ProveedorRepository proveedorRepository;
    private static final Logger logger = LoggerFactory.getLogger(Proveedor.class);
    //    EXPRESIONES PARA VALIDAR DATOS CON REGEX
    private static final Pattern RUT_PATTERN = Pattern.compile("^\\d{1,8}-[\\dkK]$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?56?\\d{9}$");

    public ServiceResult<Proveedor> addProveedor(ProveedorDto request) {
        List<String> errors = new ArrayList<>();
        try {
            if (request.getNombre() == null || request.getNombre().isBlank()) {
                errors.add("El nombre es obligatorio");
            }

            if (request.getRut() == null || request.getRut().isBlank()) {
                errors.add("El RUT es obligatorio");
            } else if (!validarRutChileno(request.getRut())) {
                errors.add("El RUT no es válido");
            }

            if (request.getEmail() != null && !request.getEmail().isBlank()
                    && !EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
                errors.add("El formato del email es inválido");
            }

            if (request.getTelefono() != null && !request.getTelefono().isBlank()
                    && !PHONE_PATTERN.matcher(request.getTelefono()).matches()) {
                errors.add("El formato del teléfono es inválido");
            }

            if (!errors.isEmpty()) {
                return new ServiceResult<>(errors);
            }

            if (proveedorRepository.existsByRut(normalizarRut(request.getRut()))) {
                errors.add("El RUT ya está registrado");
                return new ServiceResult<>(errors);
            }

            Proveedor proveedor = Proveedor.builder()
                    .nombre(request.getNombre())
                    .rut(normalizarRut(request.getRut()))
                    .direccion(request.getDireccion())
                    .email(request.getEmail())
                    .telefono(request.getTelefono())
                    .activo(true)
                    .build();

            Proveedor proveedorSaved = proveedorRepository.save(proveedor);
            logger.info("Proveedor guardado exitosamente: {}", proveedorSaved.getId());
            return new ServiceResult<>(proveedorSaved);

        } catch (Exception e) {
            logger.error("Error creando proveedor: ", e);
            errors.add("Error interno al crear proveedor: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    private boolean validarRutChileno(String rut) {
        String rutLimpio = normalizarRut(rut);

        if (!RUT_PATTERN.matcher(rutLimpio).matches()) {
            return false;
        }

        String[] partes = rutLimpio.split("-");
        String cuerpo = partes[0];
        String dv = partes[1].toUpperCase();

        int suma = 0;
        int multiplicador = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }

        int resto = suma % 11;
        String dvCalculado = switch (11 - resto) {
            case 11 -> "0";
            case 10 -> "K";
            default -> String.valueOf(11 - resto);
        };

        return dv.equals(dvCalculado);
    }

    private String normalizarRut(String rut) {
        return rut.replaceAll("[^0-9kK-]", "").toUpperCase();
    }

    public ServiceResult<List<Proveedor>> getAllProveedoresActivos() {
        List<String> errors = new ArrayList<>();
        try {
            List<Proveedor> proveedores = proveedorRepository.findByActivoTrue();
            return new ServiceResult<>(proveedores);
        } catch (Exception e) {
            logger.error("Error al obtener proveedores activos: ", e);
            errors.add("Error interno al listar proveedores: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<Proveedor> getProveedorById(Long id) {
        List<String> errors = new ArrayList<>();
        try {
            return proveedorRepository.findById(id)
                    .map(proveedor -> new ServiceResult<>(proveedor))
                    .orElseGet(() -> {
                        errors.add("Proveedor no encontrado con ID: " + id);
                        return new ServiceResult<>(errors);
                    });
        } catch (Exception e) {
            logger.error("Error al buscar proveedor por ID: {}", id, e);
            errors.add("Error interno: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<Proveedor> updateProveedor(Long id, ProveedorDto dto) {
        List<String> errors = new ArrayList<>();
        try {
            Proveedor proveedorExistente = proveedorRepository.findById(id)
                    .orElse(null);

            if (proveedorExistente == null) {
                errors.add("Proveedor no encontrado");
                return new ServiceResult<>(errors);
            }

            // Validar coincidencia de RUT
            if (!normalizarRut(dto.getRut()).equals(proveedorExistente.getRut())) {
                errors.add("No se puede modificar el RUT");
                return new ServiceResult<>(errors);
            }

            // Validaciones de campos
            if (dto.getNombre() == null || dto.getNombre().isBlank()) {
                errors.add("El nombre es obligatorio");
            }

            if (dto.getEmail() != null && !dto.getEmail().isBlank()
                    && !EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
                errors.add("Formato de email inválido");
            }

            if (dto.getTelefono() != null && !dto.getTelefono().isBlank()
                    && !PHONE_PATTERN.matcher(dto.getTelefono()).matches()) {
                errors.add("Formato de teléfono inválido");
            }

            if (!errors.isEmpty()) {
                return new ServiceResult<>(errors);
            }

            // Actualizar campos
            proveedorExistente.setNombre(dto.getNombre());
            proveedorExistente.setDireccion(dto.getDireccion());
            proveedorExistente.setEmail(dto.getEmail());
            proveedorExistente.setTelefono(dto.getTelefono());

            Proveedor proveedorActualizado = proveedorRepository.save(proveedorExistente);
            logger.info("Proveedor actualizado: {}", id);
            return new ServiceResult<>(proveedorActualizado);

        } catch (Exception e) {
            logger.error("Error actualizando proveedor ID {}: ", id, e);
            errors.add("Error interno al actualizar: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<Proveedor> toggleActivoProveedor(Long id, boolean activo) {
        List<String> errors = new ArrayList<>();
        try {
            Proveedor proveedor = proveedorRepository.findById(id)
                    .orElse(null);

            if (proveedor == null) {
                errors.add("Proveedor no encontrado");
                return new ServiceResult<>(errors);
            }


            proveedor.setActivo(activo);
            Proveedor proveedorActualizado = proveedorRepository.save(proveedor);
            return new ServiceResult<>(proveedorActualizado);

        } catch (Exception e) {
            logger.error("Error cambiando estado de proveedor ID {}: ", id, e);
            errors.add("Error interno: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<Proveedor> getProveedorByRut(String rut) {
        List<String> errors = new ArrayList<>();
        try {
            String rutNormalizado = normalizarRut(rut);

            if (!validarRutChileno(rutNormalizado)) {
                errors.add("RUT inválido");
                return new ServiceResult<>(errors);
            }

            Proveedor proveedor = proveedorRepository.findByRut(rutNormalizado)
                    .orElse(null);

            if (proveedor == null) {
                errors.add("No existe proveedor con RUT: " + rut);
                return new ServiceResult<>(errors);
            }

            return new ServiceResult<>(proveedor);

        } catch (Exception e) {
            logger.error("Error buscando por RUT {}: ", rut, e);
            errors.add("Error interno: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }
    public ServiceResult<Proveedor> deleteProveedor(Long id) {
        List<String> errors = new ArrayList<>();
        try {
            Proveedor proveedor = proveedorRepository.findById(id).orElse(null);

            if (proveedor == null) {
                errors.add("Proveedor no encontrado con ID: " + id);
                return new ServiceResult<>(errors);
            }

            if (!proveedor.isActivo()) {
                errors.add("El proveedor ya se encuentra inactivo");
                return new ServiceResult<>(errors);
            }

       proveedorRepository.delete(proveedor);
            logger.info("Proveedor eliminado (inactivado) con ID: {}", id);
            return new ServiceResult<>(proveedor);

        } catch (Exception e) {
            logger.error("Error eliminando proveedor ID {}: ", id, e);
            errors.add("Error interno al eliminar proveedor: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

}
