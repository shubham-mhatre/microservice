package com.sm.inventory_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import com.sm.inventory_service.entity.Inventory;
import com.sm.inventory_service.entity.repository.InventoryRepository;

@EnableDiscoveryClient
@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
		return args ->{
			Inventory inventory1=new Inventory();
			inventory1.setId(1);
			inventory1.setSkuCode("iphone13");
			inventory1.setQuantity(100);
			
			Inventory inventory2=new Inventory();
			inventory2.setId(2);
			inventory2.setSkuCode("iphone14");
			inventory2.setQuantity(0);
			
			inventoryRepository.save(inventory1);
			inventoryRepository.save(inventory2);
		};
		
	}

}
