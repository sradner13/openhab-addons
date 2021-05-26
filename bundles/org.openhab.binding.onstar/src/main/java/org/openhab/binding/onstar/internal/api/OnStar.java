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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link OnStar} is responsible for creating the OnStar API JSON Web Configs
 *
 * @author Scott Radner - Initial contribution
 *         From the work of Gaël L'hopital - volvooncall
 */

@NonNullByDefault
public class OnStar {
    public enum OnStarStatus {
        INITIALIZED,
        EXPIRED,
        BAD_CONNECT,
        BAD_TOKEN,
        VALIDATED,
        UPGRADED;
    }

    public static class OAuthConfig {
        public String username = "";
        public String password = "";
        public String pin = "";
        public String clientid = "";
        public String clientsecret = "";
        public String deviceid = "";
        public long expiration = 0L;
        public Boolean upgraded = false;
    }

    public static class OAuthToken {
        public String access_token = "";
        public String token_type = "";
        public long expires_in = 0L;
        public String scope = "";
        public OnStarAccountInfo onstar_account_info = new OnStarAccountInfo();
        public UserInfo user_info = new UserInfo();
        public String id_token = "";
    }

    public static class OnStarAccountInfo {
        public String country_code = "";
        public String account_no = "";
    }

    public static class UserInfo {
        public String RemoteUserId = "";
        public String country = "";
    }

    public static class Subscriber {
        public SubscriberData subscriber = new SubscriberData();
        public ResponseError error = new ResponseError();
    }

    public static class Subscribers {
        public SubscriberList subscribers = new SubscriberList();
        public ResponseError error = new ResponseError();
    }

    public static class SubscriberList {
        public long size = 0;
        public SubscriberData[] subscriber = new SubscriberData[0];
    }

    public static class SubscriberData {
        public String id = "";
        public String fname = "";
        public String lname = "";
        public String gender = "";
        public String preferredLanguageCode = "";
        public String email = "";
        public String isDefaultPin = "";
        public String emergencyNotificationOptIn = "";
        public String phone = "";
        public String type = "";
        public String url = "";
    }

    public static class Vehicle {
        public VehicleData vehicle = new VehicleData();
        public ResponseError error = new ResponseError();
    }

    public static class Vehicles {
        public VehicleList vehicles = new VehicleList();
        public ResponseError error = new ResponseError();
    }

    public static class VehicleList {
        public long size = 0;
        public VehicleData[] vehicle = new VehicleData[0];
    }

    public static class VehicleData {
        public String vin = "";
        public String make = "";
        public String model = "";
        public String year = "";
        public String manufacturer = "";
        public String bodyStyle = "";
        public String phone = "";
        public String unitType = "";
        public String onstarStatus = "";
        public String url = "";
        public String isInPreActivation = "";
        public VehicleLicensePlate licensePlate = new VehicleLicensePlate();
        public VehicleIinsuranceInfo insuranceInfo = new VehicleIinsuranceInfo();
        public String enrolledInContinuousCoverage = "";
        public String propulsionType = "";
    }

    public static class VehicleLicensePlate {
        public String plateNumber = "";
        public String country = "";
        public String provinceOrStateCode = "";
    }

    public static class VehicleIinsuranceInfo {
        public String companyName = "";
        public VehicleInsuranceAgent insuranceAgent = new VehicleInsuranceAgent();
    }

    public static class VehicleInsuranceAgent {
        public VehicleInsuranceAgentPhone phone = new VehicleInsuranceAgentPhone();
    }

    public static class VehicleInsuranceAgentPhone {
        public String number = "";
        public String ext = "";
    }

    public static class AvailableCommands {
        public AvailableCommand commands = new AvailableCommand();
        public ResponseError error = new ResponseError();
    }

    public static class AvailableCommand {
        public CommandProperties[] command = new CommandProperties[0];
    }

    public static class CommandProperties {
        public String name = "";
        public String description = "";
        public String url = "";
        public Boolean isPrivSessionRequired = false;
        public CommandData commandData = new CommandData();
    }

    public static class CommandData {
        public SupportedDiagnostics supportedDiagnostics = new SupportedDiagnostics();
    }

    public static class SupportedDiagnostics {
        public String[] supportedDiagnostic = new String[0];
    }

    public static class CommandResponse {
        public CommandResponseList commandResponse = new CommandResponseList();
        public ResponseError error = new ResponseError();
    }

    public static class CommandResponseList {
        public String completionTime = ZonedDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern(ONSTAR_DATETIME_FORMAT));
        public String requestTime = ZonedDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern(ONSTAR_DATETIME_FORMAT));
        public String status = "";
        public String type = "";
        public String url = "";
        public CommandResponseBody body = new CommandResponseBody();
    }

    public static class CommandResponseBody {
        public DiagnosticResponse[] diagnosticResponse = new DiagnosticResponse[0];
        public LocationResponse location = new LocationResponse();
        public ResponseError error = new ResponseError();
    }

    public static class DiagnosticResponse {
        public String name = "";
        public DiagnosticElement[] diagnosticElement = new DiagnosticElement[0];
    }

    public static class LocationResponse {
        @SerializedName("lat")
        public String latitude = "";
        @SerializedName("long")
        public String longitude = "";
    }

    public static class RequestData {
        public @Nullable RequestDiagnostics diagnosticsRequest;
        public @Nullable RequestLockDoor lockDoorRequest;
        public @Nullable RequestUnlockDoor unlockDoorRequest;
        public @Nullable RequestAlert alertRequest;
    }

    public static class RequestAlert {
        public String[] action = new String[0];
        public String delay = "0";
        public String duration = "1";
        public String[] override = new String[] { "DoorOpen", "IgnitionOn" };
    }

    public static class RequestLockDoor {
        public String delay = "0";
    }

    public static class RequestUnlockDoor {
        public String delay = "0";
    }

    public static class RequestDiagnostics {
        public String[] diagnosticItem = new String[0];
    }

    public static class DiagnosticElement {
        public String message = "na";
        public String name = "";
        public String status = "";
        public String unit = "";
        public String value = "";
    }

    public static class ResponseError {
        public String code = "";
        public String description = "";
    }
}
