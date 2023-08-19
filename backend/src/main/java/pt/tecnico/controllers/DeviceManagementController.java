package pt.tecnico.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.tecnico.entities.TrafficLightState;
import pt.tecnico.services.DeviceManagementService;

import java.lang.invoke.MethodHandles;

@RestController
public class DeviceManagementController {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private final DeviceManagementService deviceManagementService;

    @Autowired
    public DeviceManagementController(DeviceManagementService deviceManagementService) {
        this.deviceManagementService = deviceManagementService;
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path="/readTrafficLightState")
    public ResponseEntity<TrafficLightState> readTrafficLightState(int tf_id) {
        TrafficLightState state = deviceManagementService.getTrafficLightState(tf_id);
        if(state != null) {
            logger.info("Returning state: " + state + " for traffic light with id: " + tf_id);
            return new ResponseEntity<>(state, HttpStatus.OK);
        } else {
            logger.info("Traffic light with id: " + tf_id + " not found");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
