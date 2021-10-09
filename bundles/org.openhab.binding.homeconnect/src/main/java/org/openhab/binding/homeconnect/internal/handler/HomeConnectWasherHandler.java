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
package org.openhab.binding.homeconnect.internal.handler;

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectWasherHandler} is responsible for handling commands, which are
 * sent to one of the channels of a washing machine.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectWasherHandler extends AbstractHomeConnectThingHandler {

    private static final List<String> INACTIVE_STATE = Arrays.asList(OPERATION_STATE_INACTIVE, OPERATION_STATE_READY);

    private final Logger logger = LoggerFactory.getLogger(HomeConnectWasherHandler.class);

    public HomeConnectWasherHandler(Thing thing,
            HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    protected void configureChannelUpdateHandlers(Map<String, ChannelUpdateHandler> handlers) {
        // register default update handlers
        handlers.put(CHANNEL_DOOR_STATE, defaultDoorStateChannelUpdateHandler());
        handlers.put(CHANNEL_OPERATION_STATE, defaultOperationStateChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE, defaultRemoteControlActiveStateChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_START_ALLOWANCE_STATE, defaultRemoteStartAllowanceChannelUpdateHandler());
        handlers.put(CHANNEL_LOCAL_CONTROL_ACTIVE_STATE, defaultLocalControlActiveStateChannelUpdateHandler());
        handlers.put(CHANNEL_ACTIVE_PROGRAM_STATE, defaultActiveProgramStateUpdateHandler());
        handlers.put(CHANNEL_SELECTED_PROGRAM_STATE,
                updateProgramOptionsStateDescriptionsAndSelectedProgramStateUpdateHandler());

        // register washer specific handlers
        handlers.put(CHANNEL_WASHER_SPIN_SPEED, (channelUID, cache) -> {
            Optional<Channel> channel = getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE);
            if (channel.isPresent()) {
                updateProgramOptionsStateDescriptionsAndSelectedProgramStateUpdateHandler()
                        .handle(channel.get().getUID(), cache);
            }
        });
        handlers.put(CHANNEL_WASHER_TEMPERATURE, (channelUID, cache) -> {
            Optional<Channel> channel = getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE);
            if (channel.isPresent()) {
                updateProgramOptionsStateDescriptionsAndSelectedProgramStateUpdateHandler()
                        .handle(channel.get().getUID(), cache);
            }
        });
    }

    @Override
    protected void configureEventHandlers(Map<String, EventHandler> handlers) {
        // register default event handlers
        handlers.put(EVENT_DOOR_STATE, defaultDoorStateEventHandler());
        handlers.put(EVENT_REMOTE_CONTROL_ACTIVE, defaultBooleanEventHandler(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE));
        handlers.put(EVENT_REMOTE_CONTROL_START_ALLOWED,
                defaultBooleanEventHandler(CHANNEL_REMOTE_START_ALLOWANCE_STATE));
        handlers.put(EVENT_FINISH_IN_RELATIVE, defaultRemainingProgramTimeEventHandler());
        handlers.put(EVENT_REMAINING_PROGRAM_TIME, defaultRemainingProgramTimeEventHandler());
        handlers.put(EVENT_PROGRAM_PROGRESS, defaultPercentQuantityTypeEventHandler(CHANNEL_PROGRAM_PROGRESS_STATE));
        handlers.put(EVENT_LOCAL_CONTROL_ACTIVE, defaultBooleanEventHandler(CHANNEL_LOCAL_CONTROL_ACTIVE_STATE));
        handlers.put(EVENT_ACTIVE_PROGRAM, defaultActiveProgramEventHandler());
        handlers.put(EVENT_OPERATION_STATE, defaultOperationStateEventHandler());
        handlers.put(EVENT_SELECTED_PROGRAM, updateProgramOptionsAndSelectedProgramStateEventHandler());

        // register washer specific event handlers
        handlers.put(EVENT_WASHER_TEMPERATURE,
                event -> getThingChannel(CHANNEL_WASHER_TEMPERATURE).ifPresent(channel -> updateState(channel.getUID(),
                        event.getValue() == null ? UnDefType.UNDEF : new StringType(event.getValue()))));
        handlers.put(EVENT_WASHER_SPIN_SPEED,
                event -> getThingChannel(CHANNEL_WASHER_SPIN_SPEED).ifPresent(channel -> updateState(channel.getUID(),
                        event.getValue() == null ? UnDefType.UNDEF : new StringType(event.getValue()))));
        handlers.put(EVENT_WASHER_IDOS_1_DOSING_LEVEL,
                event -> getThingChannel(CHANNEL_WASHER_IDOS1).ifPresent(channel -> updateState(channel.getUID(),
                        event.getValue() == null ? UnDefType.UNDEF : new StringType(event.getValue()))));
        handlers.put(EVENT_WASHER_IDOS_2_DOSING_LEVEL,
                event -> getThingChannel(CHANNEL_WASHER_IDOS2).ifPresent(channel -> updateState(channel.getUID(),
                        event.getValue() == null ? UnDefType.UNDEF : new StringType(event.getValue()))));
    }

    @Override
    protected void handleCommand(final ChannelUID channelUID, final Command command,
            final HomeConnectApiClient apiClient)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        super.handleCommand(channelUID, command, apiClient);
        String operationState = getOperationState();

        // only handle these commands if operation state allows it
        if (operationState != null && INACTIVE_STATE.contains(operationState) && command instanceof StringType) {
            switch (channelUID.getId()) {
                case CHANNEL_WASHER_TEMPERATURE:
                    apiClient.setProgramOptions(getThingHaId(), OPTION_WASHER_TEMPERATURE, command.toFullString(), null,
                            false, false);
                    break;
                case CHANNEL_WASHER_SPIN_SPEED:
                    apiClient.setProgramOptions(getThingHaId(), OPTION_WASHER_SPIN_SPEED, command.toFullString(), null,
                            false, false);
                    break;
                case CHANNEL_WASHER_IDOS1:
                    apiClient.setProgramOptions(getThingHaId(), OPTION_WASHER_IDOS_1_DOSING_LEVEL,
                            command.toFullString(), null, false, false);
                    break;
                case CHANNEL_WASHER_IDOS2:
                    apiClient.setProgramOptions(getThingHaId(), OPTION_WASHER_IDOS_2_DOSING_LEVEL,
                            command.toFullString(), null, false, false);
                    break;
            }
        } else {
            logger.debug("Device can not handle command {} in current operation state ({}). haId={}", command,
                    operationState, getThingHaId());
        }
    }

    @Override
    public String toString() {
        return "HomeConnectWasherHandler [haId: " + getThingHaId() + "]";
    }

    @Override
    protected void resetProgramStateChannels(boolean offline) {
        super.resetProgramStateChannels(offline);
        getThingChannel(CHANNEL_REMAINING_PROGRAM_TIME_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_PROGRAM_PROGRESS_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_ACTIVE_PROGRAM_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        if (offline) {
            getThingChannel(CHANNEL_WASHER_TEMPERATURE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
            getThingChannel(CHANNEL_WASHER_SPIN_SPEED).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
            getThingChannel(CHANNEL_WASHER_IDOS1).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
            getThingChannel(CHANNEL_WASHER_IDOS2).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        }
    }
}
