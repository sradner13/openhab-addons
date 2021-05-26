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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onstar.internal.config.SubscriberConfiguration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * The {@link OnStarSubscriberHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Radner - Initial contribution
 */

@NonNullByDefault
public class SubscriberHandler extends BaseThingHandler {
    // private final Logger logger = LoggerFactory.getLogger(SubscriberHandler.class);

    @NonNullByDefault({})
    private SubscriberConfiguration configuration;

    public SubscriberHandler(Thing thing, VehicleStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }
}
