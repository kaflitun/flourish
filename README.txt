Flourish - a mobile application that uses sensors and AI to improve plant care routine 
by identifying plant diseases and monitoring plant conditions. 
This solution aims to provide plant owners with care recommendations based on gathered data about 
plant conditions to improve plant care management and, as a result, plant health and growth.

Languages:

• Kotlin: Mobile application development.
• C++: Microcontroller programming for sensor integration.

APIs:

• GPT API:
    Provides AI-based plant disease identification, care recommendations, and additional plant insights.

• Google Sign-In:
    Secure user authentication to simplify account management and login.

Hardware:

• ESP32 microcontroller, light sensor TSL2561, NPK 5-pin sensor, RS-485 Transceiver for Microcontroller and NPK sensor communication, USB Type-C cable 

Features:

• Plant Care Management:
    Manage Watering and Fertilizing Schedules:
        Users can create and manage customized schedules to ensure plants are watered and fertilized on time.
        Schedules are easily accessible within the plant info section for quick reference.

• Real-Time Monitoring:
    Monitor Plant Soil and Light Parameters:
        Provides live updates on soil nutrient levels (NPK) and light intensity using integrated sensors, ensuring plants receive optimal care.
    
• Historical Data Tracking:
    View Soil and Light Parameters History:
        Users can analyze past trends for soil and light conditions, helping them make informed decisions about plant care.

• Plant Disease Management:
    Search and Add Plant Diseases by Name or Image:
        Users can identify plant diseases by entering names or taking images for disease recognition.
        The app uses an external API to identify diseases and retrieve detailed care information.

• Plant Identification:
    Add New Plants by Name or Image:
        Users can grow their plant library by entering plant names or taking images for identification.
        The app integrates an external API to identify plants and retrieve relevant details such as species and care needs.

• Personalized Recommendations:
    Receive Plant Care Suggestions:
        Plant care advice is provided for watering, fertilizing, and disease management based on real-time parameters, schedules, and detected issues.

• Hardware Integration:
    Manage Hardware Connections:
        Users can seamlessly connect to a microcontroller via Wi-Fi, enabling real-time communication with sensors for monitoring soil and light conditions.

• User Authentication:
    Google Sign-In Integration:
        Secure and convenient access is provided through Google Sign-In, allowing users to log in without creating a separate account.

