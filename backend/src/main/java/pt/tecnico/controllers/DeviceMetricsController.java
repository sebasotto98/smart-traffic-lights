package pt.tecnico.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.tecnico.services.DeviceMetricsService;

import java.lang.invoke.MethodHandles;

@RestController
public class DeviceMetricsController {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private final DeviceMetricsService deviceMetricsService;

    @Autowired
    public DeviceMetricsController(DeviceMetricsService deviceMetricsService) {
        this.deviceMetricsService = deviceMetricsService;
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path="/readTrafficLightMetrics")
    public ResponseEntity<String> readTrafficLightMetrics(int tf_id) {
        logger.info("Returning overall duration of lights for traffic light with id: " + tf_id);
        String durationsJson = deviceMetricsService.computeOverallDuration(tf_id);
        return new ResponseEntity<>(durationsJson, HttpStatus.OK);
    }
}
