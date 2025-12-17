package com.mdd.auth.services;

import com.mdd.auth.entities.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

/**
 * Émet des access tokens JWT signés en RSA (clé privée générée par {@link JwtKeysService}).
 * Les services "resource" valident via JWKS publié par l'auth-service.
 */
@Service
public class JwtTokenService {

    private final JwtKeysService jwtKeysService;

    private final String issuer;
    private final long accessTokenExpirationSeconds;

    public JwtTokenService(
            JwtKeysService jwtKeysService,
            @Value("${app.issuer:http://localhost:8081}") String issuer,
            @Value("${app.jwt.access-token-expiration:900}") long accessTokenExpirationSeconds
    ) {
        this.jwtKeysService = jwtKeysService;
        this.issuer = issuer;
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenExpirationSeconds);

        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject(user.getEmail())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .claim("id", user.getId().toString())
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(jwtKeysService.getRsaJwk().getKeyID())
                            .build(),
                    claims
            );

            RSASSASigner signer = new RSASSASigner(jwtKeysService.getRsaJwk().toPrivateKey());
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Impossible de signer le JWT", e);
        }
    }
}
