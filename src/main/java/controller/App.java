package controller;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.role.Admin;
import model.role.User;
import repository.ProductInMemoryRepository;
import service.WareHouseService;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        WareHouseService wareHouseService = new WareHouseService(new ProductInMemoryRepository());
        User currentUser = new Admin(1 , "ali" , "8871");

        ProductForm form = new ProductForm(currentUser , wareHouseService);
        Scene scene = new Scene(form, 900, 600);
        stage.setScene(scene);
        stage.setTitle("مدیریت کالا");
        stage.show();


    }
}
