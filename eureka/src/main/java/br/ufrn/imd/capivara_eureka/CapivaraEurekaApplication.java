package br.ufrn.imd.capivara_eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class CapivaraEurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CapivaraEurekaApplication.class, args);
	}

}
