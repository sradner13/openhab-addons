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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onstar.internal.api.OnStar.AvailableCommands;
import org.openhab.binding.onstar.internal.api.OnStar.CommandResponse;
import org.openhab.binding.onstar.internal.api.OnStar.OAuthConfig;
import org.openhab.binding.onstar.internal.api.OnStar.OAuthToken;
import org.openhab.binding.onstar.internal.api.OnStar.OnStarStatus;
import org.openhab.binding.onstar.internal.api.OnStar.Subscribers;
import org.openhab.binding.onstar.internal.api.OnStar.Vehicles;
import org.openhab.binding.onstar.internal.config.OnStarBridgeConfiguration;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link OnStarAPI} is responsible for creating the OnStar API JSON Web Configs
 *
 * @author Scott Radner - Initial contribution
 *         From the work of Gaël L'hopital - volvooncall
 */

@NonNullByDefault
public class OnStarAPI {
    private static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(20L);

    private static final Logger logger = LoggerFactory.getLogger(OnStarAPI.class);

    private static Gson gson = new Gson();

    private static Properties authHeader = new Properties();

    private static Properties upgradeHeader = new Properties();

    private static Properties dataHeader = new Properties();

    private static OAuthConfig onstarConfig = new OAuthConfig();

    private static @Nullable OAuthToken onstarToken = new OAuthToken();

    public void Init(OnStarBridgeConfiguration configuration, String deviceID) {
        onstarConfig.username = configuration.username;
        onstarConfig.password = configuration.password;
        onstarConfig.pin = configuration.pin;
        onstarConfig.clientid = configuration.clientid;
        onstarConfig.clientsecret = configuration.clientsecret;
        onstarConfig.deviceid = deviceID;
        onstarConfig.expiration = 0L;
        onstarConfig.upgraded = false;
        authHeader.put("Accept", AUTH_HEADER_ACCEPT);
        authHeader.put("Accept-Language", "en-US");
        authHeader.put("Content-Type", "text/plain");
        authHeader.put("Host", "api.gm.com");
        authHeader.put("Connection", "close");
        authHeader.put("Accept-Encoding", "br, gzip, deflate");
        authHeader.put("User-Agent", USER_AGENT);
        upgradeHeader.put("Accept", AUTH_HEADER_ACCEPT);
        upgradeHeader.put("Accept-Language", "en-US");
        upgradeHeader.put("Content-Type", "text/plain");
        upgradeHeader.put("Host", "api.gm.com");
        upgradeHeader.put("Connection", "close");
        upgradeHeader.put("Accept-Encoding", "br, gzip, deflate");
        upgradeHeader.put("User-Agent", USER_AGENT);
        dataHeader.put("Accept", "application/json");
        dataHeader.put("Accept-Language", "en-US");
        dataHeader.put("Content-Type", "application/json; charset=UTF-8");
        dataHeader.put("Content-Type", DATA_HEADER_TYPE);
        dataHeader.put("Host", "api.gm.com");
        dataHeader.put("Connection", "close");
        dataHeader.put("Accept-Encoding", "br, gzip, deflate");
        dataHeader.put("User-Agent", USER_AGENT);
    }

    public OnStarStatus Token() {
        if (onstarConfig.expiration > System.currentTimeMillis())
            return OnStarStatus.VALIDATED;
        String jwtrequest = JWT.createJWT(onstarConfig);
        String jwt = Request("POST", SERVICE_URL + URL_OAUTH_TOKEN, authHeader, jwtrequest, TEXT_CONTENT_TYPE,
                String.class);
        if (jwt == null)
            return OnStarStatus.BAD_TOKEN;
        String jsonInString = JWT.unpackJWT(onstarConfig, jwt);
        onstarToken = (OAuthToken) gson.fromJson(jsonInString, OAuthToken.class);
        onstarConfig.upgraded = false;
        onstarConfig.expiration = System.currentTimeMillis() + onstarToken.expires_in * 1000L;
        dataHeader.put("Authorization", "Bearer " + onstarToken.access_token);
        upgradeHeader.put("Authorization", "Bearer " + onstarToken.access_token);
        return OnStarStatus.VALIDATED;
    }

    public OnStarStatus Upgrade() {
        if (onstarConfig.upgraded)
            return OnStarStatus.UPGRADED;
        String jwtrequest = JWT.upgradeJWT(onstarConfig);
        String jwt = Request("POST", SERVICE_URL + URL_OAUTH_UPGRADE, upgradeHeader, jwtrequest, TEXT_CONTENT_TYPE,
                String.class);
        if (jwt == null)
            return OnStarStatus.BAD_TOKEN;
        onstarConfig.upgraded = true;
        return OnStarStatus.UPGRADED;
    }

    public @Nullable Subscribers Subscribers() {
        return Request("GET", SERVICE_URL + URL_SUBSCRIBERS, dataHeader, null, TEXT_CONTENT_TYPE, Subscribers.class);
    }

    public @Nullable Vehicles Vehicles() {
        return Request("GET", SERVICE_URL + URL_VEHICLES, dataHeader, null, TEXT_CONTENT_TYPE, Vehicles.class);
    }

    public @Nullable AvailableCommands Commands(String vin) {
        return Request("GET", SERVICE_URL + String.format(URL_COMMANDS, new Object[] { vin }), dataHeader, null,
                TEXT_CONTENT_TYPE, AvailableCommands.class);
    }

    public @Nullable CommandResponse Command(String url, String data, Boolean wait) {
        CommandResponse response = new CommandResponse();
        response = Request("POST", url, dataHeader, data, JSON_CONTENT_TYPE, CommandResponse.class);
        while (wait && response != null && response.commandResponse.status.equals("inProgress")) {
            try {
                TimeUnit.SECONDS.sleep(10L);
                response = Request("GET", response.commandResponse.url, dataHeader, null, JSON_CONTENT_TYPE,
                        CommandResponse.class);
            } catch (InterruptedException e) {
                logger.error("InterruptedException {}", e.getMessage());
                return null;
            }
        }
        return response;
    }

    private @Nullable <T> T Request(String method, String url, Properties httpHeader, @Nullable String data,
            String type, Class<T> objectClass) {
        InputStream content = (data == null) ? null : new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        try {
            String jsonResponse = HttpUtil.executeUrl(method, url, httpHeader, content, type, REQUEST_TIMEOUT);
            logger.trace("{} {}", method, url);
            logger.trace("REQ {}", data);
            logger.trace("RSP {}", jsonResponse);
            T response = (T) gson.fromJson(jsonResponse, objectClass);
            if (objectClass == String.class)
                return (response == null) ? objectClass.cast("") : response;
            return response;
        } catch (IOException e) {
            logger.error("IOException {}", e.getMessage());
            return null;
        } catch (JsonSyntaxException e) {
            logger.error("JsonSyntaxException {}", e.getMessage());
            return null;
        }
    }
}
