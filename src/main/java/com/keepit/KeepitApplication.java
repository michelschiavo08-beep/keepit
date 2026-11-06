package com.keepit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KeepitApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeepitApplication.class, args);
        System.out.println("\n=======================================================");
        System.out.println("🚀 KeepIT - Gestão de Ativos e Manutenção Preditiva");
        System.out.println("Servidor iniciado em: http://localhost:8080");
        System.out.println("Console H2 disponível em: http://localhost:8080/h2-console");
        System.out.println("=======================================================\n");
    }
}
