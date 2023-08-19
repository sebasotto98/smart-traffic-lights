package pt.tecnico.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.tecnico.entities.TrafficLight;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrafficLightRepository extends CrudRepository<TrafficLight, Integer> {

    Optional<TrafficLight> findById(Integer id);

    TrafficLight findByStreetAddress(String streetAddress);

    List<TrafficLight> findAll();

}
