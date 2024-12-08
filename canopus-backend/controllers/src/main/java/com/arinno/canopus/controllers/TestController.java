package com.arinno.canopus.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/message")
    public String getMessage(){
        System.out.println("Hola Mundo");
        return "Hola Mundo";
    }   

}