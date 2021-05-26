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
package org.openhab.binding.onstar.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onstar.internal.handler.OnStarBridgeHandler;
import org.openhab.binding.onstar.internal.handler.SubscriberHandler;
import org.openhab.binding.onstar.internal.handler.VehicleHandler;
import org.openhab.binding.onstar.internal.handler.VehicleStateDescriptionProvider;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * The {@link OnStarHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Scott Radner - Initial contribution
 */

@NonNullByDefault
@Component(configurationPid = "binding.onstar", service = ThingHandlerFactory.class)
public class OnStarHandlerFactory extends BaseThingHandlerFactory {
    // private Logger logger = LoggerFactory.getLogger(OnStarHandlerFactory.class);

    private final VehicleStateDescriptionProvider stateDescriptionProvider;
    @Reference
    @Nullable
    UnitProvider unitProvider;
    @Reference
    @Nullable
    ItemChannelLinkRegistry itemChannelLinkRegistry;

    @Activate
    public OnStarHandlerFactory(@Reference VehicleStateDescriptionProvider provider) {
        this.stateDescriptionProvider = provider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return OnStarBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (OnStarBindingConstants.APIBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            OnStarBridgeHandler bridgeHandler = new OnStarBridgeHandler((Bridge) thing);
            return (ThingHandler) bridgeHandler;
        }
        if (OnStarBindingConstants.VEHICLE_THING_TYPE.equals(thingTypeUID))
            return (ThingHandler) new VehicleHandler(thing, stateDescriptionProvider, itemChannelLinkRegistry,
                    unitProvider);
        if (OnStarBindingConstants.SUBSCRIBER_THING_TYPE.equals(thingTypeUID))
            return (ThingHandler) new SubscriberHandler(thing, stateDescriptionProvider);
        return null;
    }
}
