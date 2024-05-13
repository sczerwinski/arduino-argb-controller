# Arduino ARGB Controller

A configurable controller for ARGB fans.

## ARGB Fans

Current setup includes 6 fans, each containing 8 individually
addressable RGB LEDs (NeoPixel), using 5V 3-pin ARGB connector.

## Microcontroller

The hardware intended for this project is [Arduino Nano] with
[ATmega328P] microcontroller.

The Arduino board and the LEDs are powered from a single SATA
connector:
- 12V and GND are connected to VIN and GND on Arduino board;
- 5V and GND are connected to 5V and GND pins of ARGB connectors.

Additionally, the Arduino board is connected directly to the USB
pins on the motherboard for serial communication.

### Constraints

With [ATmega328P] microcontroller, there comes a set of
constraints on the project:
- **SRAM:** 2048 bytes
- **EEPROM:** 1024 bytes

The size of the EEPROM limits the number of LED data points that
can be persistently stored in the microcontroller. When using
2 bytes per single LED colour (565 RGB), 10 frames can be defined
(6 fans * 8 LEDs * 10 frames * 2 bytes = 960 bytes).


[Arduino Nano]: https://docs.arduino.cc/hardware/nano/
[ATmega328P]: https://www.microchip.com/en-us/product/atmega328p
