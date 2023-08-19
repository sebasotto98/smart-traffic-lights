package pt.tecnico.services;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.tecnico.entities.TrafficLight;
import pt.tecnico.repositories.TrafficLightRepository;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DeviceMetricsService {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private final TrafficLightRepository trafficLightRepository;

    @Autowired
    public DeviceMetricsService(TrafficLightRepository trafficLightRepository) {
        this.trafficLightRepository = trafficLightRepository;
    }

    public String computeOverallDuration(int id) {
        int redDuration = 0;
        int greenDuration = 0;
        int yellowDuration = 0;
        Optional<TrafficLight> optionalTrafficLight = trafficLightRepository.findById(id);
        if(optionalTrafficLight.isPresent()) {
            TrafficLight trafficLight = optionalTrafficLight.get();
            redDuration = trafficLight.getRedLightDuration();
            greenDuration = trafficLight.getGreenLightDuration();
            yellowDuration = trafficLight.getYellowLightDuration();
        }
        int overall = redDuration + greenDuration + yellowDuration;
        Map<String, Integer> durationMap = new HashMap<>();
        if(overall == 0) {
            durationMap.put("greenDuration", 33);
            durationMap.put("redDuration", 33);
            durationMap.put("yellowDuration", 33);
        } else {
            logger.info("Green duration: " + greenDuration / overall * 100);
            durationMap.put("greenDuration", greenDuration / overall * 100);
            logger.info("Red duration: " + redDuration / overall * 100);
            durationMap.put("redDuration", redDuration / overall * 100);
            logger.info("Yellow duration: " + yellowDuration / overall * 100);
            durationMap.put("yellowDuration", yellowDuration / overall * 100);
        }

        Gson gson = new Gson();

        return gson.toJson(durationMap);
    }

    public int computeRedDuration(int tf_id) {
        //TODO
        logger.info("Fetching last red light duration...");
        //fetch timestamp of last change from yellow to red (dyr) and from red to green (drg)
        //compute last red duration (drg - dyr)
        Optional<TrafficLight> tf = trafficLightRepository.findById(tf_id);
        if(tf.isPresent()) {
            Instant lastRed = tf.get().getLastRedLight();
            Instant lastGreen = tf.get().getLastGreenLight();
            return (int)(Duration.between(lastGreen, lastRed).toSeconds());
        }
        return -1;
    }

    public int computeYellowDuration(int tf_id) {
        //TODO
        logger.info("Fetching last yellow light duration...");
        //fetch timestamp of last change from green to yellow (dgy) and from yellow to red (dyr)
        //compute last yellow duration (dyr - dgy)
        Optional<TrafficLight> tf = trafficLightRepository.findById(tf_id);
        if(tf.isPresent()) {
            Instant lastYellow = tf.get().getLastYellowLight();
            Instant lastRed = tf.get().getLastRedLight();
            return (int)(Duration.between(lastRed, lastYellow).toSeconds());
        }
        return -1;
    }

    public int computeGreenDuration(int tf_id  ) {
        //TODO
        logger.info("Fetching last green light duration...");
        //fetch timestamp of last change from red to green (drg) and from green to yellow (dgy)
        //compute last green duration (dgy - drg)
        Optional<TrafficLight> tf = trafficLightRepository.findById(tf_id);
        if(tf.isPresent()) {
            Instant lastGreen = tf.get().getLastGreenLight();
            Instant lastYellow = tf.get().getLastYellowLight();
            return (int)(Duration.between(lastYellow, lastGreen).toSeconds());
        }
        return -1;
    }
}
