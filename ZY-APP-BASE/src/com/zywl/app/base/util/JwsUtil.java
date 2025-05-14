package com.zywl.app.base.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Logger;

public class JwsUtil {

    public Jws<Claims> verifyJWT(String x5c, String jws){
        try {
            X509Certificate cert = getCert(x5c);
            if (!cert.getSubjectDN().getName().contains("Apple Inc")){
                return null;
            }
            return Jwts.parser().setSigningKey(cert.getPublicKey()).parseClaimsJws(jws);
        }catch (JwtException exc){
            return null;
        } catch (Exception exc){
            return null;
        }
    }

    public static X509Certificate getCert(String x5c) throws CertificateException {
        String stripped = x5c.replaceAll("-----BEGIN (.*)-----", "");
        stripped = stripped.replaceAll("-----END (.*)----", "");
        stripped = stripped.replaceAll("\r\n", "");
        stripped = stripped.replaceAll("\n", "");
        stripped.trim();
        byte[] keyBytes = Base64.getDecoder().decode(stripped);
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        return (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(keyBytes));
    }
}