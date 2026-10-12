package com.arinno.canopus.organization.user.api;



import java.util.List;
import java.util.Map;

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
import com.arinno.canopus.shared.contract.PageResponse;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.organization.user.contract.UserListItem;
import com.arinno.canopus.organization.user.contract.UserRequest;
import com.arinno.canopus.organization.user.contract.UserResponse;
import com.arinno.canopus.organization.user.contract.UserTechnologyResponse;
import com.arinno.canopus.security.JwtService;
import com.arinno.canopus.organization.user.application.UserService;
import com.arinno.canopus.organization.user.contract.UserProfileRequest;
import com.arinno.canopus.reporting.application.ReportingService;
import com.arinno.canopus.reporting.contract.UserMetrics;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService service;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final ReportingService reportingService;

    public UserController(UserService service, JwtService jwtService, UserMapper userMapper,
            ReportingService reportingService) {
        this.service = service;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.reportingService = reportingService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> list(@RequestHeader(value="Authorization") String auth) {
        List<User> users = service.findByCompany(jwtService.getCompanyFromToken(auth));
        return ResponseEntity.ok(toDetails(users));
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
        return ResponseEntity.ok(toDetail(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> user(@RequestHeader(value="Authorization") String auth) {
        User user = service.findByIdAndCompany(jwtService.getUserFromToken(auth).getId(), jwtService.getCompanyFromToken(auth));
        return ResponseEntity.ok(toDetail(user));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request, @RequestHeader(value="Authorization") String auth) {
        User user = userMapper.toEntity(request, jwtService.getCompanyFromToken(auth));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(toDetail(service.save(user, request.getTechnologies())));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UserProfileRequest request, @RequestHeader(value="Authorization") String auth) {
        User updatedUser = service.updateProfile(request, jwtService.getUserFromToken(auth).getId());
        return ResponseEntity.ok(toDetail(updatedUser));
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
        List<User> users = service.findByNameContainingIgnoreCaseAndCompany(term, jwtService.getCompanyFromToken(auth));
        return ResponseEntity.ok(toDetails(users));
    }

    private UserResponse toDetail(User user) {
        return userMapper.toDetail(user, reportingService.userMetrics(user), service.findTechnologiesByUser(user));
    }

    private List<UserResponse> toDetails(List<User> users) {
        List<Long> userIds = users.stream().map(User::getId).toList();
        Map<Long, UserMetrics> metricsByUser = reportingService.userMetricsByIds(userIds);
        Map<Long, List<UserTechnologyResponse>> technologiesByUser = service.findTechnologiesByUsers(users);
        return users.stream()
                .map(user -> userMapper.toDetail(user, metricsByUser.get(user.getId()),
                        technologiesByUser.getOrDefault(user.getId(), List.of())))
                .toList();
    }
}
