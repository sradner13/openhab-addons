/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.onstar.internal.api;

import static org.openhab.binding.onstar.internal.OnStarBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.openhab.binding.onstar.internal.api.OnStar.OAuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JWT} is responsible for creating the OnStar API JSON Web Configs
 *
 * @author Scott Radner - Initial contribution
 *         From the work of Gaël L'hopital - volvooncall
 */

@NonNullByDefault
public class JWT {

    private static final Logger logger = LoggerFactory.getLogger(JWT.class);

    public static String createJWT(OAuthConfig onstarConfig) {

        JwtClaims claims = new JwtClaims();
        claims.setClaim(CLAIM_CLIENT_ID, onstarConfig.clientid);
        claims.setClaim(CLAIM_DEVICE_ID, onstarConfig.deviceid);
        claims.setClaim(CLAIM_GRANT_TYPE, ONSTAR_VALUE_PASSWORD);
        claims.setClaim(CLAIM_NONCE, Nonce(26));
        claims.setClaim(CLAIM_PASSWORD, onstarConfig.password);
        claims.setClaim(CLAIM_SCOPE, ONSTAR_VALUE_SCOPE);
        claims.setClaim(CLAIM_TIMESTAMP,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
        claims.setClaim(CLAIM_USERNAME, onstarConfig.username);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setDoKeyValidation(false);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setHeader("typ", "JWT");
        jws.setKey(new HmacKey(onstarConfig.clientsecret.getBytes(StandardCharsets.UTF_8)));

        String keystring = "No JWT Yet";

        try {
            keystring = jws.getCompactSerialization();
        } catch (JoseException e) {
            logger.error("JoseException {}", e.getMessage());
        }

        return keystring;
    }

    public static String unpackJWT(OAuthConfig onstarConfig, String keystring) {

        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRelaxVerificationKeyValidation()
                .setVerificationKey(new HmacKey(onstarConfig.clientsecret.getBytes(StandardCharsets.UTF_8))).build();

        JwtClaims claims = new JwtClaims();

        try {
            claims = jwtConsumer.processToClaims(keystring);
        } catch (InvalidJwtException e) {
            logger.error("JoseException {}", e.getMessage());
        }

        return claims.toJson();
    }

    public static String upgradeJWT(OAuthConfig onstarConfig) {

        JwtClaims claims = new JwtClaims();
        claims.setClaim(CLAIM_CLIENT_ID, onstarConfig.clientid);
        claims.setClaim(CLAIM_DEVICE_ID, onstarConfig.deviceid);
        claims.setClaim(CLAIM_GRANT_TYPE, ONSTAR_VALUE_PASSWORD);
        claims.setClaim(CLAIM_CREDENTAIL, onstarConfig.pin);
        claims.setClaim(CLAIM_CREDENTAIL_TYPE, ONSTAR_VALUE_PIN);
        claims.setClaim(CLAIM_NONCE, Nonce(26));
        claims.setClaim(CLAIM_TIMESTAMP,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(ONSTAR_DATETIME_FORMAT)));

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setDoKeyValidation(false);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setHeader("typ", "JWT");
        jws.setKey(new HmacKey(onstarConfig.clientsecret.getBytes(StandardCharsets.UTF_8)));

        String keystring = "No JWT Yet";

        try {
            keystring = jws.getCompactSerialization();
        } catch (JoseException e) {
            logger.error("JoseException {}", e.getMessage());
        }

        return keystring;
    }

    private static String Nonce(int targetStringLength) {
        Random random = new Random();

        return random.ints(97, 123).limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }
}
