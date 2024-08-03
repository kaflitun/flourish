#include <IPAddress.h>
#define SOIL_SENSOR_READ_DELAY 100
#define USE_STATIC_IP true

// Wi-Fi configuration
IPAddress local_IP(192, 168, 43, 99); // Static IP, out of android hotspot range
IPAddress gateway(192, 168, 1, 1); // Android hotspot gateway
IPAddress subnet(255, 255, 0, 0); // Subnet mask

const char* ssid = "wifi_name";
const char* password = "wifi_password";
