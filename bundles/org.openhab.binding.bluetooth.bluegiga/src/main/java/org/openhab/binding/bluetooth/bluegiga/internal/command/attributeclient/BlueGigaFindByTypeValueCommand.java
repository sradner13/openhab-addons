/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaDeviceCommand;

/**
 * Class to implement the BlueGiga command <b>findByTypeValue</b>.
 * <p>
 * This command can be used to find specific attributes on a remote device based on their 16-bit
 * UUID value and value. The search can be limited by a starting and ending handle values.
 * <p>
 * This class provides methods for processing BlueGiga API commands.
 * <p>
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
@NonNullByDefault
public class BlueGigaFindByTypeValueCommand extends BlueGigaDeviceCommand {
    public static int COMMAND_CLASS = 0x04;
    public static int COMMAND_METHOD = 0x00;

    /**
     * First requested handle number
     * <p>
     * BlueGiga API type is <i>uint16</i> - Java type is {@link int}
     */
    private int start;

    /**
     * Last requested handle number
     * <p>
     * BlueGiga API type is <i>uint16</i> - Java type is {@link int}
     */
    private int end;

    /**
     * 2 octet UUID to find
     * <p>
     * BlueGiga API type is <i>uuid</i> - Java type is {@link UUID}
     */
    private UUID uuid = new UUID(0, 0);

    /**
     * Attribute value to find
     * <p>
     * BlueGiga API type is <i>uint8array</i> - Java type is {@link int[]}
     */
    private int[] value = new int[0];

    /**
     * First requested handle number
     *
     * @param start the start to set as {@link int}
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Last requested handle number
     *
     * @param end the end to set as {@link int}
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * 2 octet UUID to find
     *
     * @param uuid the uuid to set as {@link UUID}
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Attribute value to find
     *
     * @param value the value to set as {@link int[]}
     */
    public void setValue(int[] value) {
        this.value = value;
    }

    @Override
    public int[] serialize() {
        // Serialize the header
        serializeHeader(COMMAND_CLASS, COMMAND_METHOD);

        // Serialize the fields
        serializeUInt8(connection);
        serializeUInt16(start);
        serializeUInt16(end);
        serializeUuid(uuid);
        serializeUInt8Array(value);

        return getPayload();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BlueGigaFindByTypeValueCommand [connection=");
        builder.append(connection);
        builder.append(", start=");
        builder.append(start);
        builder.append(", end=");
        builder.append(end);
        builder.append(", uuid=");
        builder.append(uuid);
        builder.append(", value=");
        for (int c = 0; c < value.length; c++) {
            if (c > 0) {
                builder.append(' ');
            }
            builder.append(String.format("%02X", value[c]));
        }
        builder.append(']');
        return builder.toString();
    }
}
