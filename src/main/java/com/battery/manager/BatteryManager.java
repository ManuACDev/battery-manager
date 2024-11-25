package com.battery.manager;


import java.util.List;

import javafx.application.Application;
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

        // Actualizar información de la batería al iniciar
        updateBatteryInfo();
    }
    
    private void updateBatteryInfo() {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();

        List<PowerSource> powerSources = hal.getPowerSources();
        if (powerSources.isEmpty()) {
            batteryPercentageLabel.setText("Battery Percentage: No battery found");
            batteryStatusLabel.setText("Battery Status: Not available");
        } else {
            PowerSource powerSource = powerSources.get(0);
            double maxCapacity = powerSource.getMaxCapacity();
            double remainingCapacity = maxCapacity > 0 
                    ? (powerSource.getCurrentCapacity() / maxCapacity) * 100 
                    : 0;
            
            batteryPercentageLabel.setText("Battery Percentage: " + 
                    (maxCapacity > 0 ? String.format("%.0f", remainingCapacity) + "%" : "Unavailable"));
                batteryStatusLabel.setText("Battery Status: " + (powerSource.isCharging() ? "Charging" : "Not Charging"));
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
