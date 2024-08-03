#include "SoilSensor.h"


// Initialize serial communication with sensor
void SoilSensor::init()
{
    SOIL_SERIAL.begin(SOIL_BAUD, SERIAL_8N1, RXD, TXD);
}

// Read sensor data based on type
float SoilSensor::read(SoilData type)
{
    switch (type)
    {
    case MOISTURE:
        return querySensor(MOISTURE) * 0.1; 
    case TEMPERATURE:
        return querySensor(TEMPERATURE) * 0.1;
    case PH:
        return querySensor(PH) * 0.1;
    case NITROGEN:
        return querySensor(NITROGEN);
    case PHOSPHOROUS:
        return querySensor(PHOSPHOROUS);
    case POTASSIUM:
        return querySensor(POTASSIUM);
    }
    return 0.0f;
}

// Send query to sensor and return response
uint16_t SoilSensor::querySensor(byte requestIndex)
{
  uint32_t startTime = 0;
  uint8_t  byteCount = 0;
  memset(buf,0,sizeof(buf));// Empty incoming buffer
  // Send request
  delay(100);
  SOIL_SERIAL.write(queries[requestIndex], sizeof(queries[requestIndex]));
  SOIL_SERIAL.flush(); // Wait to be sent

  // Wait for response till timeout occurs
  startTime = millis();
  while (millis() - startTime <= TIMEOUT) {
    if (SOIL_SERIAL.available() && byteCount < sizeof(buf)) 
      buf[byteCount++] = SOIL_SERIAL.read();
  }
  // Combine 2 byte into a word..
  return (uint16_t)(buf[3] << 8 | buf[4]);
}
