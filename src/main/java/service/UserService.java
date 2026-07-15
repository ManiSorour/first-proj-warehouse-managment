package service;

import database.connectToDb.UserGenericRepository;
import model.role.*;

import java.util.List;

public class UserService {
    private final UserGenericRepository userRepository = new UserGenericRepository();


    public List<User> listUsers(User performedBy){
        requireAdmin(performedBy);
        return userRepository.findAll();
    }

    public void addUser(String username , String passwordHash , Role role , User performedBy){
        requireAdmin(performedBy);
        if(username == null || username.isBlank() ){
            System.out.println("نام کاربری نمیتواند خالی باشد");
        }
        if(userRepository.findByUsername(username) != null){
            System.out.println("یوزر از قبل وجود دارد ");
        }
        User newUser = switch (role){

            case ADMIN -> new Admin(0 , username , passwordHash);
            case WAREHOUSE_KEEPER -> new InventoryManager(0 , username , passwordHash);
            case INSPECTOR -> new Inspector(0 , username , passwordHash);
        };

        userRepository.save(newUser);
    }

    public void deleteUser(int userId, User performedBy){
        requireAdmin(performedBy);

        if(performedBy.getId() == userId){
            throw new IllegalArgumentException("نمی‌توانید حساب کاربری خودتان را حذف کنید");
        }
        if(userRepository.findById(userId) == null ){
            throw new IllegalArgumentException("کاربری با این شناسه پیدا نشد");
        }
        userRepository.delete(userId);
    }
    private void requireAdmin(User performedBy) {
        if (performedBy == null || performedBy.getRole() != Role.ADMIN) {
            throw new SecurityException("فقط مدیر (Admin) اجازه‌ی مدیریت کاربران را دارد");
        }

    }
}
