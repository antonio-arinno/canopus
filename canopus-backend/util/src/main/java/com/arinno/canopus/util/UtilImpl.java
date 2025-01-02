package com.arinno.canopus.util;

import java.util.Base64;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.arinno.canopus.entities.Company;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.servicies.UserService;

@Service
public class UtilImpl implements IUtil {

    @Autowired
    private UserService userService;

    @Override
    public Company getCompany(String auth) {
		String[] chunks = auth.substring(7).split("\\.");
		Base64.Decoder decoder = Base64.getUrlDecoder();
		String payload = new String(decoder.decode(chunks[1]));
		JSONObject jsonObject = new JSONObject(payload);
        Optional<User> optionalUser = userService.findByUsername(jsonObject.getString("username"));
        return optionalUser.orElseThrow().getCompany();
    }

    @Override
    public User getUser(String auth) {
		String[] chunks = auth.substring(7).split("\\.");
		Base64.Decoder decoder = Base64.getUrlDecoder();
		String payload = new String(decoder.decode(chunks[1]));
		JSONObject jsonObject = new JSONObject(payload);
		return userService.findByUsername(jsonObject.getString ("username")).get();  
    }	

}
