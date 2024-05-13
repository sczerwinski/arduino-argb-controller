#include <FastLED.h>
#include <EEPROM.h>

// Fans:
#define LED_TYPE NEOPIXEL       // Type of LED controller (upHere ARBG fans use NEOPIXEL)

#define DATA_PIN_1 2            // Fan 1 data pin
#define DATA_PIN_2 5            // Fan 2 data pin
#define DATA_PIN_3 7            // Fan 3 data pin
#define DATA_PIN_4 3            // Fan 4 data pin
#define DATA_PIN_5 4            // Fan 5 data pin
#define DATA_PIN_6 6            // Fan 6 data pin

#define NUM_FANS 6                              // Number of fans
#define NUM_LEDS_PER_FAN 8                      // Number of LEDs per fan
#define NUM_LEDS NUM_FANS * NUM_LEDS_PER_FAN    // Total number of LEDs

#define MAX_FRAMES 10           // Maximum number of frames
#define MAX_DELAY 1000          // Maximum frame delay

// EEPROM data structure:
#define EEPROM_BYTES_PER_LED 2                                  // Color bytes per single LED
#define EEPROM_BYTES_PER_FRAME EEPROM_BYTES_PER_LED * NUM_LEDS  // Color bytes for all LEDs per frame

#define EEPROM_ADDR_NUM_FRAMES 0                // Location of number of frames in EEPROM
#define EEPROM_ADDR_FRAME_DELAY 2               // Location of frame delay in EEPROM
#define EEPROM_ADDR_LED_DATA 64                 // Location of LED color data in EEPROM

#define EEPROM_NUM_FRAMES_BYTES 1                                       // Bytes used to store number of frames
#define EEPROM_FRAME_DELAY_BYTES 2                                      // Bytes used to store frame delay
#define EEPROM_LED_DATA_BYTES EEPROM_BYTES_PER_FRAME * MAX_FRAMES       // Bytes used to store LED color data

// 565 RGB colors:
#define SHIFT_R 11              // Bit shift of red channel
#define SHIFT_G 5               // Bit shift of green channel

#define FACTOR_R 8              // Multiplication factor for red channel value
#define FACTOR_G 4              // Multiplication factor for green channel value
#define FACTOR_B 8              // Multiplication factor for blue channel value

#define CHANNEL_MASK_R 0x1f     // Bit mask of red channel (5 bits)
#define CHANNEL_MASK_G 0x3f     // Bit mask of green channel (6 bits)
#define CHANNEL_MASK_B 0x1f     // Bit mask of blue channel (5 bits)

// Serial commands:
#define CMD_READ 0              // Read data
#define CMD_WRITE 1             // Write data

// Global variables:
CRGB leds[NUM_LEDS];            // LED color data

unsigned int frameDelay;        // Frame delay in milliseconds
uint8_t numFrames;              // Number of frames

uint8_t frame = 0;              // Current frame

/**
 * Initializes serial.
 */
void initSerial() {
  Serial.begin(115200);
  Serial.setTimeout(1000);
  Serial.println();
  Serial.println("INIT");
}

/**
 * Initializes LEDs.
 */
void initLeds() {
  FastLED.addLeds<LED_TYPE, DATA_PIN_1>(leds, 0, NUM_LEDS_PER_FAN);
  FastLED.addLeds<LED_TYPE, DATA_PIN_2>(leds, NUM_LEDS_PER_FAN, NUM_LEDS_PER_FAN);
  FastLED.addLeds<LED_TYPE, DATA_PIN_3>(leds, 2 * NUM_LEDS_PER_FAN, NUM_LEDS_PER_FAN);
  FastLED.addLeds<LED_TYPE, DATA_PIN_4>(leds, 3 * NUM_LEDS_PER_FAN, NUM_LEDS_PER_FAN);
  FastLED.addLeds<LED_TYPE, DATA_PIN_5>(leds, 4 * NUM_LEDS_PER_FAN, NUM_LEDS_PER_FAN);
  FastLED.addLeds<LED_TYPE, DATA_PIN_6>(leds, 5 * NUM_LEDS_PER_FAN, NUM_LEDS_PER_FAN);
}

/**
 * Coerces number of frames in valid range (1..MAX_FRAMES).
 */
void coerceNumFrames() {
  if (numFrames < 1) {
    numFrames = 1;
  }
  if (numFrames > MAX_FRAMES) {
    numFrames = MAX_FRAMES;
  }
}

/**
 * Coerces frame delay in valid range (0..MAX_DELAY).
 */
