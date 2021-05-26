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
package org.openhab.binding.onstar.internal.discovery;

import static org.openhab.binding.onstar.internal.OnStarBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onstar.internal.OnStarBindingConstants;
import org.openhab.binding.onstar.internal.api.OnStar;
import org.openhab.binding.onstar.internal.api.OnStarAPI;
import org.openhab.binding.onstar.internal.handler.OnStarBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * The {@link OnStarDiscoveryService} is responsible for discovery of Vehicles and Subscribers
 *
 * @author Scott Radner - Initial contribution
 *         From the work of Gaël L'hopital - volvooncall
 */

@NonNullByDefault
public class OnStarDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    // private final Logger logger = LoggerFactory.getLogger(OnStarDiscoveryService.class);

    private @Nullable OnStarBridgeHandler handler;

    public OnStarDiscoveryService() {
        super(OnStarBindingConstants.SUPPORTED_THING_TYPES_UIDS, 2, true);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof OnStarBridgeHandler) {
            this.handler = (OnStarBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        OnStarBridgeHandler bridgeHandler = this.handler;
        if (bridgeHandler == null)
            return;
        OnStarAPI onStar = bridgeHandler.getOnStarAPI();
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        if (onStar.Token() != OnStar.OnStarStatus.VALIDATED)
            return;
        OnStar.Subscribers onstarSubscribers = onStar.Subscribers();
        if (onstarSubscribers != null && onstarSubscribers.subscribers.size > 0L) {
            for (OnStar.SubscriberData subscriber : onstarSubscribers.subscribers.subscriber) {
                thingDiscovered(DiscoveryResultBuilder
                        .create(new ThingUID(OnStarBindingConstants.SUBSCRIBER_THING_TYPE, bridgeUID,
                                String.valueOf(subscriber.id.hashCode())))
                        .withLabel(String.valueOf(subscriber.fname) + " " + subscriber.lname)
                        .withBridge(this.handler.getThing().getUID()).withProperty(SUBSCRIBER_ID, subscriber.id)
                        .withProperty(SUBSCRIBER_NAME, String.valueOf(subscriber.fname) + " " + subscriber.lname)
                        .withProperty(SUBSCRIBER_GENDER, subscriber.gender)
                        .withProperty(SUBSCRIBER_PLC, subscriber.preferredLanguageCode)
                        .withProperty(SUBSCRIBER_EMAIL, subscriber.email)
                        .withProperty(SUBSCRIBER_ISDP, subscriber.isDefaultPin)
                        .withProperty(SUBSCRIBER_ENOPTIN, subscriber.emergencyNotificationOptIn)
                        .withProperty(SUBSCRIBER_PHONE, subscriber.phone).withProperty(SUBSCRIBER_TYPE, subscriber.type)
                        .withRepresentationProperty(SUBSCRIBER_ID).build());
            }
        }
        OnStar.Vehicles onstarVehicles = onStar.Vehicles();
        if (onstarVehicles != null && onstarVehicles.vehicles.size > 0L) {
            for (OnStar.VehicleData vehicle : onstarVehicles.vehicles.vehicle) {
                thingDiscovered(DiscoveryResultBuilder
                        .create(new ThingUID(OnStarBindingConstants.VEHICLE_THING_TYPE, bridgeUID, vehicle.vin))
                        .withLabel(String.valueOf(vehicle.year) + " " + vehicle.make + " " + vehicle.model)
                        .withBridge(this.handler.getThing().getUID()).withProperty(VEHICLE_VIN, vehicle.vin)
                        .withProperty(VEHICLE_MAKE, vehicle.make).withProperty(VEHICLE_MODEL, vehicle.model)
                        .withProperty(VEHICLE_YEAR, vehicle.year).withProperty(VEHICLE_MNFCTR, vehicle.manufacturer)
                        .withProperty(VEHICLE_BODY, vehicle.bodyStyle).withProperty(VEHICLE_PHONE, vehicle.phone)
                        .withProperty(VEHICLE_UNIT_TYPE, vehicle.unitType)
                        .withProperty(VEHICLE_STATUS, vehicle.onstarStatus)
                        .withProperty(VEHICLE_ISPA, vehicle.isInPreActivation)
                        .withProperty(VEHICLE_PLATE,
                                String.valueOf(vehicle.licensePlate.provinceOrStateCode) + " "
                                        + vehicle.licensePlate.plateNumber)
                        .withProperty(VEHICLE_INSURANCE, vehicle.insuranceInfo.companyName)
                        .withProperty(VEHICLE_ECC, vehicle.enrolledInContinuousCoverage)
                        .withProperty(VEHICLE_PROPULSION, vehicle.propulsionType)
                        .withRepresentationProperty(VEHICLE_VIN).build());
            }
        }
        stopScan();
    }
}
