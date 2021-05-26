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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OnStarBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Scott Radner - Initial contribution
 */
@NonNullByDefault
public class OnStarBindingConstants {
    public static final String BINDING_ID = "onstar";

    public static final String VIN = "vin";
    public static final String ID = "id";

    public static final String SERVICE_URL = "https://api.gm.com/api/v1/";
    public static final String DATA_HEADER_TYPE = "application/json";
    public static final String AUTH_HEADER_ACCEPT = "application/json";
    public static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 9; en-US; Google Pixel 2 Build/PI)";

    // public static final String SERVICE_URL = "http://192.168.1.45:4010/";
    // public static final String DATA_HEADER_TYPE = "application/text";
    // public static final String AUTH_HEADER_ACCEPT = "application/text";

    public static final String URL_OAUTH_TOKEN = "oauth/token";
    public static final String URL_OAUTH_UPGRADE = "oauth/token/upgrade";
    public static final String URL_SUBSCRIBERS = "account/subscribers";
    public static final String URL_VEHICLES = "account/vehicles";
    public static final String URL_COMMANDS = "account/vehicles/%s/commands";

    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID("onstar", "onstarapi");
    public static final ThingTypeUID VEHICLE_THING_TYPE = new ThingTypeUID("onstar", "vehicle");
    public static final ThingTypeUID SUBSCRIBER_THING_TYPE = new ThingTypeUID("onstar", "subscriber");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = (Set<ThingTypeUID>) Stream
            .<ThingTypeUID> of(new ThingTypeUID[] { APIBRIDGE_THING_TYPE, VEHICLE_THING_TYPE, SUBSCRIBER_THING_TYPE })
            .collect(Collectors.toSet());

    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String TEXT_CONTENT_TYPE = "text/plain";

    public static final String HEADER_CONNECTION = "Connection";

    public static final String SUBSCRIBER_ID = "id";
    public static final String SUBSCRIBER_NAME = "Name";
    public static final String SUBSCRIBER_GENDER = "Gender ";
    public static final String SUBSCRIBER_PLC = "Preferred Language Code";
    public static final String SUBSCRIBER_EMAIL = "Email";
    public static final String SUBSCRIBER_ISDP = "Is Default Pin";
    public static final String SUBSCRIBER_ENOPTIN = "Emergency Notification Opt-In";
    public static final String SUBSCRIBER_PHONE = "Phone";
    public static final String SUBSCRIBER_TYPE = "Type";

    public static final String VEHICLE_VIN = "vin";
    public static final String VEHICLE_MAKE = "Make";
    public static final String VEHICLE_MODEL = "Model";
    public static final String VEHICLE_YEAR = "Year";
    public static final String VEHICLE_MNFCTR = "Manufacturer";
    public static final String VEHICLE_BODY = "Body Style";
    public static final String VEHICLE_PHONE = "Phone";
    public static final String VEHICLE_UNIT_TYPE = "Unit Type";
    public static final String VEHICLE_STATUS = "Onstar Status";
    public static final String VEHICLE_ISPA = "Is In Pre-Activation";
    public static final String VEHICLE_PLATE = "License Plate";
    public static final String VEHICLE_INSURANCE = "Insurance Info";
    public static final String VEHICLE_ECC = "Enrolled In Continuous Coverage";
    public static final String VEHICLE_PROPULSION = "Propulsion Type";
    public static final String VEHICLE_SUPPORTED_COMMANDS = "Supported Commands";
    public static final String VEHICLE_SUPPORTED_DIAGNOSTICS = "Supported Diagnostics";

    public static final String CONF_PASSWORD = "password";
    public static final String PROP_DEVICE_ID = "deviceId";

    public static final String CLAIM_CLIENT_ID = "client_id";
    public static final String CLAIM_DEVICE_ID = "device_id";
    public static final String CLAIM_GRANT_TYPE = "grant_type";
    public static final String CLAIM_NONCE = "nonce";
    public static final String CLAIM_PASSWORD = "password";
    public static final String CLAIM_SCOPE = "scope";
    public static final String CLAIM_TIMESTAMP = "timestamp";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_CREDENTAIL = "credential";
    public static final String CLAIM_CREDENTAIL_TYPE = "credential_type";

    public static final String ONSTAR_VALUE_PASSWORD = "password";
    public static final String ONSTAR_VALUE_PIN = "PIN";
    public static final String ONSTAR_VALUE_SCOPE = "onstar gmoc commerce user_trailer";
    public static final String ONSTAR_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String CHANNELID_DIAGNOSTICS = "commands#diagnostics";
    public static final String CHANNELID_LOCATION = "commands#location";
    public static final String CHANNELID_REMOTESTART = "commands#remotestart";
    public static final String CHANNELID_VEHICLEACCESS = "commands#vehicleaccess";
    public static final String CHANNELID_VEHICLEALERT = "commands#vehiclealert";

    public static final String ONSTAR_COMMAND_CONNECT = "connect";
    public static final String ONSTAR_COMMAND_HEAT = "start";
    public static final String ONSTAR_COMMAND_NOHEAT = "cancelStart";
    public static final String ONSTAR_COMMAND_LOCK = "lockDoor";
    public static final String ONSTAR_COMMAND_UNLOCK = "unlockDoor";
    public static final String ONSTAR_COMMAND_ALERT = "alert";
    public static final String ONSTAR_COMMAND_ALERT_HONK = "honk";
    public static final String ONSTAR_COMMAND_ALERT_FLASH = "flash";
    public static final String ONSTAR_COMMAND_ALERT_BOTH = "both";
    public static final String ONSTAR_COMMAND_SILENCE = "cancelAlert";
    public static final String ONSTAR_COMMAND_DIAGNOSTICS = "diagnostics";
    public static final String ONSTAR_COMMAND_LOCATION = "location";
}
