package quyet.learn.spring.service;

import com.nimbusds.jose.JOSEException;
import org.springframework.stereotype.Service;
import quyet.learn.spring.dto.request.auth.AuthenticationRequest;
import quyet.learn.spring.dto.request.auth.IntrospectRequest;
import quyet.learn.spring.dto.response.auth.AuthenticationResponse;
import quyet.learn.spring.dto.response.auth.IntrospectResponse;

import java.text.ParseException;

@Service
public interface AuthService {
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);

    public IntrospectResponse introspect(IntrospectRequest introspectRequest) throws JOSEException, ParseException;
}
