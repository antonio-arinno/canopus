package com.arinno.canopus.organization.user.api;



import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.arinno.canopus.organization.user.api.mapper.UserMapper;
import com.arinno.canopus.organization.user.contract.ChangePasswordRequest;
import com.arinno.canopus.entities.PageResponse;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.organization.user.contract.UserListItem;
import com.arinno.canopus.organization.user.contract.UserRequest;
import com.arinno.canopus.organization.user.contract.UserResponse;
import com.arinno.canopus.services.JwtService;
import com.arinno.canopus.organization.user.application.UserService;
import com.arinno.canopus.organization.user.contract.UserProfileRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService service;
    private final JwtService jwtService;
    private final UserMapper userMapper;

        public UserController(UserService service, JwtService jwtService, UserMapper userMapper) {
        this.service = service;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> list(@RequestHeader(value="Authorization") String auth) {
        List<UserResponse> users = service.findByCompany(jwtService.getCompanyFromToken(auth))
                .stream()
                .map(userMapper::toDetail)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponse<UserResponse>> listPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, 
            @RequestHeader(value="Authorization") String auth) {
        
        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.min(Math.max(size, 1), 100);
        
        org.springframework.data.domain.Page<UserListItem> users = service.findListItemsByCompany(
                jwtService.getCompanyFromToken(auth),
                PageRequest.of(boundedPage, boundedSize)
        );
        return ResponseEntity.ok(PageResponse.from(users, userMapper::toListResponse));
    }

    @GetMapping("/technology/{id}")
    public ResponseEntity<List<UserResponse>> listByTechnologies(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        List<UserResponse> users = service.findByTechnologyAndCompany(id, jwtService.getCompanyFromToken(auth).getId())
                .stream()
                .map(userMapper::toIdName)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> show(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        User user = service.findByIdAndCompany(id, jwtService.getCompanyFromToken(auth));
        return ResponseEntity.ok(userMapper.toDetail(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> user(@RequestHeader(value="Authorization") String auth) {
        User user = service.findByIdAndCompany(jwtService.getUserFromToken(auth).getId(), jwtService.getCompanyFromToken(auth));
        return ResponseEntity.ok(userMapper.toDetail(user));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request, @RequestHeader(value="Authorization") String auth) {
        User user = userMapper.toEntity(request, jwtService.getCompanyFromToken(auth));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userMapper.toDetail(service.save(user, request.getTechnologies())));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UserProfileRequest request, @RequestHeader(value="Authorization") String auth) {
        User updatedUser = service.updateProfile(request, jwtService.getUserFromToken(auth).getId());
        return ResponseEntity.ok(userMapper.toDetail(updatedUser));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, @RequestHeader(value="Authorization") String auth) {
        service.changePassword(request, jwtService.getUserFromToken(auth).getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader(value="Authorization") String auth) {
        service.deleteById(id, jwtService.getCompanyFromToken(auth));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/select/{term}")
    public ResponseEntity<List<UserResponse>> listSelection(@PathVariable String term, @RequestHeader(value="Authorization") String auth) {
        List<UserResponse> selections = service.findByNameContainingIgnoreCaseAndCompany(term, jwtService.getCompanyFromToken(auth))
                .stream()
                .map(userMapper::toDetail)
                .toList();
        return ResponseEntity.ok(selections);
    }
}
