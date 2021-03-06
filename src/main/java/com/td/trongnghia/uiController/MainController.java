/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.td.trongnghia.uiController;

import com.td.trongnghia.constants.UIConstants;
import com.td.trongnghia.elements.DateEditingCell;
import com.td.trongnghia.entity.OrderEntity;
import com.td.trongnghia.entity.OrderResourceEntity;
import com.td.trongnghia.entity.ResourceEntity;
import com.td.trongnghia.entity.UserEntity;
import com.td.trongnghia.manager.AppManager;
import com.td.trongnghia.util.Util;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.controlsfx.control.table.TableFilter;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * FXML Controller class
 *
 * @author TRONGNGHIA
 */
public class MainController {

    @FXML
    private TableView<OrderEntity> orderTable;
    @FXML
    private TableView<OrderResourceEntity> orderDetailTable;
    @FXML
    private TableView<ResourceEntity> resourcesTable;
    @FXML
    private TableView<UserEntity> usersTable;
    @FXML
    private Button newOrderBtn;
    @FXML
    private TextField totalOrdersCreatedTF;
    @FXML
    private TextField totalOrdersCompletedTF;
    @FXML
    private TextField totalOrdersNotCompletedTF;
    @FXML
    private TextField invoiceTF;
    @FXML
    private TextField realInvoiceTF;
    @FXML
    private TextField resourceNameTF;
    @FXML
    private TextField resourceDescTF;
    @FXML
    private TextField resourceOrigPriceTF;
    @FXML
    private TextField resourceRentPriceTF;
    @FXML
    private TextField shipperPaymentTF;
    @FXML
    private TextField receiverPaymentTF;
    @FXML
    private TextField suppPaymentTF;
    @FXML
    private TextField shipperTF;
    @FXML
    private TextField receiverTF;
    @FXML
    private TextField plannedPaymentTF;
    @FXML
    private TextField finalPaymentTF;
    @FXML
    private TextField shipPaymentTF;
    @FXML
    private Button addResourceBtn;
    @FXML
    private Button viewHistoryBtn;
    @FXML
    private Button printBtn;
    @FXML
    private Button addUserBtn;
    @FXML
    private Tab resourceTab;
    @FXML
    private Tab userTab;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button signOutBtn;
    private ObservableList<OrderEntity> orderObservableList;
    private ObservableList<OrderResourceEntity> orderDetailObservableList;
    private ObservableList<OrderEntity> orderObservableBackingList;
    private ObservableList<ResourceEntity> resourceObservableList;
    private ObservableList<ResourceEntity> resourceObservableBackingList;
    private ObservableList<UserEntity> userObservableList;
    private ObservableList<UserEntity> userObservableBackingList;

    private OrderEntity orderSelected;

    private Scene mainScene;

