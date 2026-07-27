package controller;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.product.Product;
import model.product.ProductStatus;
import model.role.User;
import service.WareHouseService;

import java.util.List;


public class ProductForm extends BorderPane {
    private TextField searchField;
    private Button searchButton;
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private VBox dataHolder;
    private TableView<Product> tableView;

    private final User currentUser;
    private final WareHouseService wareHouseService;


    public ProductForm(User currentUser, WareHouseService wareHouseService) {
        this.currentUser = currentUser;
        this.wareHouseService = wareHouseService;

        searchField = new TextField();
        searchButton = new Button();
        addButton = new Button("افزودن کالا");
        editButton = new Button("ویرایش کالا");
        deleteButton = new Button("حذف کالا");

        dataHolder = new VBox(10);


        initializeTable();
        searchSetting();
        loadData();
        boolean canEdit = currentUser.canEditStock();
        addButton.setDisable(!canEdit);
        editButton.setDisable(!canEdit);
        deleteButton.setDisable(!canEdit);

        addButton.setOnAction(e -> addBox());
        editButton.setOnAction(e -> editBox());
        deleteButton.setOnAction(e -> deleteBox());


        HBox topBar = new HBox(10, searchField, searchButton, addButton, editButton, deleteButton);
        setTop(topBar);
        setCenter(dataHolder);
    }


    private void initializeTable() {
        tableView = new TableView<>();

        TableColumn<Product, String> name = new TableColumn("product-name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Integer> id = new TableColumn<>("id");
        id.setCellValueFactory(new PropertyValueFactory<>("id"));


        TableColumn<Product, String> code = new TableColumn("code");
        code.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Product, String> category = new TableColumn<>("category");
        category.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Product, Integer> quantity = new TableColumn<>("quantity");
        quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, ProductStatus> status = new TableColumn<>("status");
        status.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableView.getColumns().addAll(name, id, code, category, quantity, status);

        if (currentUser.canEditStock()) {

            TableColumn<Product, Double> purchasePrice = new TableColumn<>("purchasePrice");
            purchasePrice.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));

            TableColumn<Product, Double> sellPrice = new TableColumn<>("sellPrice");
            sellPrice.setCellValueFactory(new PropertyValueFactory<>("sellPrice"));

            tableView.getColumns().addAll(purchasePrice, sellPrice);
        }

        dataHolder.getChildren().add(tableView);

    }

    private void loadData() {

        List<Product> products = wareHouseService.getAllProducts();
        tableView.setItems(FXCollections.observableArrayList(products));
    }


    private void searchSetting() {
        searchButton.setText("search");
        searchButton.setStyle("-fx-background-color: #ff0000; -fx-text-fill: #ffffff;");
        searchButton.setOnAction(event ->
                {

                    String searchFill = searchField.getText().trim();
                    try {
                        List<Product> searchFieldConditions = searchFill.isEmpty()
                                ? wareHouseService.getAllProducts()
                                : wareHouseService.findProductsByCategory(searchFill);
                        tableView.setItems(FXCollections.observableArrayList(searchFieldConditions));
                    } catch (Exception e) {
                        System.out.println("جستجو با خطا مواجه شد " + e.getMessage());
                    }

                }

        );
    }


    private void addBox() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("افزودن کالا");


        TextField nameField = new TextField();
        TextField codeField = new TextField();
        TextField categoryField = new TextField();
        TextField quantityField = new TextField();
        TextField purchasePriceField = new TextField();
        TextField sellPriceField = new TextField();
        TextField minStockField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("نام:"), nameField);
        grid.addRow(1, new Label("کد:"), codeField);
        grid.addRow(2, new Label("دسته‌بندی:"), categoryField);
        grid.addRow(3, new Label("موجودی:"), quantityField);
        grid.addRow(4, new Label("قیمت خرید:"), purchasePriceField);
        grid.addRow(5, new Label("قیمت فروش:"), sellPriceField);
        grid.addRow(6, new Label("حداقل موجودی مجاز:"), minStockField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String name = nameField.getText().trim();
                    String code = codeField.getText().trim();
                    String category = categoryField.getText().trim();
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    double purchasePrice = Double.parseDouble(purchasePriceField.getText().trim());
                    double sellPrice = Double.parseDouble(sellPriceField.getText().trim());
                    int minStock = Integer.parseInt(minStockField.getText().trim());


                    wareHouseService.addProduct( name, code, category,
                            purchasePrice, sellPrice,
                            quantity, minStock,
                            currentUser);
                    loadData();
                } catch (NumberFormatException e) {
                    System.out.println("لطفاً مقادیر عددی معتبر وارد کنید.");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void editBox() {

        Product select = tableView.getSelectionModel().getSelectedItem();
        if (select == null) {
            System.out.println("لطفاً یک کالا را از جدول انتخاب کنید.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("ویرایش کالا");

        TextField nameField = new TextField(select.getName());
        TextField codeField = new TextField(select.getCode());
        TextField categoryField = new TextField(select.getCategory());
        TextField purchasePriceField = new TextField(String.valueOf(select.getPurchasePrice()));
        TextField sellPriceField = new TextField(String.valueOf(select.getSellPrice()));
        TextField minStockField = new TextField(String.valueOf(select.getMinStockLevel()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("نام:"), nameField);
        grid.addRow(1, new Label("کد:"), codeField);
        grid.addRow(2, new Label("دسته‌بندی:"), categoryField);
        grid.addRow(3, new Label("قیمت خرید:"), purchasePriceField);
        grid.addRow(4, new Label("قیمت فروش:"), sellPriceField);
        grid.addRow(5, new Label("حداقل موجودی مجاز:"), minStockField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    wareHouseService.updateProduct(
                            select.getId(),
                            nameField.getText().trim(),
                            codeField.getText().trim(),
                            categoryField.getText().trim(),
                            Double.parseDouble(purchasePriceField.getText().trim()),
                            Double.parseDouble(sellPriceField.getText().trim()),
                            Integer.parseInt(minStockField.getText().trim()),
                            currentUser
                    );
                    loadData();
                } catch (NumberFormatException e) {
                    System.out.println("لطفاً مقادیر عددی معتبر وارد کنید.");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }


    private void deleteBox() {
        Product select = tableView.getSelectionModel().getSelectedItem();
        if (select == null) {
            System.out.println("لطفاً یک کالا را از جدول انتخاب کنید.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "آیا از حذف «" + select.getName() + "» مطمئنید؟");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    wareHouseService.deleteProduct(select.getId(), currentUser);
                    loadData();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });
    }

    }


