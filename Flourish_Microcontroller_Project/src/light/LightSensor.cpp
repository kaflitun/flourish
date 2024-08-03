#include "LightSensor.h";

// Initialize the light sensor
void LightSensor::init()
{
  light.begin();
  light.setTiming(gain, time, ms);
  light.setPowerUp();
}

// Read the data from the light sensor
double LightSensor::readData()
{
  unsigned int data0, data1;
  // Read the data from the sensor
  if (light.getData(data0, data1))
  {
    // If lux value is out of the sensor range, display a warning message to the console
    if (!light.getLux(gain, ms, data0, data1, lux))
      Serial.println(" Warning: sensor saturated");
  }
  else
  {
    Serial.println(" Error: read error");
  }

  return lux;
}
