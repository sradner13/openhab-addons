# OpenHAB 3 Initial Version at https://github.com/dmazan/openhab-addons/tree/isy-binding-3.1.0/bundles/org.openhab.binding.isy


# ISY Binding

This binding integrates with [Universal Device's ISY994](https://www.universal-devices.com/residential/isy994i-series/) control system.


## Supported Things

This binding currently supports the following thing types:


* dimmer
* switch
* keypad
* motion sensor
* appliance linc
* keypadlinc5
* keypadlinc6
* remotelinc8
* leak detector
* inlinelinc switch
* garage door kit
* program
* scene
* variable

## Discovery

Discovery is supported for the above insteon devices.  Discovery is also supported for Scenes, Programs and Variables.


**Note:** Discovery of the actual Isy has not been implemented.  You will need to add that manually.  Once the Isy has been added, scanning again for the Isy binding will find the insteon devices, programs, etc.

## Binding Configuration

This binding does not require any special configuration.

## Thing Configuration

The bridge requires the IP address of the bridge as well as the username and password to log in to the bridge.


## Channels

The following channels are supported:

| Thing Type      | Channel Type ID   | Item Type    | Description                                  |
|-----------------|-------------------|--------------|--------------------------------------------- |
| dimmer          | lightlevel        | Dimmer       | Increase/decrease the light level            |
| switch          | switchstatus      | Switch       | On/off status of the switch                  |
| motion          | motion_sensor     | Switch       | Motion Detected                              |
| motion          | dusk_sensor       | Switch       | Dusk/Dawn Sensor                             |
| motion          | low_battery_sensor| Switch       | Low Battery Sensor                           |
| garage          | relay             | Switch       | Dusk/Dawn Sensor                             |
| garage          | contactSensor     | Switch       | Low Battery Sensor                           |
| keypadlinc6     | lightlevel        | Switch       | Button to trigger a scene or rule            |
| keypadlinc6     | button_a          | Switch       | Button a                                     |
| keypadlinc6     | button_b          | Switch       | Button b                                     |
| keypadlinc6     | button_c          | Switch       | Button c                                     |
| keypadlinc6     | button_d          | Switch       | Button d                                     |
| remotelinc8     | button_a          | Switch       | Button e                                     |
| remotelinc8     | button_a          | Switch       | Button a                                     |
| remotelinc8     | button_b          | Switch       | Button b                                     |
| remotelinc8     | button_c          | Switch       | Button c                                     |
| remotelinc8     | button_d          | Switch       | Button d                                     |
| remotelinc8     | button_e          | Switch       | Button e                                     |
| remotelinc8     | button_f          | Switch       | Button f                                     |
| remotelinc8     | button_g          | Switch       | Button g                                     |
| remotelinc8     | button_h          | Switch       | Button h                                     |
| leakdetector    | dry               | Switch       | Dry contact sensor                           |
| leakdetector    | wet               | Switch       | Wet contact sensor                           |
| leakdetector    | heartbeat         | Switch       | Heartbeat sensor                             |
