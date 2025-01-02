package com.arinno.canopus.controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserRequest;
import com.arinno.canopus.servicies.UserService;
import com.arinno.canopus.util.IUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
	private IUtil util;

    @GetMapping
    public List<User> list(@RequestHeader(value="Authorization") String auth) {
//        getCompany(auth);
        return service.findByCompany(util.getCompany(auth));
    }
        

    @GetMapping("/{id}")
    public ResponseEntity<?> show(@PathVariable Long id) {
        Optional<User> userOptional = service.findById(id);
        if (userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(userOptional.orElseThrow());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", "el usuario no se encontro por el id:" + id));
    }
    
    /*
    public ResponseEntity<?> create(@Valid @RequestBody User user, BindingResult result, @RequestHeader(value="Authorization") String auth) {
        if (result.hasErrors()) {
            return validation(result);
        }
     */        
    @PostMapping
    public ResponseEntity<?> create(@RequestBody User user, @RequestHeader(value="Authorization") String auth) {
        user.setCompany(util.getCompany(auth));
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody UserRequest user, BindingResult result, @PathVariable Long id) {

        if (result.hasErrors()) {
            return validation(result);
        }
        
        Optional<User> userOptional = service.update(user, id);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<User> userOptional = service.findById(id);
        if (userOptional.isPresent()) {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error -> {
            errors.put(error.getField(), "El campo " + error.getField() + " " + error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }    


	@GetMapping("/select/{term}")
	public List<User> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth){	
		return service.findByNameContainingIgnoreCaseAndCompany(term, util.getCompany(auth));
	}	

    @GetMapping("/message")
    public String getMessage(){
        System.out.println("klj");
        return "Hola Mundo 2";
    }   
  
}
