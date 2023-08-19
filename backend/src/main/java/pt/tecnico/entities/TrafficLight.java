package pt.tecnico.entities;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "`Traffic_Light`")
public class TrafficLight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Traffic_Light_ID")
    private int id;

    @Column(name = "Red_Light_Duration")
    private int redLightDuration;

    @Column(name = "Green_Light_Duration")
    private int greenLightDuration;

    @Column(name = "Yellow_Light_Duration")
    private int yellowLightDuration;

    @Column(name = "Last_Red_Light")
    private Instant lastRedLight;

    @Column(name = "Last_Green_Light")
    private Instant lastGreenLight;

    @Column(name = "Last_Yellow_Light")
    private Instant lastYellowLight;

    @Column(name = "Current_Light_State")
    private TrafficLightState currentLightState;

    @Column(name = "Street_Address")
    private String streetAddress;

    @Column(name = "Cars")
    private int cars;

    public TrafficLight() {

    }

    public TrafficLight(String streetAddress, int cars) {
        this.streetAddress = streetAddress;
        this.cars = cars;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRedLightDuration() {
        return redLightDuration;
    }

    public void setRedLightDuration(int redLightDuration) {
        this.redLightDuration = redLightDuration;
    }

    public int getGreenLightDuration() {
        return greenLightDuration;
    }

    public void setGreenLightDuration(int greenLightDuration) {
        this.greenLightDuration = greenLightDuration;
    }

    public int getYellowLightDuration() {
        return yellowLightDuration;
    }

    public void setYellowLightDuration(int yellowLightDuration) {
        this.yellowLightDuration = yellowLightDuration;
    }

    public TrafficLightState getCurrentLightState() {
        return currentLightState;
    }

    public void setCurrentLightState(TrafficLightState currentLightState) {
        this.currentLightState = currentLightState;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public int getCars() {
        return cars;
    }

    public void setCars(int cars) {
        this.cars = cars;
    }

    public Instant getLastRedLight() {
        return lastRedLight;
    }

    public void setLastRedLight(Instant lastRedLight) {
        this.lastRedLight = lastRedLight;
    }

    public Instant getLastGreenLight() {
        return lastGreenLight;
    }

    public void setLastGreenLight(Instant lastGreenLight) {
        this.lastGreenLight = lastGreenLight;
    }

    public Instant getLastYellowLight() {
        return lastYellowLight;
    }

    public void setLastYellowLight(Instant lastYellowLight) {
        this.lastYellowLight = lastYellowLight;
    }
}
