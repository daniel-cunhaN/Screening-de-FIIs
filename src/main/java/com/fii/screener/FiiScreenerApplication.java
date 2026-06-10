package com.fii.screener;

import com.fii.screener.service.BrapiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FiiScreenerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FiiScreenerApplication.class, args);
	}

	@Bean
	public CommandLineRunner init(BrapiService brapiService) {
		return args -> {
			System.out.println(">>> Executando carga inicial de dados de FIIs...");
			brapiService.updateFiiData();
			System.out.println(">>> Carga inicial concluída!");
		};
	}

}
