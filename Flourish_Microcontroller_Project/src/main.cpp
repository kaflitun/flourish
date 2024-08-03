#include "Arduino.h"
#include <SoftwareSerial.h>
#include <Wire.h>
#include <ArduinoJson.h>
#include "light/LightSensor.h"
#include "soil/SoilSensor.h"
#include "server/WebServer.h"
#include "config.h"

LightSensor lightSensor;
SoilSensor soilSensor;
WebServer webServer(local_IP, gateway, subnet, ssid, password, USE_STATIC_IP);

String buildRootResponse(SoilSensor soilSensor, LightSensor lightSensor);
void setup()
{
  // Initialize serial communication  for debugging
  Serial.begin(9600); 
  delay(1000);
  // Initialize the sensors
  soilSensor.init();  
  lightSensor.init();
  // Register root request endpoint
  webServer.onRequest("/", HTTP_GET, [](AsyncWebServerRequest *request) {
    Serial.println("Root request received");
    // Respond with sensor data in JSON
    request->send(200, "application/json", buildRootResponse(soilSensor, lightSensor));
  });
  // Start server
  webServer.begin();

  // Print IP address
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());
}

// Build JSON response for root request containing sensor data
String buildRootResponse(SoilSensor soilSensor, LightSensor lightSensor)
{
  JsonDocument data;
  data["lightLevel"] = lightSensor.readData();
  data["humidity"] = soilSensor.read(SoilData::MOISTURE);
  delay(SOIL_SENSOR_READ_DELAY);
  data["temperature"] = soilSensor.read(SoilData::TEMPERATURE);
  delay(SOIL_SENSOR_READ_DELAY);
  data["ph"] = soilSensor.read(SoilData::PH);
  delay(SOIL_SENSOR_READ_DELAY);
  data["nitrogen"] = soilSensor.read(SoilData::NITROGEN);
  delay(SOIL_SENSOR_READ_DELAY);
  data["phosphorus"] = soilSensor.read(SoilData::PHOSPHOROUS);
  delay(SOIL_SENSOR_READ_DELAY);
  data["potassium"] = soilSensor.read(SoilData::POTASSIUM);
  String response;
  serializeJson(data, response);
  // Print response to the console
  Serial.println("Response: ");
  Serial.println(response);
  return response;
}

void loop(){
  // Ensure Wi-Fi connection
  webServer.ensureWiFiConnected();
  delay(5000);
}