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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;
import oshi.software.os.OperatingSystem;

public class BatteryManager extends Application {
	
	private Label batteryPercentageLabel;
    private Label batteryStatusLabel;
    private ProgressBar batteryProgressBar;
    private Circle powerIndicator;
    private Button toggleChargeButton;
    private boolean useDirectPower = false;
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
        
        // Botón para alternar carga o corriente directa
        toggleChargeButton = new Button("Use AC Power");
        toggleChargeButton.setOnAction(event -> togglePowerMode());
        
        // Contenedor principal
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f0f0f0;");
        layout.getChildren().addAll(batteryPercentageLabel, batteryProgressBar, batteryStatusLabel, powerIndicator, toggleChargeButton);
        
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
                    if (useDirectPower) {
                        powerIndicator.setFill(Color.GREEN);
                        batteryStatusLabel.setText("Battery Status: Using AC Power (Direct)");
                    } else if (powerSource.isCharging()) {
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
    }
    
    private void togglePowerMode() {
        useDirectPower = !useDirectPower;
        Platform.runLater(() -> {
            if (useDirectPower) {
                toggleChargeButton.setText("Charge Battery");
                powerIndicator.setFill(Color.GREEN); // Indica corriente directa
                batteryStatusLabel.setText("Battery Status: Using AC Power");
            } else {
                toggleChargeButton.setText("Use AC Power");
                powerIndicator.setFill(Color.YELLOW); // Indica carga
                batteryStatusLabel.setText("Battery Status: Charging Battery");
            }
        });
    }

}
