package service;

import database.connectToDb.UserGenericRepository;
import model.role.User;

public class AuthService {
    private static final UserGenericRepository userRepository = new UserGenericRepository();

    public static User login(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return null;
        }

        if (!user.getPasswordHash().equals(password)) {
            return null;
        }

        return user;
    }
}
