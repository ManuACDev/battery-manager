package com.battery.manager;


import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;

public class BatteryManager extends Application {
	
	private VBox sideMenu; // Menú lateral
    private StackPane contentArea; // Área de contenido
    private boolean isMenuOpen = false; // Estado del menú lateral

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Battery Manager");

        // Barra superior
        StackPane topBar = new StackPane();
        topBar.setStyle("-fx-background-color: #4CAF50; -fx-padding: 10;");

        // Botón de menú hamburguesa
        Button menuButton = new Button("☰");
        menuButton.setStyle("-fx-font-size: 18; -fx-background-color: transparent; -fx-text-fill: white;");
        menuButton.setOnAction(e -> toggleMenu()); // Manejar apertura/cierre del menú lateral
        StackPane.setAlignment(menuButton, Pos.CENTER_LEFT); // Alinear a la izquierda

        // Título
        Label titleLabel = new Label("Battery Manager");
        titleLabel.setStyle("-fx-font-size: 20; -fx-text-fill: white;");
        StackPane.setAlignment(titleLabel, Pos.CENTER); // Centrar título

        // Agregar elementos al StackPane
        topBar.getChildren().addAll(menuButton, titleLabel);

        // Menú lateral
        sideMenu = new VBox();
        sideMenu.setStyle("-fx-background-color: #333333;");
        sideMenu.setPadding(new Insets(20));
        sideMenu.setSpacing(15);
        sideMenu.setPrefWidth(200);

        // Opciones del menú lateral
        Button homeButton = new Button("Home");
        Button historyButton = new Button("History");

        styleMenuButton(homeButton);
        styleMenuButton(historyButton);

        sideMenu.getChildren().addAll(homeButton, historyButton);

        // Acciones de los botones
        homeButton.setOnAction(e -> showHome());
        historyButton.setOnAction(e -> showHistory());

        // Área de contenido
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f0f0f0;");
        contentArea.setPadding(new Insets(20));

        // Mostrar la pantalla inicial (Home)
        showHome();

        // Layout principal
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(topBar);
        mainLayout.setLeft(sideMenu);
        mainLayout.setCenter(contentArea);

        // Mostrar inicialmente el menú cerrado
        sideMenu.setVisible(false);

        // Escena principal
        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    // Método para mostrar u ocultar el menú lateral
    private void toggleMenu() {
        isMenuOpen = !isMenuOpen;
        sideMenu.setVisible(isMenuOpen);
    }

    // Método para mostrar la pantalla de Home
    private void showHome() {
        contentArea.getChildren().clear();
        Label homeLabel = new Label("Welcome to Home!");
        homeLabel.setStyle("-fx-font-size: 24; -fx-text-fill: #333333;");
        contentArea.getChildren().add(homeLabel);
    }

    // Método para mostrar la pantalla de History
    private void showHistory() {
        contentArea.getChildren().clear();
        Label historyLabel = new Label("Battery History");
        historyLabel.setStyle("-fx-font-size: 24; -fx-text-fill: #333333;");
        contentArea.getChildren().add(historyLabel);
    }

