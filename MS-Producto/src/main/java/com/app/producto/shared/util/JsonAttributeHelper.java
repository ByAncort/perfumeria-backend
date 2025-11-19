package com.app.producto.shared.util;


import com.app.producto.dto.AtributosProducto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonAttributeHelper {

    private static final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Configuración para manejar fechas y evitar errores con tipos Java Time
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Configuración para ser más permisivo con JSON
        mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        return mapper;
    }

    /**
     * Convierte JSON string a objeto AtributosProducto
     */
    public static AtributosProducto jsonToAtributos(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new AtributosProducto();
        }
        try {
            return objectMapper.readValue(json, AtributosProducto.class);
        } catch (JsonProcessingException e) {
            log.warn("Error parsing JSON attributes, returning empty attributes. JSON: {}", json, e);
            return new AtributosProducto();
        } catch (Exception e) {
            log.error("Unexpected error parsing JSON attributes: {}", json, e);
            return new AtributosProducto();
        }
    }

    /**
     * Convierte objeto AtributosProducto a JSON string
     */
    public static String atributosToJson(AtributosProducto atributos) {
        if (atributos == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(atributos);
        } catch (JsonProcessingException e) {
            log.error("Error converting attributes to JSON", e);
            return "{}";
        }
    }

    /**
     * Convierte JSON string a Map genérico
     */
    public static Map<String, Object> jsonToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Error parsing JSON to map, returning empty map. JSON: {}", json, e);
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("Unexpected error parsing JSON to map: {}", json, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Convierte Map a JSON string
     */
    public static String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Error converting map to JSON", e);
            return "{}";
        }
    }

    /**
     * Convierte JSON string a List de Strings (útil para tallas, colores, etc.)
     */
    public static List<String> jsonToStringList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Error parsing JSON to string list, returning empty list. JSON: {}", json, e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error parsing JSON to string list: {}", json, e);
            return Collections.emptyList();
        }
    }

    /**
     * Convierte List de Strings a JSON string
     */
    public static String stringListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("Error converting string list to JSON", e);
            return "[]";
        }
    }

    /**
     * Valida si un string es JSON válido
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Método específico para crear JSON de atributos desde campos individuales
     */
    public static String buildAtributosJson(String tipo, List<String> tallas, List<String> colores,
                                            String material, String marca, String temporada,
                                            Map<String, String> especificaciones) {
        AtributosProducto atributos = AtributosProducto.builder()
                .tipo(tipo)
                .tallas(tallas != null ? tallas : Collections.emptyList())
                .colores(colores != null ? colores : Collections.emptyList())
                .material(material)
                .marca(marca)
                .temporada(temporada)
                .especificaciones(especificaciones != null ? especificaciones : Collections.emptyMap())
                .build();

        return atributosToJson(atributos);
    }

    /**
     * Extrae un campo específico del JSON de atributos
     */
    public static String extractFieldFromJson(String json, String fieldName) {
        Map<String, Object> map = jsonToMap(json);
        return map.containsKey(fieldName) ? String.valueOf(map.get(fieldName)) : null;
    }

    /**
     * Extrae lista de un campo específico del JSON de atributos
     */
    @SuppressWarnings("unchecked")
    public static List<String> extractListFromJson(String json, String fieldName) {
        Map<String, Object> map = jsonToMap(json);
        if (map.containsKey(fieldName) && map.get(fieldName) instanceof List) {
            try {
                return (List<String>) map.get(fieldName);
            } catch (ClassCastException e) {
                log.warn("Field {} is not a List<String>", fieldName);
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
}
