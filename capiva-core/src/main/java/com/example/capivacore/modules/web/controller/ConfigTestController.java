package com.example.capivacore.modules.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
@RequiredArgsConstructor
public class ConfigTestController {

    private final Environment environment;

    @Value("${capiva.ambiente.mensagem:Erro: Nao puxou do Config Server}")
    private String mensagemAmbiente;

    @GetMapping("/api/test-config")
    public ResponseEntity<String> testConfig() {
        // Pega a porta real em tempo de execução onde o Tomcat/Netty subiu
        String portaReal = environment.getProperty("local.server.port");

        return ResponseEntity.ok("Instância respondendo na Porta [" + portaReal + "] - Mensagem: " + mensagemAmbiente);
    }
}