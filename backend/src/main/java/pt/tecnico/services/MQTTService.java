package pt.tecnico.services;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import pt.tecnico.entities.TrafficLight;
import pt.tecnico.entities.TrafficLightState;
import pt.tecnico.repositories.TrafficLightRepository;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static java.lang.Thread.sleep;

@Service
public class MQTTService {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
    private final String TRAFFIC_LIGHT_1_CARS_TOPIC = "TRAFFIC_LIGHT_1_CARS";
    private final String TRAFFIC_LIGHT_2_CARS_TOPIC = "TRAFFIC_LIGHT_2_CARS";
    private final String TRAFFIC_LIGHT_1_STATE_TOPIC = "TRAFFIC_LIGHT_1_STATE";
    private final String TRAFFIC_LIGHT_2_STATE_TOPIC = "TRAFFIC_LIGHT_2_STATE";
    private final String SERVER_URI = "tcp://localhost:1883";
    private final int CONNECTION_TIMEOUT = 10;
    private final int QOS = 0;

    private final DeviceManagementService deviceManagementService;
    private final TrafficLightRepository trafficLightRepository;
    private IMqttClient publisher;
    private IMqttClient subscriber;

    @Autowired
    public MQTTService(DeviceManagementService deviceManagementService, TrafficLightRepository trafficLightRepository) {
        this.deviceManagementService = deviceManagementService;
        this.trafficLightRepository = trafficLightRepository;
        setup();
    }

    public void setup() {
        String publisherId = UUID.randomUUID().toString();
        String subscriberId = UUID.randomUUID().toString();
        try {
            publisher = new MqttClient(SERVER_URI, publisherId);
            subscriber = new MqttClient(SERVER_URI, subscriberId);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            logger.error("Error: ", e);
        }
    }

    public void connect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(CONNECTION_TIMEOUT);
        try {
            publisher.connect(options);
            subscriber.connect(options);
        } catch (MqttException e) {
            logger.error("Error: ", e);
        }
    }

    private void send(String topic, MqttMessage msg) {
        if (!publisher.isConnected()) {
            logger.error("Publisher is not connected.");
            return;
        }
        msg.setQos(QOS);
        msg.setRetained(true);
        try {
            publisher.publish(topic, msg);
        } catch (MqttException e) {
            logger.error("Error: ", e);
        }
    }

    public void sendNewTrafficLightState(int tf_id, TrafficLightState state) {
        logger.info("Sending new traffic light state...");
        byte[] payload = String.valueOf(state).getBytes();
        if(tf_id == 1) {
            send(TRAFFIC_LIGHT_1_CARS_TOPIC, new MqttMessage(payload));
        } else if(tf_id == 2) {
            send(TRAFFIC_LIGHT_2_CARS_TOPIC, new MqttMessage(payload));
        }
    }

    private List<TrafficLight> receiveTrafficLightState() {
        if (!subscriber.isConnected()) {
            logger.error("Subscriber is not connected.");
            return null;
        }
        logger.info("Receiving traffic light state...");
        List<TrafficLight> trafficLightList = new ArrayList<>();

        trafficLightList.add(subscribeToTopic(TRAFFIC_LIGHT_1_STATE_TOPIC));
        trafficLightList.add(subscribeToTopic(TRAFFIC_LIGHT_2_STATE_TOPIC));

        return trafficLightList;
    }

    private TrafficLight subscribeToTopic(String TOPIC) {
        CountDownLatch receivedSignal = new CountDownLatch(10);
        List<TrafficLight> trafficLightList = new ArrayList<>();
        try {
            subscriber.subscribe(TOPIC, (topic, msg) -> {
                if (msg != null) {
                    logger.info(msg.toString());
                    String[] received = msg.toString().split("/");
                    TrafficLight deserializedTrafficLight =
                            new TrafficLight(received[0], Integer.parseInt(received[1]));
                    logger.info("Deserialized tf object with address: " + deserializedTrafficLight.getStreetAddress());
                    trafficLightList.add(deserializedTrafficLight);
                } else {
                    logger.error("Message is null...");
                }
                receivedSignal.countDown();
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        if(trafficLightList.isEmpty()) {
            return null;
        }
        return trafficLightList.get(0);
    }

    @PostConstruct
    public void initialize() {

        List<TrafficLight> trafficLightList;
        connect();
        trafficLightList = receiveTrafficLightState();

        if(trafficLightList != null) {
            if(trafficLightList.get(0) != null) {
                TrafficLight tf1 = trafficLightList.get(0);
                try {
                    trafficLightRepository.save(tf1);
                } catch (DataAccessException e) {
                    logger.error("Error: ", e);
                }
            }
            if(trafficLightList.get(1) != null) {
                TrafficLight tf2 = trafficLightList.get(1);
                try {
                    trafficLightRepository.save(tf2);
                } catch (DataAccessException e) {
                    logger.error("Error: ", e);
                }
            }
        }

        Runnable runnable = () -> {
            List<TrafficLight> trafficLights;
            while(true) {
                trafficLights = receiveTrafficLightState();
                if(trafficLightList != null) {
                    if(trafficLightList.get(0) != null) {
                        TrafficLight tf1 = trafficLightRepository.findByStreetAddress(
                                trafficLights != null ? trafficLights.get(0).getStreetAddress() : null);
                        try {
                            trafficLightRepository.save(tf1);
                        } catch (DataAccessException e) {
                            logger.error("Error: ", e);
                        }
                    }
                    if(trafficLightList.get(1) != null) {
                        TrafficLight tf2 = trafficLightRepository.findByStreetAddress(
                                trafficLights != null ? trafficLights.get(1).getStreetAddress() : null);
                        try {
                            trafficLightRepository.save(tf2);
                        } catch (DataAccessException e) {
                            logger.error("Error: ", e);
                        }
                    }
                }
                try {
                    sleep(1000);
                }
                catch (InterruptedException e) {}
            }
        };
        Thread t = new Thread(runnable);
        t.start();

        Runnable runnable1 = () -> {
            int trafficLightSize = trafficLightRepository.findAll().size();
            for(int i = 0; i<trafficLightSize; i++) {
                TrafficLightState state = deviceManagementService.setTrafficLightState(i);
                sendNewTrafficLightState(i, state);
            }
            try {
                sleep(1000);
            }
            catch (InterruptedException e) {}
        };
        Thread t1 = new Thread(runnable1);
        t1.start();

    }
}