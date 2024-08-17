package com.sm.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {
	
	@RequestMapping("/fallback/product")
    public ResponseEntity<String> productServiceFallback() {
        return new ResponseEntity<>("Product Service is currently unavailable. Please try again later.", HttpStatus.OK);
    }

    @RequestMapping("/fallback/order")
    public ResponseEntity<String> orderServiceFallback() {
        return new ResponseEntity<>("Order Service is currently unavailable. Please try again later.", HttpStatus.OK);
    }

    @RequestMapping("/fallback/inventory")
    public ResponseEntity<String> inventoryServiceFallback() {
        return new ResponseEntity<>("Inventory Service is currently unavailable. Please try again later.", HttpStatus.OK);
    }
    
    @RequestMapping("/fallback/auth-jwt")
    public ResponseEntity<String> authJwtServiceFallback() {
        return new ResponseEntity<>("Auth JWT Service is currently unavailable. Please try again later.", HttpStatus.OK);
    }

}
