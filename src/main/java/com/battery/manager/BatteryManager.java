package com.battery.manager;


import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;
import oshi.software.os.OperatingSystem;

public class BatteryManager extends Application {
	
	private Label batteryPercentageLabel;
    private Label batteryStatusLabel;
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

        // Crear etiquetas para mostrar la información
        batteryPercentageLabel = new Label("Battery Percentage: --%");
        batteryStatusLabel = new Label("Battery Status: Unknown");
        
        // Botón para alternar carga o corriente directa
        toggleChargeButton = new Button("Use AC Power");
        toggleChargeButton.setOnAction(event -> togglePowerMode());

        // Contenedor principal
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(batteryPercentageLabel, batteryStatusLabel, toggleChargeButton);

        // Escena
        Scene scene = new Scene(layout, 300, 200);
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
                batteryStatusLabel.setText("Battery Status: Not available");
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
                    batteryStatusLabel.setText("Battery Status: " + (powerSource.isCharging() ? "Charging" : "Not Charging"));
                });
			} else {
				Platform.runLater(() -> { 
					batteryPercentageLabel.setText("Battery Percentage: Invalid data"); 
					batteryStatusLabel.setText("Battery Status: Not available"); 
				});
			}
        }
    }
    
    private void togglePowerMode() {
        useDirectPower = !useDirectPower;
        if (useDirectPower) {
            toggleChargeButton.setText("Charge Battery");
            System.out.println("Using AC power directly. Battery will not charge.");
        } else {
            toggleChargeButton.setText("Use AC Power");
            System.out.println("Charging the battery.");
        }
    }

}
