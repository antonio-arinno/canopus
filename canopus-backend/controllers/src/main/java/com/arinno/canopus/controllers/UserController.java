package com.arinno.canopus.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.TechnologyResponse;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserRequest;
import com.arinno.canopus.entities.UserResponse;
import com.arinno.canopus.servicies.IImputationService;
import com.arinno.canopus.servicies.IProductService;
import com.arinno.canopus.servicies.UserService;
import com.arinno.canopus.util.IUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService service;

    private final IProductService productService;

    private final IImputationService imputationService;

    private final IUtil util;

    UserController(UserService service, IProductService productService, IImputationService imputationService, IUtil util) {
        this.service = service;
        this.productService = productService;
        this.imputationService = imputationService;
        this.util = util;
    }

    @GetMapping
    public List<UserResponse> list(@RequestHeader(value="Authorization") String auth) {
        service.registrarYVerificar();
        return service.findByCompany(util.getCompany(auth)).stream().map(user -> GetUserResponse(user)).toList();
    }

    @GetMapping("/technology/{id}")
    public List<UserResponse> listByTechnologies(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        Technology technology = new Technology();
        technology.setId(id);
        List<Technology> technologies = new ArrayList<>();
        technologies.add(technology);
        return service.findByTechnologies(technologies).stream().map(user -> GetUserResponseIdName(user)).toList();
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<?> show(@PathVariable Long id) {;
        Optional<User> userOptional = service.findById(id);
        if (userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(GetUserResponse(userOptional.orElseThrow()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Collections.singletonMap("error", "el usuario no se encontro por el id:" + id));
    }

    @GetMapping("/me")
    public ResponseEntity<?> user(@RequestHeader(value="Authorization") String auth) {;
        Optional<User> userOptional = service.findById(util.getUser(auth).getId());
        if (userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(GetUserResponse(userOptional.orElseThrow()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Collections.singletonMap("error", "el usuario no se encontro"));
    }
    
    private UserResponse GetUserResponse (User user){
        return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .email(user.getEmail())
                    .lastname(user.getLastname())
                    .technologies(user.getTechnologies().stream().map(technology -> GetTechnologyResponse(technology)).toList())
                    .countProducts(productService.countByResponsible(user))
                    .time(imputationService.timeByUser(user))
                    .build();
    }

    private UserResponse GetUserResponseIdName (User user){
            return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
    
    private TechnologyResponse GetTechnologyResponse (Technology technology){
        return TechnologyResponse.builder()
                    .id(technology.getId())
                    .name(technology.getName())
                    .build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody User user, @RequestHeader(value="Authorization") String auth) {
        user.setCompany(util.getCompany(auth));
        user.setPassword(user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user));
    }
  
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody UserRequest user, BindingResult result, @PathVariable Long id) {
        if (result.hasErrors()) {
            return validation(result);
        }        
        Optional<User> userOptional = service.update(user, id);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(GetUserResponse(userOptional.orElseThrow()));
//            return ResponseEntity.ok(userOptional.orElseThrow());
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
	public List<UserResponse> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth){	
        return service.findByNameContainingIgnoreCaseAndCompany(term, util.getCompany(auth)).stream().map(user -> GetUserResponse(user)).toList();
	}	

    @GetMapping("/message")
    public String getMessage(){
        System.out.println("klj");
        return "Hola Mundo 2";
    }   
  
}
