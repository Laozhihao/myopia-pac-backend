package com.wupol.myopia.oauth.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JWTUtilTest {

    @Test
    public void generateToken() throws JOSEException, ParseException {

        // String payload = "{\"I\":\"Hello, World\"}";
        String payload = "Hello, World";

        // Generate random 256-bit (32-byte) shared secret
        // SecureRandom random = new SecureRandom();
        // byte[] sharedSecret = new byte[32];
        // random.nextBytes(sharedSecret);

        byte[] sharedSecret = "JaNdRgUkXp2s5v8y/B?E(H+MbPeShVmY".getBytes();

        // Create HMAC signer
        JWSSigner signer = new MACSigner(sharedSecret);

        // Prepare JWS object with "Hello, world!" payload
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(payload));

        // Apply the HMAC
        jwsObject.sign(signer);

        // To serialize to compact form, produces something like
        // eyJhbGciOiJIUzI1NiJ9.SGVsbG8sIHdvcmxkIQ.onO9Ihudz3WkiauDO2Uhyuz0Y18UASXlSc1eS0NkWyA
        String s = jwsObject.serialize();

        // To parse the JWS and verify it, e.g. on client-side
        jwsObject = JWSObject.parse(s);

        JWSVerifier verifier = new MACVerifier(sharedSecret);

        assertTrue(jwsObject.verify(verifier));

        assertEquals(payload, jwsObject.getPayload().toString());
    }

}
