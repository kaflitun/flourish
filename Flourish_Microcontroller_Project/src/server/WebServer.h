#include <ESPAsyncWebServer.h>
#include "AsyncJson.h"
#include "ArduinoJson.h"
#include <IPAddress.h> 

// Web server class definition
class WebServer {
public:
  WebServer(IPAddress local_IP, IPAddress gateway, IPAddress subnet, const char* ssid, const char* password, bool useStaticIP = true);
  void begin();
  void onRequest(const char* uri, WebRequestMethodComposite method, ArRequestHandlerFunction onRequest);
  void initWifi();
  void ensureWiFiConnected();
  IPAddress getLocalIP();
private:
  bool useStaticIP = true;
  IPAddress local_IP;
  IPAddress gateway;
  IPAddress subnet;
  const char* ssid; // Wi-Fi network name
  const char* password;
  AsyncWebServer server;
};
