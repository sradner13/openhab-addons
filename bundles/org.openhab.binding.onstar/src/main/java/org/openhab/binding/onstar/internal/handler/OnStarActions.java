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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@vehicleAction } class is responsible to call corresponding
 * action on vehicle Handler
 *
 * @author Scott Radner - Initial contribution
 *         From the work of Gaël L'hopital - volvooncall
 */
@ThingActionsScope(name = "onstar")
@NonNullByDefault
public class OnStarActions implements ThingActions {

    private final static Logger logger = LoggerFactory.getLogger(OnStarActions.class);

    private @Nullable VehicleHandler handler;

    public OnStarActions() {
        logger.info("OnStar vehicle actions service instanciated");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof VehicleHandler) {
            this.handler = (VehicleHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "OnStar : SendCommand", description = "Call Supported Command")
    public @ActionOutput(name = "result", label = "Result", type = "java.lang.String") String sendCommand(
            @ActionInput(name = "command", label = "Command") String command,
            @ActionInput(name = "payload", label = "Payload") String payload) {
        logger.debug("sendCommand called");
        if (handler != null) {
            return handler.actionSendCommand(command, payload);
        } else {
            return "OnStar Action service ThingHandler is null!";
        }
    }

    public static String sendCommand(@Nullable ThingActions actions, String command, String payload) {
        if (actions instanceof OnStarActions) {
            return ((OnStarActions) actions).sendCommand(command, payload);
        } else {
            return "Instance is not an OnStarActionsService class.";
        }
    }
}