void coerceFrameDelay() {
  if (frameDelay < 0) {
    frameDelay = 0;
  }
  if (frameDelay > MAX_DELAY) {
    frameDelay = MAX_DELAY;
  }
}

/**
 * Loads animation config from EEPROM.
 */
void loadConfigFromEEPROM() {
  EEPROM.get(EEPROM_ADDR_NUM_FRAMES, numFrames);
  coerceNumFrames();

  EEPROM.get(EEPROM_ADDR_FRAME_DELAY, frameDelay);
  coerceFrameDelay();
}

/**
 * Loads LED color data for current frame from EEPROM.
 */
void loadFrameFromEEPROM() {
  unsigned int rgb;
  for (uint8_t i = 0; i < NUM_LEDS; i++) {
    EEPROM.get(EEPROM_ADDR_LED_DATA + frame * EEPROM_BYTES_PER_FRAME + i * EEPROM_BYTES_PER_LED, rgb);
    leds[i].r = FACTOR_R * ((rgb >> SHIFT_R) & CHANNEL_MASK_R);
    leds[i].g = FACTOR_G * ((rgb >> SHIFT_G) & CHANNEL_MASK_G);
    leds[i].b = FACTOR_B * (rgb & CHANNEL_MASK_B);
  }
}

/**
 * Writes data from EEPROM to serial port.
 */
void printDataToSerial() {
  // Print number of frames:
  Serial.print("DATA numFrames ");
  Serial.println(numFrames);

  // Print frame delay:
  Serial.print("DATA frameDelay ");
  Serial.println(frameDelay);

  // Print LED color data:
  unsigned int rgb;
  for (uint8_t f = 0; f < MAX_FRAMES; f++) {
    Serial.print("DATA frame");
    Serial.print(f);
    Serial.print(" ");
    for (uint8_t i = 0; i < NUM_LEDS; i++) {
      EEPROM.get(EEPROM_ADDR_LED_DATA + f * EEPROM_BYTES_PER_FRAME + i * EEPROM_BYTES_PER_LED, rgb);
      if (i > 0) {
        Serial.print(" ");
      }
      Serial.print(rgb);
    }
    Serial.println();
  }
}

/**
 * Reads data from serial port and updates EEPROM.
 */
void updateDataFromSerial() {
  // Read number of frames:
  numFrames = Serial.parseInt(SKIP_WHITESPACE);
  coerceNumFrames();
  EEPROM.put(EEPROM_ADDR_NUM_FRAMES, numFrames);
  Serial.print("SET numFrames ");
  Serial.println(numFrames);

  // Read frame delay:
  frameDelay = Serial.parseInt(SKIP_WHITESPACE);
  coerceFrameDelay();
  EEPROM.put(EEPROM_ADDR_FRAME_DELAY, frameDelay);
  Serial.print("SET frameDelay ");
  Serial.println(frameDelay);

  // Read LED color data and store in EEPROM:
  unsigned int rgb;
  for (unsigned int i = 0; i < NUM_LEDS * numFrames; i++) {
    rgb = Serial.parseInt(SKIP_WHITESPACE);
    EEPROM.put(EEPROM_ADDR_LED_DATA + i * EEPROM_BYTES_PER_LED, rgb);
    Serial.print("SET data");
    Serial.print(i);
    Serial.print(" ");
    Serial.println(rgb);
  }
}

/**
 * Handles commands from serial port.
 */
void handleSerial() {
  // Read numbers after BEGIN marker:
  if (Serial.find("BEGIN")) {

    // Read command type:
    uint8_t cmd = Serial.parseInt(SKIP_WHITESPACE);

    switch (cmd) {
      case CMD_READ:
        printDataToSerial();
        break;

      case CMD_WRITE:
        updateDataFromSerial();
        break;

      default:
        Serial.println("ERR unknown command");
    }

    // Read remaining bytes until END marker reached:
    if (Serial.find("END")) {
      // Acknowledge end of read:
      Serial.println("DONE");
    } else {
      // Inform about timeout:
      Serial.println("ERR timeout");
    }
  }
}


void setup() {
  initSerial();
  initLeds();
  loadConfigFromEEPROM();
}

void loop() {
  // Read data from serial input if available:
  if (Serial.available() > 0) {
    handleSerial();
  }

  // Update LED data:
  loadFrameFromEEPROM();
  FastLED.show();

  // Next frame:
  frame = (frame + 1) % numFrames;

  // Delay:
  delay(frameDelay);
}
