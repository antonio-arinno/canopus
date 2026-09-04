package com.arinno.canopus.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.RegisterRequest;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.error.CustomException;
import com.arinno.canopus.error.ErrorResponseFactory;
import com.arinno.canopus.servicies.CompanyService;
import com.arinno.canopus.servicies.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/register")
public class AuthController {

    private final CompanyService companyService;

    private final UserService userService;

    AuthController(CompanyService companyService, UserService userService) {
        this.companyService = companyService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult result) throws CustomException {
        if (result.hasErrors()) {
            return ErrorResponseFactory.ofValidation(result);
        }

        if (companyService.existsByName(request.getCompanyName())) {
            return ErrorResponseFactory.of(HttpStatus.CONFLICT, "Ya existe una empresa registrada con ese nombre.");
        }

        if (userService.findByUsername(request.getUsername()).isPresent()) {
            return ErrorResponseFactory.of(HttpStatus.CONFLICT, "El usuario ya está en uso.");
        }

        try {
            Company company = new Company();
            company.setName(request.getCompanyName());
            company.setDescription(request.getCompanyDescription());
            company.setCreateAt(new Date());
            Company savedCompany = companyService.save(company);

            User user = new User();
            user.setName(request.getName());
            user.setLastname(request.getLastname());
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setAdmin(true);
            user.setCompany(savedCompany);
            user.setTechnologies(new ArrayList<>());
            userService.save(user);

            Map<String, String> body = new HashMap<>();
            body.put("message", "Empresa y usuario administrador creados con éxito. Ya puedes iniciar sesión.");
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }

}
