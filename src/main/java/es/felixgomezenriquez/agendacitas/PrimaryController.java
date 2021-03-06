package es.felixgomezenriquez.agendacitas;

import es.felixgomezenriquez.agendacitas.entities.Reunion;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javax.persistence.Query;

public class PrimaryController implements Initializable {
    // Declaracion de atributos de la clase
    //la mayoria de atributos los declara automaticamente el scene builder

    private Reunion reunionSeleccionada;

    @FXML
    private TableView tableViewReuniones;
    @FXML
    private TableColumn<Reunion, String> columnNombre;
    @FXML
    private TableColumn<Reunion, String> columnLugar;
    @FXML
    private TableColumn<Reunion, String> columnFecha;
    @FXML
    private TableColumn<Reunion, String> columnTemas_A_Tratar;
    @FXML
    private TableColumn<Reunion, String> columnEmpresa;
    @FXML
    private TextField textFieldNombre;
    @FXML
    private TextField textFieldLugar;
    @FXML
    private Button buttonGuardar;
    @FXML
    private TableColumn<Reunion, String> columnOrganizador;
    @FXML
    private TextField textFieldBuscar;
    @FXML
    private CheckBox checkBoxCoincide;
    @FXML
    private Button buttonBuscar;

    //Este metodo se ejecutara al inicializar la aplicacion
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Se le dice a cada columna que valor va a guardar
        columnOrganizador.setCellValueFactory(new PropertyValueFactory<Reunion, String>("organizador"));
        columnNombre.setCellValueFactory(new PropertyValueFactory<Reunion, String>("nombreReunion"));
        columnLugar.setCellValueFactory(new PropertyValueFactory<Reunion, String>("lugarReunion"));
        columnFecha.setCellValueFactory(cellData -> {
            SimpleStringProperty property = new SimpleStringProperty();
            if (cellData.getValue().getFechaReunion() != null) {

                //Se formatea la fecha para que sea mas legible para el usuario
                DateFormat formateadorFechaCorta = DateFormat.getDateInstance(DateFormat.SHORT);

                String fechaReunion = formateadorFechaCorta.format(cellData.getValue().getFechaReunion());

                property.setValue(fechaReunion);
            }
            return property;
        });
        columnTemas_A_Tratar.setCellValueFactory(new PropertyValueFactory<>("temasATratar"));

        columnEmpresa.setCellValueFactory(cellData -> {
            SimpleStringProperty property = new SimpleStringProperty();
            if (cellData.getValue().getEmpresa() != null) {
                String nombreEmpresa = cellData.getValue().getEmpresa().getNombre();
                property.setValue(nombreEmpresa);
            }
            return property;
        });

//Aqui se a??ade un listener a el tableview para poder trabajar segun la reunion seleccionada
        tableViewReuniones.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    reunionSeleccionada = (Reunion) newValue;
                    if (reunionSeleccionada != null) {
                        textFieldNombre.setText(reunionSeleccionada.getNombreReunion());
                        textFieldLugar.setText(reunionSeleccionada.getLugarReunion());
                    } else {
                        textFieldNombre.setText("");
                        textFieldLugar.setText("");
                    }
                });
        cargarTodasReuniones();

        //Con esto conseguimos que se pueda pulsar la tecla enter al hacer una busqueda por organizador
        
        textFieldBuscar.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                buttonBuscar.fire();
                event.consume();
            }
        });

    }

    
    //Este metodo carga todas las reuniones haciendo una busqueda en la base de datos y 
    //Muestra el resultado en un tableview
    private void cargarTodasReuniones() {
        Query queryReunionFindAll = App.em.createNamedQuery("Reunion.findAll");

        List<Reunion> listReunion = queryReunionFindAll.getResultList();
        System.out.println("tama??o de la lista" + listReunion);
        tableViewReuniones.setItems(FXCollections.observableArrayList(listReunion));
    }

    //Este metodo cambia de pantalla
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    
    //Este metodo contiene el codigo que se ejecuta al pulsar el boton guardar
    //para hacer una edicion rapida del tipo de reunion y el lugar de reunion
    @FXML
    private void OnActionButtonGuardar(ActionEvent event) {

        if (reunionSeleccionada != null) {
            reunionSeleccionada.setNombreReunion(textFieldNombre.getText());
            reunionSeleccionada.setLugarReunion(textFieldLugar.getText());
            App.em.getTransaction().begin();
            App.em.merge(reunionSeleccionada);
            App.em.getTransaction().commit();

            int numFilaSeleccionada = tableViewReuniones.getSelectionModel().getSelectedIndex();
            tableViewReuniones.getItems().set(numFilaSeleccionada, reunionSeleccionada);
            TablePosition pos = new TablePosition(tableViewReuniones, numFilaSeleccionada, null);
            tableViewReuniones.getFocusModel().focus(pos);
            tableViewReuniones.requestFocus();
        }

    }

    
    
