#include <SparkFunTSL2561.h>

// Light sensor class definition
class LightSensor {
public:
    void init();
    double readData();
     
private:
    // Library object for the light sensor
    SFE_TSL2561 light;
    // Configuration parameters for the light sensor
    boolean gain = 0;   
    unsigned int ms;
    unsigned char time = 2;
    // Variable to store the lux value
    double lux = 0.0;   
};
