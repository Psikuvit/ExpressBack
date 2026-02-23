package me.psikuvit.express.service;

import me.psikuvit.express.model.Location;
import me.psikuvit.express.model.User;
import me.psikuvit.express.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final UserRepository userRepository;

    public LocationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Location updateUserLocation(String username, Location location) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLocation(location);
        userRepository.save(user);

        return location;
    }

    public Location getUserLocation(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getLocation();
    }
}