//Este metodo se ejecuta al pulsar el boton surpimir y su funcion es remover el registro seleccionado
    
    @FXML
    private void OnActionButtonSuprimir(ActionEvent event) {

        if (reunionSeleccionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar");
            alert.setHeaderText("??Desea suprimir el siguiente registro?");
            alert.setContentText(reunionSeleccionada.getNombreReunion() + " " + reunionSeleccionada.getLugarReunion());
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                App.em.getTransaction().begin();
                App.em.remove(reunionSeleccionada);
                App.em.getTransaction().commit();
                tableViewReuniones.getItems().remove(reunionSeleccionada);
                tableViewReuniones.getFocusModel().focus(null);
                tableViewReuniones.requestFocus();
            } else {
                int numFilaSeleccionada = tableViewReuniones.getSelectionModel().getSelectedIndex();
                tableViewReuniones.getItems().set(numFilaSeleccionada, reunionSeleccionada);
                TablePosition pos = new TablePosition(tableViewReuniones, numFilaSeleccionada, null);
                tableViewReuniones.getFocusModel().focus(pos);
                tableViewReuniones.requestFocus();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Atenci??n");
            alert.setHeaderText("Debe seleccionar un registro");
            alert.showAndWait();
        }

    }

    
    //Este metodo se ejecuta al pulsar el boton nuevo y se encarga de crear un nuevo registro
    //pasando a la segunda pantalla "detalle"
    @FXML
    private void onActionButtonNuevo(ActionEvent event) {

        try {
            App.setRoot("secondary");
            SecondaryController secondaryController = (SecondaryController) App.fxmlLoader.getController();
            reunionSeleccionada = new Reunion();
            secondaryController.setReunion(reunionSeleccionada, true);
        } catch (IOException ex) {
            Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    //Este metodo se ejecuta al pulsar el boton editar y edita un registro ya creado, que sera el seleccionado
    // tambien yendo a la segunda pantalla
    @FXML
    private void onActionButtonEditar(ActionEvent event) {
        if (reunionSeleccionada != null) {
            try {
                App.setRoot("secondary");
                SecondaryController secondaryController = (SecondaryController) App.fxmlLoader.getController();
                secondaryController.setReunion(reunionSeleccionada, false);
            } catch (IOException ex) {
                Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Atenci??n");
            alert.setHeaderText("Debe seleccionar un registro");
            alert.showAndWait();
        }

    }

    //Este metodo se encarga de hacer una busqueda en la base de datos y mostrar los resultados
    //A traves del tableview
    @FXML
    private void onActionButtonBuscar(ActionEvent event) {

        if (!textFieldBuscar.getText().isEmpty()) {
            if (checkBoxCoincide.isSelected()) {
                Query queryReunionFindByOrganizador = App.em.createNamedQuery("Reunion.findByOrganizador");
                queryReunionFindByOrganizador.setParameter("organizador", textFieldBuscar.getText());
                List<Reunion> listReunion = queryReunionFindByOrganizador.getResultList();
                tableViewReuniones.setItems(FXCollections.observableArrayList(listReunion));
            } else {
                String strQuery = "SELECT * FROM Reunion WHERE LOWER(organizador) LIKE ";
                strQuery += "\'%" + textFieldBuscar.getText().toLowerCase() + "%\'";
                Query queryReunionLikeOrganizador = App.em.createNativeQuery(strQuery, Reunion.class);

                List<Reunion> listReunion = queryReunionLikeOrganizador.getResultList();
                tableViewReuniones.setItems(FXCollections.observableArrayList(listReunion));

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, strQuery);

            }

        } else {
            cargarTodasReuniones();
        }

    }

}
