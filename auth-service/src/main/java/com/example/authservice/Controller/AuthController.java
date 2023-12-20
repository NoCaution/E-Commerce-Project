package com.example.authservice.Controller;

import com.example.authservice.Entity.Dto.AuthenticationRequestDto;
import com.example.authservice.Entity.Dto.AuthenticationResponseDto;
import com.example.authservice.Entity.Dto.RegistirationRequestDto;
import com.example.authservice.Service.AuthService;
import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Util.AppUtil;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AppUtil appUtil;


    @PostMapping("/register")
    public APIResponse register(@RequestBody RegistirationRequestDto request) {
        String BASKET_SERVICE_URL = "http://localhost:9000/api/basket/";
        AuthenticationResponseDto response = authService.register(request);
        if (response.isUserExists()) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "this email linked to another account."
            );
        }

        if (response.getToken() == null) {
            return new APIResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "token generate failed"
            );
        }

        //initialize user basket
        APIResponse basketServiceResponse = appUtil.sendRequest(Request.Post(BASKET_SERVICE_URL + "initializeLoggedInUserBasket"), response.getToken(), APIResponse.class);
        if(basketServiceResponse.getHttpStatus() != HttpStatus.CREATED){
            return new APIResponse(
                    HttpStatus.CREATED,
                    "user register success but initialize basket failed"
            );
        }

        return new APIResponse(
                HttpStatus.CREATED,
                "success"
        );
    }

    @PostMapping("/login")
    public APIResponse login(@RequestBody AuthenticationRequestDto request) {
        AuthenticationResponseDto response = authService.login(request);
        if (response.getToken() == null) {
            return new APIResponse(
                    HttpStatus.UNAUTHORIZED,
                    "login failed"
            );
        }

        return new APIResponse(
                HttpStatus.OK,
                "success",
                response
        );
    }
}
