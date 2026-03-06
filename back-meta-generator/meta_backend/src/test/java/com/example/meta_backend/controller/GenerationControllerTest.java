package com.example.meta_backend.controller;

import com.example.meta_backend.service.generator.CodeGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GenerationControllerTest {

    @Mock
    private CodeGeneratorService generatorService;

    @InjectMocks
    private GenerationController controller;

    @Test
    void shouldDefaultMissingCollectionsAndReturnOk() {
        Map<String, Object> payload = Map.of("appName", "my_app");

        ResponseEntity<String> response = controller.generate(payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Código gerado em /my_app/", response.getBody());

        ArgumentCaptor<List<Map<String, Object>>> entitiesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Map<String, Object>>> functionalitiesCaptor = ArgumentCaptor.forClass(List.class);

        verify(generatorService).generateApplication(
                org.mockito.ArgumentMatchers.eq("my_app"),
                entitiesCaptor.capture(),
                functionalitiesCaptor.capture()
        );

        assertTrue(entitiesCaptor.getValue().isEmpty());
        assertTrue(functionalitiesCaptor.getValue().isEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenServiceRejectsPayload() {
        doThrow(new IllegalArgumentException("appName é obrigatório."))
                .when(generatorService)
                .generateApplication(any(), any(), any());

        ResponseEntity<String> response = controller.generate(Map.of());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("appName é obrigatório.", response.getBody());
    }
}