    /**
     * Initializes the controller class.
     */
    @FXML
    private void initialize() {
        orderTable.setEditable(true);
        Calendar cal = Calendar.getInstance(Locale.FRANCE);
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        java.sql.Date firstDate = new java.sql.Date(cal.getTime().getTime());
        cal.add(Calendar.DAY_OF_WEEK, 7);
        java.sql.Date lastDate = new java.sql.Date(cal.getTime().getTime());

        List<OrderEntity> orderEntities = Util.orderDAO.getCurrentOrders(firstDate, lastDate);
        this.orderObservableList = FXCollections.observableArrayList(orderEntities);

        tabPane.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldTab, newTab) -> {
                    if (newTab == resourceTab) {
                        if (this.resourceObservableList == null) {
                            List<ResourceEntity> resourceEntities = Util.resourceDAO.findAll();
                            this.resourceObservableList = FXCollections.observableArrayList(resourceEntities);
                            loadResourcesTable();
                        }
                    } else if (newTab == userTab) {
                        if (this.userObservableList == null) {
                            List<UserEntity> userEntities = Util.userDAO.findAll();
                            this.userObservableList = FXCollections.observableArrayList(userEntities);
                            loadUsersTable();
                        }
                    }
                });

        addUserBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showUserDialog(null);
            }
        });

        statistics();
    }

    public void initManager(final AppManager appManager, Scene scene) {
        this.mainScene = scene;
        loadOrderTable(appManager);
        loadOrderDetailTable();

        signOutBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Util.userLogin = null;
                appManager.showLoginScreen();
            }
        });

        newOrderBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openNewOrderWindow();
            }

            private void openNewOrderWindow() {
                appManager.showNewOrderScreen(null);
            }
        });

        printBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (orderSelected == null || orderSelected.getDateReturned() == null || orderDetailObservableList == null || orderDetailObservableList.size() == 0) {
                    Util.showNotification(UIConstants.TASK_FAILED, "Không chọn hóa đơn nào hoặc hóa đơn chưa thanh toán");
                    return;
                }
                try {
                    Util.printOrder(orderDetailObservableList, orderSelected);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        addResourceBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showResourceDialog(null);
            }
        });

        viewHistoryBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openOverviewWindow();
            }

            private void openOverviewWindow() {
                appManager.showOverviewScreen();
            }
        });
    }

    private void loadOrderDetailTable() {
        TableColumn resourceNameCol = new TableColumn("Tên thiết bị");
        resourceNameCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderResourceEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderResourceEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getResourceEntity().getResourceName());
            }
        });

        TableColumn quantityCol = new TableColumn("Số lượng");
        quantityCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        quantityCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderResourceEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderResourceEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getQuantity().toString());
            }
        });

        TableColumn paymentCol = new TableColumn("Thành tiền");
        paymentCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        paymentCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderResourceEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderResourceEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getPayment() != null ? String.format(UIConstants.DOUBLE_FORMAT, data.getValue().getPayment() * 1000) : null);
            }
        });

        orderDetailTable.getColumns().addAll(resourceNameCol, quantityCol, paymentCol);

        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                OrderEntity currentOrder = newSelection;
                this.orderSelected = currentOrder;
                this.orderDetailObservableList = FXCollections.observableArrayList(Util.orderResourceDAO.getOrderResourcesByOrderId(currentOrder.getOrderId()));
                orderDetailTable.setItems(this.orderDetailObservableList);
                shipperTF.setText(currentOrder.getShipper() == null ? null : currentOrder.getShipper().getName());
                receiverTF.setText(currentOrder.getReceiver() == null ? null : currentOrder.getReceiver().getName());
                shipperPaymentTF.setText(Util.getMoneyNumber(currentOrder.getShipperPayment() * -1));
                receiverPaymentTF.setText(Util.getMoneyNumber(currentOrder.getReceiverPayment() * -1));
                plannedPaymentTF.setText(Util.getMoneyNumber(currentOrder.getPlannedPayment()));
                suppPaymentTF.setText(Util.getMoneyNumber(currentOrder.getSuppPayment()));
                shipPaymentTF.setText(Util.getMoneyNumber(currentOrder.getShipPayment()));
                finalPaymentTF.setText(Util.getMoneyNumber(currentOrder.getFinalPayment()));
            }
        });
    }

    private void loadOrderTable(AppManager appManager) {
        TableColumn statusCol = new TableColumn("Trạng thái");
        statusCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderEntity, String> data) {
                String displayedString = null;
                if (data.getValue().getStatus() == UIConstants.STATUS_SHIPPED) {
                    displayedString = "Đã giao";
                } else if (data.getValue().getStatus() == UIConstants.STATUS_RECEIVED) {
                    displayedString = "Đã nhận";
                } else {
                    displayedString = "Mới tạo";
                }
                return new SimpleStringProperty(displayedString);
            }
        });

        statusCol.setCellFactory(column -> {
            return new TableCell<OrderEntity, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);

                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        // Style all dates in March with a different color.
                        if (item.equals("Đã nhận")) {
                            setTextFill(Color.CHOCOLATE);
                            setStyle("-fx-background-color: yellow");
                        } else {
                            setTextFill(Color.BLACK);
                            setStyle("");
                        }
                    }
                }
            };
        });

        TableColumn depositCol = new TableColumn("Cọc");
        depositCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderEntity, String> data) {
                String displayedString = null;
                switch (data.getValue().getDeposit()) {
                    case -1:
                        displayedString = "CMND";
                        break;
                    case 2:
                        displayedString = data.getValue().getDeposit().toString();
                        break;
                    default:
                        break;
                }
                return new SimpleStringProperty(displayedString);
            }
        });

        TableColumn customerNameCol = new TableColumn("Tên khách hàng");
        customerNameCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getCustomerEntity().getCustomerName());
            }
        });

        TableColumn customerPhoneCol = new TableColumn("SĐT khách hàng");
        customerPhoneCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getCustomerEntity().getCustomerPhone());
            }
        });

        TableColumn customerIdentCol = new TableColumn("CMND khách hàng");
        customerIdentCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getCustomerEntity().getCustomerIdent());
            }
        });

        TableColumn dateCreatedCol = new TableColumn("Thời điểm tạo");
        dateCreatedCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderEntity, String> data) {
                String formattedDate = new SimpleDateFormat(UIConstants.DATE_TIME_FORMAT).format(data.getValue().getDateCreated());
                return new SimpleStringProperty(formattedDate);
            }
        });

        TableColumn dateModifiedCol = new TableColumn("Thời điểm sửa");
        dateModifiedCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderEntity, String> data) {
                String formattedDate = data.getValue().getDateModified() == null ? null : new SimpleDateFormat(UIConstants.DATE_TIME_FORMAT).format(data.getValue().getDateModified());
                return new SimpleStringProperty(formattedDate);
            }
        });

        TableColumn dateOrderedCol = new TableColumn("Thời điểm giao");
        dateOrderedCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderEntity, String> data) {
                String formattedDate = new SimpleDateFormat(UIConstants.DATE_TIME_FORMAT).format(data.getValue().getDateShipped());
                return new SimpleStringProperty(formattedDate);
            }
        });

        TableColumn dateReturnCol = new TableColumn("Thời điểm trả");
        dateReturnCol.setCellFactory(new Callback<TableColumn<OrderEntity, Timestamp>, TableCell<OrderEntity, Timestamp>>() {

            @Override
            public TableCell<OrderEntity, Timestamp> call(TableColumn<OrderEntity, Timestamp> p) {
                return new DateEditingCell();
            }

        });
        dateReturnCol.setCellValueFactory(new PropertyValueFactory("dateReturned"));
        dateReturnCol.setEditable(true);
        dateReturnCol.setMinWidth(180);
        dateReturnCol.setOnEditCommit(
                new EventHandler<CellEditEvent<OrderEntity, Timestamp>>() {
            @Override
            public void handle(CellEditEvent<OrderEntity, Timestamp> t) {
                ((OrderEntity) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setDateReturned(t.getNewValue());
            }
        }
        );

        TableColumn userCreatedCol = new TableColumn("Người tạo");
        userCreatedCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getUserCreated().getUserName());
            }
        });

        TableColumn businessTypeCol = new TableColumn("Loại giao dịch");
        businessTypeCol.setCellValueFactory(
                new Callback<CellDataFeatures<OrderEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<OrderEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getBusinessTypeEntity().getBusinessTypeDescription());
            }
        });

        orderTable.setItems(this.orderObservableList);
        orderTable.getColumns().addAll(statusCol, depositCol, customerNameCol, customerPhoneCol, customerIdentCol, dateOrderedCol, dateReturnCol, dateCreatedCol, dateModifiedCol, userCreatedCol, businessTypeCol);

        orderTable.setRowFactory(new Callback<TableView<OrderEntity>, TableRow<OrderEntity>>() {
            @Override
            public TableRow<OrderEntity> call(TableView<OrderEntity> tableView) {
                final TableRow<OrderEntity> row = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();
                final MenuItem removeMenuItem = new MenuItem("Xóa");
                final MenuItem confirmShippedMenuItem = new MenuItem("Xác nhận Đã Giao");
                final MenuItem confirmReceivedMenuItem = new MenuItem("Xác nhận Đã Nhận");
                final MenuItem shipperPaymentMenuItem = new MenuItem("Thanh toán cho Người giao");
                final MenuItem receiverPaymentMenuItem = new MenuItem("Thanh toán cho Người nhận");
                final MenuItem suppPaymentMenuItem = new MenuItem("Thanh toán Thêm/ Bớt");
                final MenuItem editMenuItem = new MenuItem("Chỉnh sửa thông tin");
                removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        OrderEntity currentOrder = row.getItem();
                        removeOrder(currentOrder);
                        orderTable.refresh();
                    }
                });
                confirmShippedMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        OrderEntity currentOrder = row.getItem();
                        if (currentOrder.getDateReturned() == null) {
                            Util.showNotification(UIConstants.TASK_FAILED, "Thiếu thông tin Thời điểm trả");
                            return;
                        }
                        currentOrder.setStatus(1);
                        updateOrder(currentOrder);
                        orderTable.refresh();
                    }
                });
                confirmReceivedMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        OrderEntity currentOrder = row.getItem();
                        if (currentOrder.getDateReturned() == null) {
                            Util.showNotification(UIConstants.TASK_FAILED, "Thiếu thông tin Thời điểm trả");
                            return;
                        }
                        currentOrder.setStatus(2);
                        updateOrder(currentOrder);
                        orderTable.refresh();
                    }
                });
                editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        OrderEntity currentOrder = row.getItem();
                        openNewOrderWindow(currentOrder);
                    }

                    private void openNewOrderWindow(OrderEntity currentOrder) {
                        appManager.showNewOrderScreen(currentOrder);
                    }
                });
                shipperPaymentMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        OrderEntity currentOrder = row.getItem();
                        showPaymentDialog(currentOrder, UIConstants.SHIPPER_PAYMENT);
                    }
                });
                receiverPaymentMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        OrderEntity currentOrder = row.getItem();
                        showPaymentDialog(currentOrder, UIConstants.RECEIVER_PAYMENT);
                    }
                });
                suppPaymentMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        OrderEntity currentOrder = row.getItem();
                        showPaymentDialog(currentOrder, UIConstants.SUPP_PAYMENT);
                    }
                });
                contextMenu.getItems().addAll(removeMenuItem, confirmShippedMenuItem, confirmReceivedMenuItem, shipperPaymentMenuItem, receiverPaymentMenuItem, suppPaymentMenuItem, editMenuItem);
                // Set context menu on row, but use a binding to make it only show for non-empty rows:  
                row.contextMenuProperty().bind(
                        Bindings.when(row.emptyProperty())
                                .then((ContextMenu) null)
                                .otherwise(contextMenu)
                );
                return row;
            }
        });

        //Filter on table like Excel
        TableFilter tableFilter = new TableFilter(orderTable);
        this.orderObservableBackingList = tableFilter.getBackingList();
    }

    private void loadResourcesTable() {
        TableColumn resourceNameCol = new TableColumn("Tên thiết bị");
        resourceNameCol.setCellValueFactory(
                new Callback<CellDataFeatures<ResourceEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<ResourceEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getResourceName());
            }
        });

        TableColumn resourceDescCol = new TableColumn("Mô tả");
        resourceDescCol.setCellValueFactory(
                new Callback<CellDataFeatures<ResourceEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<ResourceEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getDescription());
            }
        });

        TableColumn resourceOrigPriceCol = new TableColumn("Giá gốc");
        resourceOrigPriceCol.setCellValueFactory(
                new Callback<CellDataFeatures<ResourceEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<ResourceEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getOriginalPrice().toString());
            }
        });

        TableColumn resourceRentPriceCol = new TableColumn("Giá cho thuê ngày đầu");
        resourceRentPriceCol.setCellValueFactory(
                new Callback<CellDataFeatures<ResourceEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<ResourceEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getRentPrice().toString());
            }
        });

        TableColumn quantityCol = new TableColumn("Số lượng");
        quantityCol.setCellValueFactory(
                new Callback<CellDataFeatures<ResourceEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<ResourceEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getQuantity() == null ? "0" : data.getValue().getQuantity().toString());
            }
        });

        resourcesTable.setItems(this.resourceObservableList);
        resourcesTable.getColumns().addAll(resourceNameCol, resourceDescCol, resourceOrigPriceCol, resourceRentPriceCol, quantityCol);

        resourcesTable.setEditable(true);

        //Filter on table like Excel
        TableFilter resourceTableFilter = new TableFilter(resourcesTable);
        this.resourceObservableBackingList = resourceTableFilter.getBackingList();

        resourcesTable.setRowFactory(new Callback<TableView<ResourceEntity>, TableRow<ResourceEntity>>() {
            @Override
            public TableRow<ResourceEntity> call(TableView<ResourceEntity> tableView) {
                final TableRow<ResourceEntity> row = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();
                final MenuItem removeMenuItem = new MenuItem("Xóa");
                final MenuItem editMenuItem = new MenuItem("Chỉnh sửa thông tin");
                removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        ResourceEntity currentResource = row.getItem();
                        removeResource(currentResource);
                        resourcesTable.refresh();
                    }
                });
                editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        ResourceEntity currentResource = row.getItem();
                        showResourceDialog(currentResource);
                        resourcesTable.refresh();
                    }
                });
                contextMenu.getItems().addAll(removeMenuItem, editMenuItem);
                // Set context menu on row, but use a binding to make it only show for non-empty rows:  
                row.contextMenuProperty().bind(
                        Bindings.when(row.emptyProperty())
                                .then((ContextMenu) null)
                                .otherwise(contextMenu)
                );
                return row;
            }
        });
    }

    private void loadUsersTable() {
        TableColumn userNameCol = new TableColumn("Tên tài khoản");
        userNameCol.setCellValueFactory(
                new Callback<CellDataFeatures<UserEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<UserEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getUserName());
            }
        });

        TableColumn nameCol = new TableColumn("Tên người dùng");
        nameCol.setCellValueFactory(
                new Callback<CellDataFeatures<UserEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<UserEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getName());
            }
        });

        TableColumn emailCol = new TableColumn("Email");
        emailCol.setCellValueFactory(
                new Callback<CellDataFeatures<UserEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<UserEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getEmail());
            }
        });

        TableColumn phoneCol = new TableColumn("Số điện thoại");
        phoneCol.setCellValueFactory(
                new Callback<CellDataFeatures<UserEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<UserEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getPhone());
            }
        });

        TableColumn roleCol = new TableColumn("Vai trò");
        roleCol.setCellValueFactory(
                new Callback<CellDataFeatures<UserEntity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<UserEntity, String> data) {
                return new SimpleStringProperty(data.getValue().getRole());
            }
        });

        usersTable.setItems(this.userObservableList);
        usersTable.getColumns().addAll(userNameCol, nameCol, emailCol, phoneCol, roleCol);

        //Filter on table like Excel
        TableFilter userTableFilter = new TableFilter(usersTable);
        this.userObservableBackingList = userTableFilter.getBackingList();

        usersTable.setRowFactory(new Callback<TableView<UserEntity>, TableRow<UserEntity>>() {
            @Override
            public TableRow<UserEntity> call(TableView<UserEntity> tableView) {
                final TableRow<UserEntity> row = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();
                final MenuItem removeMenuItem = new MenuItem("Xóa");
                final MenuItem editMenuItem = new MenuItem("Chỉnh sửa thông tin");
                removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserEntity currentUser = row.getItem();
                        removeUser(currentUser);
                        usersTable.refresh();
                    }
                });
                editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserEntity currentUser = row.getItem();
                        showUserDialog(currentUser);
                        usersTable.refresh();
                    }
                });
                contextMenu.getItems().addAll(removeMenuItem, editMenuItem);
                // Set context menu on row, but use a binding to make it only show for non-empty rows:  
                row.contextMenuProperty().bind(
                        Bindings.when(row.emptyProperty())
                                .then((ContextMenu) null)
                                .otherwise(contextMenu)
                );
                return row;
            }
        });
    }

    private void removeOrder(OrderEntity orderEntity) {
        Util.orderDAO.delete(orderEntity);
        Util.showNotification(UIConstants.TASK_DONE, "Xóa hóa đơn khỏi CSDL thành công");
        this.orderObservableBackingList.remove(orderEntity);
        statistics();
    }

    private void removeResource(ResourceEntity resourceEntity) {
        try {
            Util.resourceDAO.delete(resourceEntity);
            Util.showNotification(UIConstants.TASK_DONE, "Xóa thiết bị khỏi CSDL thành công");
            this.resourceObservableBackingList.remove(resourceEntity);
            statistics();
        } catch (DataIntegrityViolationException ex) {
            Util.showNotification(UIConstants.TASK_FAILED, "Xóa thất bại. Thông tin Thiết bị này đang được dùng ở hóa đơn");
        }
    }

    private void removeUser(UserEntity userEntity) {
        try {
            this.resourceObservableBackingList.remove(userEntity);
            Util.userDAO.delete(userEntity);
            usersTable.refresh();
            Util.showNotification(UIConstants.TASK_DONE, "Xóa người dùng khỏi CSDL thành công");
        } catch (DataIntegrityViolationException ex) {
            Util.showNotification(UIConstants.TASK_FAILED, "Xóa thất bại. Thông tin Người dùng này đang được dùng ở hóa đơn!");
        }
    }

    private void updateOrder(OrderEntity orderEntity) {
        calPayment(orderEntity);
        Util.showNotification(UIConstants.TASK_DONE, "Cập nhật Thành tiền và Trạng thái cho hóa đơn thành công");
        statistics();
    }

    private Integer calPayment(OrderEntity orderEntity) {
        switch (orderEntity.getBusinessTypeEntity().getBusinessTypeCode()) {
            case "RENT_BY_DAY":
                if (orderEntity.getDateReturned() == null) {
                    return null;
                }
                Double totalPayment = 0.0;
                for (OrderResourceEntity orderResourceEntity : this.orderDetailObservableList) {
                    Long rentTime = orderEntity.getDateReturned().getTime() - orderEntity.getDateShipped().getTime();
                    Long daysOfRent = (rentTime / 86400000) + 1;
                    Double rentPrice = daysOfRent >= 1 ? (orderResourceEntity.getResourceEntity().getRentPrice() + (daysOfRent - 1) * UIConstants.PRICE_DEC_FACTOR * orderResourceEntity.getResourceEntity().getRentPrice()) * orderResourceEntity.getQuantity() : 0.0;
                    rentPrice = Math.round(rentPrice * 100.0) / 100.0;
                    totalPayment += rentPrice;
                    orderResourceEntity.setPayment(rentPrice);
                    Util.orderResourceDAO.update(orderResourceEntity);
                }
                orderEntity.setPlannedPayment(totalPayment);
                Util.calTotalPayment(orderEntity);
                Util.orderDAO.update(orderEntity);
                orderDetailTable.refresh();
                break;
            case "BUY":
//                return orderEntity.getResourceEntity().getOriginalPrice() * -1;
            case "SELL":
//                return orderEntity.getResourceEntity().getOriginalPrice();
        }
        return null;
    }

    private void statistics() {
        Integer numOfOrderCreated = this.orderObservableList.size();
        Predicate<OrderEntity> predicate = s -> s.getStatus() == 2;
        Integer numOfOrderCompleted = Integer.valueOf(String.valueOf(this.orderObservableList.stream().filter(predicate).count()));
        totalOrdersCreatedTF.setText(numOfOrderCreated.toString());
        totalOrdersCompletedTF.setText(numOfOrderCompleted.toString());
        totalOrdersNotCompletedTF.setText(String.valueOf(numOfOrderCreated - numOfOrderCompleted));
        Double totalPlannedPayment = 0.0;
        for (OrderEntity orderEntity : this.orderObservableList) {
            totalPlannedPayment += orderEntity.getPlannedPayment() + orderEntity.getShipPayment();
        }
        Double totalFinalPayment = this.orderObservableList.stream().mapToDouble(OrderEntity::getFinalPayment).sum();
        invoiceTF.setText(Util.getMoneyNumber(totalPlannedPayment));
        realInvoiceTF.setText(Util.getMoneyNumber(totalFinalPayment));
        if (this.orderSelected != null) {
            plannedPaymentTF.setText(Util.getMoneyNumber(this.orderSelected.getPlannedPayment()));
            shipperPaymentTF.setText(Util.getMoneyNumber(this.orderSelected.getShipperPayment() * -1));
            receiverPaymentTF.setText(Util.getMoneyNumber(this.orderSelected.getReceiverPayment() * -1));
            suppPaymentTF.setText(Util.getMoneyNumber(this.orderSelected.getSuppPayment()));
            shipPaymentTF.setText(Util.getMoneyNumber(this.orderSelected.getShipPayment()));
            finalPaymentTF.setText(Util.getMoneyNumber(this.orderSelected.getFinalPayment()));
        }
    }

    private void showPaymentDialog(OrderEntity currentOrder, String paymentType) {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);

        Scene scene = new Scene(new StackPane());
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/payment.fxml")
        );
        try {
            scene.setRoot((Parent) loader.load());
            dialog.setResizable(false);
        } catch (IOException ex) {
            Logger.getLogger(AppManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        dialog.setScene(scene);

        if (paymentType.equals(UIConstants.SHIPPER_PAYMENT)) {
            dialog.setTitle("Cập nhật thanh toán cho người giao");
        } else if (paymentType.equals(UIConstants.RECEIVER_PAYMENT)) {
            dialog.setTitle("Cập nhật thanh toán cho người nhận");
        } else if (paymentType.equals(UIConstants.SUPP_PAYMENT)) {
            dialog.setTitle("Cập nhật thanh toán bổ sung");
        }

        PaymentController controller
                = loader.<PaymentController>getController();
        controller.setCurrentOrder(currentOrder);
        controller.setPaymentType(paymentType);
        controller.setMainScene(this.mainScene);
        controller.setStage(dialog);

        dialog.show();
    }

    private void showUserDialog(UserEntity currentUser) {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);

        Scene scene = null;
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/user.fxml")
        );
        try {
            scene = new Scene((Parent) loader.load());
            dialog.setResizable(false);
        } catch (IOException ex) {
            Logger.getLogger(AppManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        dialog.setScene(scene);

        dialog.setTitle(currentUser == null ? "Tạo mới người dùng" : "Cập nhật người dùng");

        UserController controller
                = loader.<UserController>getController();
        controller.setCurrentUser(currentUser);
        controller.setStage(dialog);
        controller.setUsersTable(usersTable);
        controller.setUsersObservableList(userObservableList);
        controller.init();

        dialog.show();
    }

    private void showResourceDialog(ResourceEntity currentResource) {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);

        Scene scene = null;
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/resource.fxml")
        );
        try {
            scene = new Scene((Parent) loader.load());
            dialog.setResizable(false);
        } catch (IOException ex) {
            Logger.getLogger(AppManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        dialog.setScene(scene);

        dialog.setTitle(currentResource == null ? "Tạo mới thiết bị" : "Cập nhật thiết bị");

        ResourceController controller
                = loader.<ResourceController>getController();
        controller.setCurrentResource(currentResource);
        controller.setStage(dialog);
        controller.setResourcesTable(resourcesTable);
        controller.setResourcesObservableList(resourceObservableList);
        controller.init();

        dialog.show();
    }
}
