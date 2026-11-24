package com.escolasegura.modules.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class RbacTestController {

    @GetMapping("/coordinator")
    public ResponseEntity<Map<String, Object>> coordinatorArea(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(Map.of(
                "status", "authorized",
                "role", "Coordenador / Supervisor",
                "message", "Acesso concedido à gestão e monitoramento escolar.",
                "user", email
        ));
    }

    @GetMapping("/guardian")
    public ResponseEntity<Map<String, Object>> guardianArea(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(Map.of(
                "status", "authorized",
                "role", "Responsável",
                "message", "Acesso concedido ao portal do responsável familiar.",
                "user", email
        ));
    }

    @GetMapping("/student")
    public ResponseEntity<Map<String, Object>> studentArea(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(Map.of(
                "status", "authorized",
                "role", "Aluno",
                "message", "Acesso concedido à área do aluno.",
                "user", email
        ));
    }
}
