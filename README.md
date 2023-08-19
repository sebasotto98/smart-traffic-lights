# SmartTrafficLights
Project for Ambient Intelligence (AmI) 2022

To start the backend:

``` 
./mvnw spring-boot:run
```

To start the frontend:

```
yarn start
```

To start the IoT:

- Define the IP address of the machine running the MQTT broker inside the traffic.py file.
- In a terminal run:
```
source /home/pi/project/env/bin/activate
cd /home/pi/Documents/object_detection_projects-master/car_counting
python traffic.py
```
