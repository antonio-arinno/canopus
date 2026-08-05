package com.arinno.canopus.servicies;

import java.util.Base64;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.User;

@Service
public class JwtServiceImpl implements JwtService {

    private final UserService userService;

    public JwtServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Company getCompanyFromToken(String auth) {
        String[] chunks = auth.substring(7).split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        JSONObject jsonObject = new JSONObject(payload);
        Optional<User> optionalUser = userService.findByUsername(jsonObject.getString("username"));
        return optionalUser.orElseThrow().getCompany();
    }

    @Override
    public User getUserFromToken(String auth) {
        String[] chunks = auth.substring(7).split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        JSONObject jsonObject = new JSONObject(payload);
        return userService.findByUsername(jsonObject.getString("username")).get();
    }

}
