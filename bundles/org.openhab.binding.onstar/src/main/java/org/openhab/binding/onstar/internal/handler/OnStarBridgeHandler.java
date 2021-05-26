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
package org.openhab.binding.onstar.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onstar.internal.api.OnStar.OnStarStatus;
import org.openhab.binding.onstar.internal.api.OnStarAPI;
import org.openhab.binding.onstar.internal.config.OnStarBridgeConfiguration;
import org.openhab.binding.onstar.internal.discovery.OnStarDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * The {@link OnStarBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Radner - Initial contribution
 */

@NonNullByDefault
public class OnStarBridgeHandler extends BaseBridgeHandler {
    // private final Logger logger = LoggerFactory.getLogger(OnStarBridgeHandler.class);

    private static OnStarAPI onStar = new OnStarAPI();

    public OnStarBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(OnStarDiscoveryService.class);
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void initialize() {
        OnStarBridgeConfiguration configuration = (OnStarBridgeConfiguration) getConfigAs(
                OnStarBridgeConfiguration.class);
        Map<String, String> properties = editProperties();
        if (!properties.containsKey("deviceId")) {
            properties.put("deviceId", UUID.randomUUID().toString());
            updateProperties(properties);
        }
        String deviceID = properties.get("deviceId");
        onStar.Init(configuration, (deviceID == null) ? "" : deviceID);
        this.scheduler.execute(() -> {
            if (onStar.Token() != OnStarStatus.VALIDATED) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Could Not Get Valid Token");
                return;
            }
            updateStatus(ThingStatus.ONLINE);
        });
    }

    public OnStarAPI getOnStarAPI() {
        return onStar;
    }
}
