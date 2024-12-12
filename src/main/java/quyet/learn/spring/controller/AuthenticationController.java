package quyet.learn.spring.controller;

import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import quyet.learn.spring.dto.request.auth.AuthenticationRequest;
import quyet.learn.spring.dto.request.auth.IntrospectRequest;
import quyet.learn.spring.dto.request.auth.LogoutRequest;
import quyet.learn.spring.dto.request.auth.RefreshRequest;
import quyet.learn.spring.dto.response.ApiResponse;
import quyet.learn.spring.dto.response.auth.AuthenticationResponse;
import quyet.learn.spring.dto.response.auth.IntrospectResponse;
import quyet.learn.spring.service.AuthService;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthService authService;

    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> login(@RequestBody final AuthenticationRequest authenticationRequest) {
        var result = authService.authenticate(authenticationRequest);
        return ApiResponse.<AuthenticationResponse>builder()

                .data(result)
                .build();
    }


    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .data(result)
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> login(@RequestBody final IntrospectRequest request) throws ParseException, JOSEException {
        var result = authService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .data(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody final LogoutRequest request) throws ParseException, JOSEException {
        authService.logout(request);
        return ApiResponse.<Void>builder()

                .build();
    }
}