    // Estilizar los botones del menú lateral
    private void styleMenuButton(Button button) {
        button.setStyle("-fx-font-size: 16; -fx-text-fill: white; -fx-background-color: #555555;");
        button.setPrefWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(10));
        button.setOnMouseEntered(e -> button.setStyle("-fx-font-size: 16; -fx-text-fill: white; -fx-background-color: #666666;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-font-size: 16; -fx-text-fill: white; -fx-background-color: #555555;"));
    }
	
	/*private Label batteryPercentageLabel;
    private Label batteryStatusLabel;
    private ProgressBar batteryProgressBar;
    private Circle powerIndicator;
    private volatile boolean running = true;
    private SystemInfo systemInfo; 
    private HardwareAbstractionLayer hal;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Battery Manager");
        
        // Etiqueta para el porcentaje de batería
        batteryPercentageLabel = new Label("Battery Percentage: --%");
        batteryPercentageLabel.setFont(Font.font("Arial", 20));
        batteryPercentageLabel.setTextFill(Color.DARKGREEN);
        
        // Barra de progreso para mostrar el nivel de la batería
        batteryProgressBar = new ProgressBar(0);
        batteryProgressBar.setPrefWidth(250);
        
        // Etiqueta para el estado de la batería
        batteryStatusLabel = new Label("Battery Status: Unknown");
        batteryStatusLabel.setFont(Font.font("Arial", 14));
        batteryStatusLabel.setTextFill(Color.DARKBLUE);
        
        // Indicadora de estado
        powerIndicator = new Circle(10);
        powerIndicator.setFill(Color.GRAY);        
        
        // Contenedor principal
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f0f0f0;");
        layout.getChildren().addAll(batteryPercentageLabel, batteryProgressBar, batteryStatusLabel, powerIndicator);
        
        // Escena
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Instanciar SystemInfo y HardwareAbstractionLayer 
        systemInfo = new SystemInfo(); 
        hal = systemInfo.getHardware();

        // Iniciar el hilo para actualizar la información de la batería
        startBatteryInfoUpdater();

        // Detener el hilo al cerrar la ventana
        primaryStage.setOnCloseRequest(event -> stopUpdaterThread());
    }
    
    private void startBatteryInfoUpdater() {
        Thread updaterThread = new Thread(() -> {
            while (running) {
                try {
                    // Actualizar información de la batería
                    updateBatteryInfo();

                    // Pausar el hilo por 5 segundos
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
                    break;
                }
            }
        });

        updaterThread.setDaemon(true); // Terminar el hilo automáticamente al cerrar la aplicación
        updaterThread.start();
    }
    
    private void stopUpdaterThread() {
        running = false;
    }
    
    private void updateBatteryInfo() {
        List<PowerSource> powerSources = hal.getPowerSources();
        if (powerSources.isEmpty()) {
            Platform.runLater(() -> {
                batteryPercentageLabel.setText("Battery Percentage: No battery found");
                batteryProgressBar.setProgress(0);
                batteryStatusLabel.setText("Battery Status: Not available");
                batteryProgressBar.setStyle("-fx-accent: gray;");
                powerIndicator.setFill(Color.GRAY);
            });
        } else {
            PowerSource powerSource = powerSources.get(0);
            double currentCapacity = powerSource.getCurrentCapacity();
            double maxCapacity = powerSource.getMaxCapacity();
            
            // Validar que currentCapacity y maxCapacity
            if (currentCapacity > 0 && maxCapacity > 0) {
            	double remainingCapacity = (currentCapacity / maxCapacity) * 100;

                Platform.runLater(() -> {
                	batteryPercentageLabel.setText("Battery Percentage: " + String.format("%.0f", remainingCapacity) + "%");
                	batteryProgressBar.setProgress(remainingCapacity / 100);
                	//batteryStatusLabel.setText("Battery Status: " + (powerSource.isCharging() ? "Charging" : "Not Charging"));
                
                	// Cambiar el color de la barra de progreso según el nivel de batería
                    if (remainingCapacity > 0.5) {
                        batteryProgressBar.setStyle("-fx-accent: green;");
                    } else if (remainingCapacity > 0.2) {
                        batteryProgressBar.setStyle("-fx-accent: orange;");
                    } else {
                        batteryProgressBar.setStyle("-fx-accent: red;");
                    }
                    
                    // Actualizar luz según capacidad y estado
                    if (powerSource.isCharging()) {
                        powerIndicator.setFill(Color.YELLOW);
                        batteryStatusLabel.setText("Battery Status: Charging");
                    } else if (powerSource.isDischarging()) {
                        powerIndicator.setFill(remainingCapacity > 20 ? Color.ORANGE : Color.RED);
                        batteryStatusLabel.setText("Battery Status: On Battery Power");
                    } else {
                        powerIndicator.setFill(Color.BLUE);
                        batteryStatusLabel.setText("Battery Status: Connected but Not Charging");
                    }
                });
			} else {
				Platform.runLater(() -> { 
					batteryPercentageLabel.setText("Battery Percentage: Invalid data");
					batteryProgressBar.setProgress(0);
					batteryStatusLabel.setText("Battery Status: Not available");
					batteryProgressBar.setStyle("-fx-accent: gray;");
					powerIndicator.setFill(Color.GRAY);
				});
			}
        }
    }*/
}
