#include "WebServer.h"

// Web server constructor
WebServer::WebServer(IPAddress local_IP, IPAddress gateway, IPAddress subnet, const char* ssid, const char* password, bool useStaticIP)
  : local_IP(local_IP), 
    gateway(gateway), 
    subnet(subnet),
    ssid(ssid), 
    password(password),
    server(80) 
{
  this->useStaticIP = useStaticIP;
}

//  Connect to the Wi-Fi and start the server
void WebServer::begin() {
  // Initialize the Wi-Fi connection
  initWifi();
  // Start the HTTP server
  Serial.println("Starting server... ");
  server.begin();
  Serial.println("Server started.");
}

// Register a request handler for a specific URI
void WebServer::onRequest(const char* uri, WebRequestMethodComposite method, ArRequestHandlerFunction onRequest) {
    server.on(uri, method, onRequest);
}

// Wi-Fi connection initialization
void WebServer::initWifi()
{
  // Set the static IP address
  if (useStaticIP && !WiFi.config(local_IP, gateway, subnet)) {
    Serial.println("Failed to configure network interface. Restarting...");
    ESP.restart();
  }
  // Wi-Fi configuration 
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  // Wait for connection
  // If the connection fails, try again after 1 second 
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected... IP address: "+WiFi.localIP().toString()+"\n");
}

// Checks if the Wi-Fi connection is lost and reconnects
void WebServer::ensureWiFiConnected()
{
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi connection lost. Reconnecting...");
    initWifi();
  }
}