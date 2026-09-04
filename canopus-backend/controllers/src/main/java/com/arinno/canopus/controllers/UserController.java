package com.arinno.canopus.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arinno.canopus.controllers.mapper.UserMapper;
import com.arinno.canopus.entities.ChangePasswordRequest;
import com.arinno.canopus.entities.PageResponse;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserListItem;
import com.arinno.canopus.entities.UserRequest;
import com.arinno.canopus.entities.UserResponse;
import com.arinno.canopus.entities.UserProfileRequest;
import com.arinno.canopus.error.CustomException;
import com.arinno.canopus.error.ErrorResponseFactory;
import com.arinno.canopus.servicies.JwtService;
import com.arinno.canopus.servicies.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService service;

    private final JwtService jwtService;

    private final UserMapper userMapper;

    UserController(UserService service, JwtService jwtService, UserMapper userMapper) {
        this.service = service;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<UserResponse> list(@RequestHeader(value="Authorization") String auth) {
        return service.findByCompany(jwtService.getCompanyFromToken(auth)).stream().map(userMapper::toDetail).toList();
    }

    @GetMapping("/page")
    public PageResponse<UserResponse> listPage(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestHeader(value="Authorization") String auth) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.min(Math.max(size, 1), 100);
        org.springframework.data.domain.Page<UserListItem> users = service.findListItemsByCompany(jwtService.getCompanyFromToken(auth),
                PageRequest.of(boundedPage, boundedSize));
        return PageResponse.from(users, userMapper::toListResponse);
    }

    @GetMapping("/technology/{id}")
    public List<UserResponse> listByTechnologies(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        return service.findByTechnologyAndCompany(id, jwtService.getCompanyFromToken(auth).getId()).stream()
            .map(userMapper::toIdName).toList();
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<?> show(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        Optional<User> userOptional = service.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
        if (userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(userMapper.toDetail(userOptional.orElseThrow()));
        }
        return ErrorResponseFactory.of(HttpStatus.NOT_FOUND, "El usuario no se encontró por el id:" + id);
    }

    @GetMapping("/me")
    public ResponseEntity<?> user(@RequestHeader(value="Authorization") String auth) {;
        Optional<User> userOptional = service.findByIdAndCompany(jwtService.getUserFromToken(auth).getId(), jwtService.getCompanyFromToken(auth));
        if (userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(userMapper.toDetail(userOptional.orElseThrow()));
        }
        return ErrorResponseFactory.of(HttpStatus.NOT_FOUND, "El usuario no se encontró");
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UserRequest request, BindingResult result, @RequestHeader(value="Authorization") String auth) throws CustomException {
        if (result.hasErrors()) {
            return ErrorResponseFactory.ofValidation(result);
        }
        try {
            User user = userMapper.toEntity(request, jwtService.getCompanyFromToken(auth));
            return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDetail(service.save(user)));
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }
  
    @PutMapping("/me")
    public ResponseEntity<?> updateMe(@Valid @RequestBody UserProfileRequest user, BindingResult result, @RequestHeader(value="Authorization") String auth) {
        if (result.hasErrors()) {
            return ErrorResponseFactory.ofValidation(result);
        }        
        Optional<User> userOptional = service.updateProfile(user, jwtService.getUserFromToken(auth).getId());
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userMapper.toDetail(userOptional.orElseThrow()));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, BindingResult result,
            @RequestHeader(value="Authorization") String auth) {
        if (result.hasErrors()) {
            return ErrorResponseFactory.ofValidation(result);
        }
        boolean changed = service.changePassword(request, jwtService.getUserFromToken(auth).getId());
        if (!changed) {
            return ErrorResponseFactory.of(HttpStatus.BAD_REQUEST, "La contraseña actual no es válida.");
        }
        return ResponseEntity.noContent().build();
    }
  
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) throws CustomException {
        try {
            if (service.deleteById(id, jwtService.getCompanyFromToken(auth))) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }
    
	@GetMapping("/select/{term}")
	public List<UserResponse> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth){	
        return service.findByNameContainingIgnoreCaseAndCompany(term, jwtService.getCompanyFromToken(auth)).stream().map(userMapper::toDetail).toList();
	}	

    @GetMapping("/message")
    public String getMessage(){
        System.out.println("klj");
        return "Hola Mundo 2";
    }   
  
}
