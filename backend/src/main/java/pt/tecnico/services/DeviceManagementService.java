package pt.tecnico.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.tecnico.entities.TrafficLight;
import pt.tecnico.entities.TrafficLightState;
import pt.tecnico.repositories.TrafficLightRepository;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.Optional;

@Service
public class DeviceManagementService {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private final TrafficLightRepository trafficLightRepository;
    private final DeviceMetricsService deviceMetricsService;

    @Autowired
    public DeviceManagementService(TrafficLightRepository trafficLightRepository, DeviceMetricsService deviceMetricsService) {
        this.trafficLightRepository = trafficLightRepository;
        this.deviceMetricsService = deviceMetricsService;
    }

    public TrafficLightState getTrafficLightState(int id) {
        logger.info("Fetching current tf light state...");
        Optional<TrafficLight> optionalTrafficLight = trafficLightRepository.findById(id);
        return optionalTrafficLight.map(TrafficLight::getCurrentLightState).orElse(null);
    }

    public TrafficLightState setTrafficLightState(int id) {

        TrafficLightState newState = computeTrafficLightState(id);

        computeLastLightDuration(id, newState);

        logger.info("Updating traffic light state...");
        Optional<TrafficLight> optionalTrafficLight = trafficLightRepository.findById(id);
        if(optionalTrafficLight.isPresent()) {
            optionalTrafficLight.get().setCurrentLightState(newState);
            trafficLightRepository.save(optionalTrafficLight.get());
        }

        return newState;
    }

    private void computeLastLightDuration(int tf_id, TrafficLightState newState) {
        logger.info("Computing last traffic light duration...");
        int duration;
        Optional<TrafficLight> optionalTrafficLight = trafficLightRepository.findById(tf_id);
        switch (newState) {
            case GREEN:
                if(optionalTrafficLight.isPresent()) {
                    optionalTrafficLight.get().setLastRedLight(Instant.now());
                    duration = deviceMetricsService.computeRedDuration(tf_id);
                    optionalTrafficLight.get().setRedLightDuration(duration);
                    trafficLightRepository.save(optionalTrafficLight.get());
                }
                break;
            case RED:
                if(optionalTrafficLight.isPresent()) {
                    optionalTrafficLight.get().setLastYellowLight(Instant.now());
                    duration = deviceMetricsService.computeYellowDuration(tf_id);
                    optionalTrafficLight.get().setYellowLightDuration(duration);
                    trafficLightRepository.save(optionalTrafficLight.get());
                }
                break;
            case YELLOW:
                if(optionalTrafficLight.isPresent()) {
                    optionalTrafficLight.get().setLastGreenLight(Instant.now());
                    duration = deviceMetricsService.computeGreenDuration(tf_id);
                    optionalTrafficLight.get().setGreenLightDuration(duration);
                    trafficLightRepository.save(optionalTrafficLight.get());
                }
        }
    }

    private TrafficLightState computeTrafficLightState(int tf_id) {
        TrafficLightState newState = TrafficLightState.RED;

        Optional<TrafficLight> tf1 = trafficLightRepository.findById(1);
        Optional<TrafficLight> tf2 = trafficLightRepository.findById(2);
        if(tf1.isPresent() && tf2.isPresent()) {
            int redDuration = 0;
            if (tf1.get().getCurrentLightState().equals(TrafficLightState.RED)) {
                redDuration = deviceMetricsService.computeRedDuration(tf_id);
            } else if (tf2.get().getCurrentLightState().equals(TrafficLightState.RED)) {
                redDuration = deviceMetricsService.computeRedDuration(tf_id);
            }
            int tf1Cars = tf1.get().getCars();
            int tf2Cars = tf2.get().getCars();
            int thisCars = tf_id == 1 ? tf1Cars : tf2Cars;
            logger.info("Computing next traffic light state...");
            //compute traffic density (di) and waiting time (ti) of cars in WZ
            //  if ((ti2 the maximum delay found on the road with the red light < 7 seconds)
            //      || (di1 the density of the road with green light < di2 the density of the road with red light))
            //      letPass(di2)
            while (thisCars > 0) {
                if(redDuration < 7 || tf1Cars < tf2Cars) {
                    newState = TrafficLightState.GREEN;
                }
                if(tf1.get().getCurrentLightState().equals(TrafficLightState.RED)) {
                    redDuration = deviceMetricsService.computeRedDuration(tf_id);
                } else if(tf2.get().getCurrentLightState().equals(TrafficLightState.RED)) {
                    redDuration = deviceMetricsService.computeRedDuration(tf_id);
                }
                tf1Cars = tf1.get().getCars();
                tf2Cars = tf2.get().getCars();
                thisCars = tf_id == 1 ? tf1Cars : tf2Cars;
            }
        }

        return newState;
    }

}