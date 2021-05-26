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

import static org.openhab.binding.onstar.internal.OnStarBindingConstants.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onstar.internal.api.OnStar.*;
import org.openhab.binding.onstar.internal.api.OnStarAPI;
import org.openhab.binding.onstar.internal.config.VehicleConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.link.AbstractLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link OnStarVehicleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Radner - Initial contribution
 */

@NonNullByDefault
public class VehicleHandler extends BaseThingHandler {
    private static Map<String, CommandProperties> commandMap = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    private @Nullable VehicleConfiguration configuration;
    private @Nullable ScheduledFuture<?> refreshDiagnostics;
    private @Nullable ScheduledFuture<?> refreshLocation;
    private @Nullable ItemChannelLinkRegistry itemChannelLinkRegistry;
    private @Nullable UnitProvider unitProvider;

    public VehicleHandler(Thing thing, VehicleStateDescriptionProvider stateDescriptionProvider,
            @Nullable ItemChannelLinkRegistry itemChannelLinkRegistry, @Nullable UnitProvider unitProvider) {
        super(thing);
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.unitProvider = unitProvider;
    }

    public void initialize() {
        OnStarBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null)
            return;
        OnStarAPI onStar = bridgeHandler.getOnStarAPI();
        configuration = (VehicleConfiguration) getConfigAs(VehicleConfiguration.class);
        scheduler.execute(() -> {
            if (onStar.Token() != OnStarStatus.VALIDATED) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Could Not Get Valid Token");
                return;
            }
            AvailableCommands commands = onStar.Commands(configuration.vin);
            if (commands == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not Get Command List from Server");
                return;
            }
            String supportedCommands = "";
            String supportedDiagnostics = "";
            for (CommandProperties command : commands.commands.command) {
                commandMap.put(command.name, command);
                supportedCommands = String.valueOf(supportedCommands) + command.name + ", ";
                if (command.name.equals(ONSTAR_COMMAND_DIAGNOSTICS)) {
                    for (String diagnostic : command.commandData.supportedDiagnostics.supportedDiagnostic) {
                        supportedDiagnostics = String.valueOf(supportedDiagnostics) + diagnostic + ", ";
                    }
                }
            }
            Map<String, String> properties = editProperties();
            properties.put(VEHICLE_SUPPORTED_COMMANDS, supportedCommands.substring(0, supportedCommands.length() - 2));
            properties.put(VEHICLE_SUPPORTED_DIAGNOSTICS,
                    supportedDiagnostics.substring(0, supportedDiagnostics.length() - 2));
            updateProperties(properties);
            updateStatus(ThingStatus.ONLINE);
            if (configuration.refreshdiagnostics.intValue() != 0
                    && (refreshDiagnostics == null || refreshDiagnostics.isCancelled()))
                refreshDiagnostics = scheduler.scheduleWithFixedDelay(this::refreshDiagnostics, 0L,
                        (1440 / configuration.refreshdiagnostics.intValue()), TimeUnit.MINUTES);
            if (configuration.refreshlocation.intValue() != 0
                    && (refreshLocation == null || refreshLocation.isCancelled()))
                refreshLocation = scheduler.scheduleWithFixedDelay(this::refreshLocation, 0L,
                        (1440 / configuration.refreshlocation.intValue()), TimeUnit.MINUTES);
        });
    }

    public void dispose() {
        if (this.refreshDiagnostics != null)
            this.refreshDiagnostics.cancel(true);
        if (this.refreshLocation != null)
            this.refreshLocation.cancel(true);
        super.dispose();
    }

    private void refreshDiagnostics() {
        SendCommand(CHANNELID_DIAGNOSTICS, ONSTAR_COMMAND_DIAGNOSTICS);
    }

    private void refreshLocation() {
        SendCommand(CHANNELID_LOCATION, ONSTAR_COMMAND_LOCATION);
    }

    void updateIfMatches(String id) {
        id.equalsIgnoreCase(this.configuration.vin);
    }

    private @Nullable OnStarBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null)
                return (OnStarBridgeHandler) handler;
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        return null;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(OnStarActions.class);
    }

    public String actionSendCommand(String commandName, String payload) {
        Gson gson = new Gson();
        CommandResponse errorResponse = new CommandResponse();
        errorResponse.error.code = "HANDLER";
        if (!commandMap.containsKey(commandName)) {
            errorResponse.error.description = "Command Not Found";
            return gson.toJson(errorResponse);
        }
        OnStarBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            errorResponse.error.description = "Could Not Get Bridge Handler";
            return gson.toJson(errorResponse);
        }
        OnStarAPI onStar = bridgeHandler.getOnStarAPI();
        if (onStar.Token() != OnStarStatus.VALIDATED) {
            errorResponse.error.description = "Could Not Get Valid Token";
            return gson.toJson(errorResponse);
        }
        CommandProperties command = commandMap.get(ONSTAR_COMMAND_CONNECT);
        CommandResponse response = onStar.Command(command.url, "{}", false);
        command = commandMap.get(commandName);
        if (command.isPrivSessionRequired && onStar.Upgrade() != OnStarStatus.UPGRADED) {
            errorResponse.error.description = "Could Not Upgrade Token";
            return gson.toJson(errorResponse);
        }
        response = onStar.Command(command.url, payload, true);
        if (response == null) {
            errorResponse.error.description = "Null Response from Server";
            return gson.toJson(errorResponse);
        }
        return gson.toJson(response);
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelID = channelUID.getId();
        if (command instanceof org.openhab.core.types.RefreshType)
            return;
        if (command instanceof StringType)
            scheduler.execute(() -> SendCommand(channelID, command.toString()));
    }

    private void SendCommand(String channelID, String commandName) {
        OnStarBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null)
            return;
        OnStarAPI onStar = bridgeHandler.getOnStarAPI();
        Gson gson = new Gson();
        updateState(String.valueOf(channelID) + "-message", (State) new StringType("Initiated"));
        if (onStar.Token() != OnStarStatus.VALIDATED) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Could Not Get Valid Token");
            return;
        }
        CommandProperties command = commandMap.get(ONSTAR_COMMAND_CONNECT);
        CommandResponse response = onStar.Command(command.url, "{}", false);
        RequestData requestData = new RequestData();
        switch (commandName) {
            case ONSTAR_COMMAND_UNLOCK:
                command = commandMap.get(ONSTAR_COMMAND_UNLOCK);
                requestData.unlockDoorRequest = new RequestUnlockDoor();
                break;
            case ONSTAR_COMMAND_DIAGNOSTICS:
                command = commandMap.get(ONSTAR_COMMAND_DIAGNOSTICS);
                requestData.diagnosticsRequest = new RequestDiagnostics();
                requestData.diagnosticsRequest.diagnosticItem = command.commandData.supportedDiagnostics.supportedDiagnostic;
                break;
            case ONSTAR_COMMAND_ALERT_BOTH:
                command = commandMap.get(ONSTAR_COMMAND_ALERT);
                requestData.alertRequest = new RequestAlert();
                requestData.alertRequest.action = new String[] { "Honk", "Flash" };
                break;
            case ONSTAR_COMMAND_ALERT_HONK:
                command = commandMap.get(ONSTAR_COMMAND_ALERT);
                requestData.alertRequest = new RequestAlert();
                requestData.alertRequest.action = new String[] { "Honk" };
                break;
            case ONSTAR_COMMAND_ALERT_FLASH:
                command = commandMap.get(ONSTAR_COMMAND_ALERT);
                requestData.alertRequest = new RequestAlert();
                requestData.alertRequest.action = new String[] { "Flash" };
                break;
            case ONSTAR_COMMAND_LOCK:
                command = commandMap.get(ONSTAR_COMMAND_LOCK);
                requestData.lockDoorRequest = new RequestLockDoor();
                break;
            default:
                command = commandMap.get(commandName);
                break;
        }
        if (command == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown Command");
            return;
        }
        if (command.isPrivSessionRequired && onStar.Upgrade() != OnStarStatus.UPGRADED) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Could Not Upgrade Token");
            return;
        }
        updateState(String.valueOf(channelID) + "-message", (State) new StringType("Waiting"));
        response = onStar.Command(command.url, gson.toJson(requestData), true);
        if (response == null) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR);
            updateState(String.valueOf(channelID) + "-message",
                    (State) new StringType("Error:Invalid Server Response"));
            return;
        }
        if (!response.commandResponse.status.equals("success")) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR,
                    response.commandResponse.body.error.description);
            updateState(String.valueOf(channelID) + "-message",
                    (State) new StringType("Error:" + TimeOut(response.commandResponse.completionTime) + " : "
                            + response.commandResponse.body.error.description));
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        updateState(String.valueOf(channelID) + "-message",
                (State) new StringType("Okay:" + TimeOut(response.commandResponse.completionTime)));
        switch (commandName) {
            case ONSTAR_COMMAND_DIAGNOSTICS:
                SaveDiagnostics(response);
                return;
            case ONSTAR_COMMAND_LOCATION:
                updateState("location#location",
                        (State) new PointType(new StringType(response.commandResponse.body.location.latitude),
                                new StringType(response.commandResponse.body.location.longitude)));
                return;
        }
        updateState(channelID, (State) new StringType(commandName));
    }

    private String TimeOut(String timein) {
        return LocalDateTime.ofInstant(Instant.parse(timein), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("E, MMM dd, h:mm:ss a"));
    }

    private void SaveDiagnostics(CommandResponse response) {
        String category = "";
        String name = "";
        Map<String, ChannelUID> channelMap = new HashMap<>();
        if (configuration.unlink) {
            getThing().getChannels().stream().map(chnl -> chnl.getUID())
                    .filter(uid -> !uid.getGroupId().equals("commands"))
                    .filter(uid -> !uid.getGroupId().equals("location"))
                    .forEach(uid -> channelMap.put(uid.getId(), uid));
        }
        for (DiagnosticResponse diagResp : response.commandResponse.body.diagnosticResponse) {
            category = diagResp.name.toLowerCase().replaceAll("\\s+", "");
            for (DiagnosticElement diagItem : diagResp.diagnosticElement) {
                name = diagItem.name.toLowerCase().replaceAll("\\s+", "");
                if (diagItem.value != "") {
                    vehicleUpdateState(String.valueOf(category) + "#" + name, diagItem.value, diagItem.unit);
                    channelMap.remove(String.valueOf(category) + "#" + name);
                }
                if (diagItem.message != "na") {
                    vehicleUpdateState(String.valueOf(category) + "#" + name + "-message", diagItem.message, "");
                    channelMap.remove(String.valueOf(category) + "#" + name + "-message");
                }
            }
        }
        if (configuration.unlink) {
            logger.info("Attempting to Unlink {} entries", channelMap.size());
            channelMap.values().stream().forEach(uid -> itemChannelLinkRegistry.getLinkedItemNames(uid).stream()
                    .forEach(nm -> itemChannelLinkRegistry.remove(AbstractLink.getIDFor(nm, uid))));
            configuration.unlink = false;
            Configuration newconfiguration = editConfiguration();
            newconfiguration.put("unlink", "false");
            updateConfiguration(newconfiguration);
        }
    }

    private void vehicleUpdateState(String channelID, String value, String unit) {
        State state;
        if (!isLinked(channelID))
            return;
        switch (unit.toLowerCase()) {
            case "":
            case "n/a":
            case "stat":
                state = new StringType(value);
                break;
            case "%":
                state = new QuantityType<>(Double.valueOf(value), Units.PERCENT);
                break;
            case "l":
                state = new QuantityType<>(Double.valueOf(value), Units.LITRE);
                break;
            case "km":
                state = isImperial() ? new QuantityType<>(Double.valueOf(value) * 0.621371, ImperialUnits.MILE)
                        : new QuantityType<>(Double.valueOf(value), MetricPrefix.KILO(SIUnits.METRE));
                break;
            case "cel":
                state = new QuantityType<>(Double.valueOf(value), SIUnits.CELSIUS);
                break;
            case "kpa":
                state = new QuantityType<>(Double.valueOf(value) * (isImperial() ? 0.145038 : 1), Units.ONE);
                break;
            case "kwh":
                state = new QuantityType<>(Double.valueOf(value), Units.KILOWATT_HOUR);
                break;
            case "rpm":
                state = new QuantityType<>(Double.valueOf(value), Units.ONE);
                break;
            case "kmpl":
            case "kmple":
                state = new QuantityType<>(Double.valueOf(value) * (isImperial() ? 2.35215 : 1), Units.ONE);
                break;
            case "volts":
                state = new QuantityType<>(Double.valueOf(value), Units.VOLT);
                break;
            default:
                logger.info("Unknown unit {}", unit);
                state = new StringType(value);
        }
        updateState(channelID, state);
    }

    private boolean isImperial() {
        return unitProvider.getMeasurementSystem() instanceof ImperialUnits ? true : false;
    }
}
