
#include "Arduino.h"
#include <SoftwareSerial.h>

#define SOIL_BAUD 4800 // Baud rate for the sensor
#define TXD      27 // Pin for transmitting data
#define RXD      14 // Pin for receiving data
#define BUF_SIZE 20 // Buffer size for storing sensor data
#define TIMEOUT  100 // Timeout for waiting for sensor response
#define SOIL_SERIAL Serial2 // Serial port for the sensor

// Enum for different types of soil data
enum SoilData {
   MOISTURE = 0, 
   TEMPERATURE =1,
   PH = 2, 
   NITROGEN = 3, 
   PHOSPHOROUS = 4, 
   POTASSIUM = 5
};

// Soil sensor class definition
class SoilSensor {
public:
  void init();
  float read(SoilData type);
private:
  uint16_t querySensor(byte val); // Send query to sensor and return response
  byte buf[BUF_SIZE]; // Buffer to store sensor data
  // Queries for each data type
  const byte queries[6][8] = {
    {0x01, 0x03, 0x00, 0x00, 0x00, 0x01, 0x84, 0x0A},// Moisture
    {0x01, 0x03, 0x00, 0x01, 0x00, 0x01, 0xD5, 0xCA},// Temperature
    {0x01, 0x03, 0x00, 0x03, 0x00, 0x01, 0x74, 0x0A},// Ph
    {0x01, 0x03, 0x00, 0x04, 0x00, 0x01, 0xc5, 0xcb},// Nitrogen
    {0x01, 0x03, 0x00, 0x05, 0x00, 0x01, 0x94, 0x0b},// Phosphorous
    {0x01, 0x03, 0x00, 0x06, 0x00, 0x01, 0x64, 0x0b} // Potaasium
 };
};