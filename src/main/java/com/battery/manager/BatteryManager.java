package com.battery.manager;


import java.util.List;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty; 
import javafx.beans.property.SimpleDoubleProperty;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    
    private SystemInfo systemInfo; 
    private HardwareAbstractionLayer hal;
    
    // Elementos de la pantalla de batería
    private Label batteryPercentage;
    private Label percentageCircle;
    private Label batteryStatus;
    private Label remainingTime;
    private Label designCapacity;
    private Label fullChargeCapacity;
    private Label speedCharge;
    private DoubleProperty batteryPercentageNumeric = new SimpleDoubleProperty();
    private boolean isCharging = false;
    private volatile boolean running = true;
    private Slider disconnectSlider;
    private Slider connectSlider;
    
    private boolean hasNotifiedLowBattery = false; 
    private boolean hasNotifiedHighBattery = false;

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
        sideMenu.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
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
        homeButton.setOnAction(e -> { showHome(); });
        historyButton.setOnAction(e -> { showHistory(); });

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
        mainLayout.setStyle("-fx-background-color: #f0f0f0;");

        // Mostrar inicialmente el menú cerrado
        sideMenu.setVisible(true);

        // Escena principal
        Scene scene = new Scene(mainLayout, 900, 550);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Instanciar SystemInfo y HardwareAbstractionLayer 
        systemInfo = new SystemInfo(); 
        hal = systemInfo.getHardware();
        
        // Iniciar el hilo de actualización de batería
        startBatteryInfoUpdater();

        // Detener el hilo al cerrar la ventana
        primaryStage.setOnCloseRequest(event -> running = false);
    }
    
    // Método para mostrar u ocultar el menú lateral
    private void toggleMenu() {
        isMenuOpen = !isMenuOpen;
        sideMenu.setVisible(isMenuOpen);
    }

    // Método para mostrar la pantalla de Home
    private void showHome() {
        contentArea.getChildren().clear();
        
        // Crear etiquetas dinámicas
        batteryPercentage = new Label("--%");
        percentageCircle = new Label("--%");
        percentageCircle.setStyle("-fx-font-size: 25; -fx-text-fill: darkgreen;");
        batteryStatus = new Label("Unknown");
        remainingTime = new Label("--");
        designCapacity = new Label("--");
        fullChargeCapacity = new Label("--");
        speedCharge = new Label("--");
        
        // Lado izquierdo: Figura de batería y configuraciones
        VBox leftColumn = new VBox(20);
        leftColumn.setAlignment(Pos.TOP_CENTER);
        leftColumn.setPadding(new Insets(20));

        // Crear el círculo que representará la batería 
        Circle batteryCircle = new Circle(75); 
        batteryCircle.setStroke(Color.DARKGREEN); 
        batteryCircle.setStrokeWidth(5); 
        batteryCircle.setFill(Color.TRANSPARENT);
        
        // Crear el círculo interior que crecerá según el porcentaje de batería 
        Circle innerCircle = new Circle(0); 
        innerCircle.setFill(Color.LIGHTGREEN); 
        
        // Vincular el radio del círculo interior con el porcentaje de batería 
        innerCircle.radiusProperty().bind(batteryPercentageNumeric.multiply(0.75));
        
        // Usar un StackPane para superponer el texto sobre el círculo 
        StackPane batteryIndicator = new StackPane(); 
        batteryIndicator.getChildren().addAll(innerCircle, batteryCircle, percentageCircle);
        
        // Checkbox para notificaciones 
        CheckBox notificationToggle = new CheckBox("Enable Notifications"); 
        notificationToggle.setSelected(true); // Activado por defecto 
        notificationToggle.setStyle("-fx-font-size: 14; -fx-text-fill: #333333;");
        
        // Slider para porcentaje de desconexión 
        Label disconnectLabel = new Label("Aviso para desconectar al:"); 
        disconnectLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #333333;"); 
        
        disconnectSlider = new Slider(50, 100, 80); 
        disconnectSlider.setShowTickLabels(true); 
        disconnectSlider.setShowTickMarks(true); 
        disconnectSlider.setMajorTickUnit(10); 
        disconnectSlider.setStyle("-fx-accent: #4CAF50;");
        
        // Etiqueta de porcentaje seleccionado 
        Label disconnectValue = new Label("80%"); 
        disconnectValue.setStyle("-fx-font-size: 14; -fx-text-fill: #333333;"); 
        disconnectSlider.valueProperty().addListener((obs, oldVal, newVal) -> { 
        	disconnectValue.setText(String.format("%.0f%%", newVal.doubleValue())); 
        	if (notificationToggle.isSelected()) {
        		checkAndSendNotification(newVal.doubleValue(), true);
            }
        });
        
        VBox disconnectBox = new VBox(5, disconnectLabel, disconnectSlider, disconnectValue);
        
        // Slider para porcentaje de conexión 
        Label connectLabel = new Label("Aviso para conectar al:"); 
        connectLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #333333;"); 
        
        connectSlider = new Slider(0, 50, 20); // Min: 0%, Max: 50%, Valor inicial: 20% 
        connectSlider.setShowTickLabels(true); 
        connectSlider.setShowTickMarks(true); 
        connectSlider.setMajorTickUnit(10); 
        connectSlider.setStyle("-fx-accent: #4CAF50;");
        
        // Etiqueta de porcentaje seleccionado 
        Label connectValue = new Label("20%"); 
        connectValue.setStyle("-fx-font-size: 14; -fx-text-fill: #333333;"); 
        connectSlider.valueProperty().addListener((obs, oldVal, newVal) -> { 
        	connectValue.setText(String.format("%.0f%%", newVal.doubleValue())); 
        	if (notificationToggle.isSelected()) {
        		checkAndSendNotification(newVal.doubleValue(), false);
            }
        });
        
        VBox connectBox = new VBox(5, connectLabel, connectSlider, connectValue);
        
        // Agregar todos los elementos al lado izquierdo 
        leftColumn.getChildren().addAll(batteryIndicator, notificationToggle, disconnectBox, connectBox);
        
        // Lado derecho: Detalles técnicos
        VBox rightColumn = new VBox(10);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setPadding(new Insets(10));
        
        // Creación de etiquetas con iconos, texto y valor dentro de un rectángulo
        rightColumn.getChildren().addAll(
        		createDetailBox("🔋", "Porcentage", batteryPercentage),
        		createDetailBox("🔌", "Power Status", batteryStatus),
        		createDetailBox("⏳", "Remaining Time", remainingTime),
        		createDetailBox("📊", "Design Capacity", designCapacity),
        		createDetailBox("⚡", "Full Charge Capacity", fullChargeCapacity),
                createDetailBox("🚀", "Speed", speedCharge)
        );

        // Contenedor principal para dividir en dos columnas
        HBox mainLayout = new HBox(50, leftColumn, rightColumn);
        mainLayout.setAlignment(Pos.CENTER);

        contentArea.getChildren().add(mainLayout);
    }
    
    // Métodos para gestionar notificaciones
    private void checkAndSendNotification(Double threshold, Boolean disconnect) {
    	double batteryLevel = batteryPercentageNumeric.get();
    	 
    	if (disconnect) {
    		if (isCharging && batteryLevel >= threshold && !hasNotifiedHighBattery) {
    			showNotification("Aviso", "Nivel de batería suficiente. Puedes desconectar.");
    			hasNotifiedHighBattery = true; // Marca que se ha enviado la notificación 
    			hasNotifiedLowBattery = false; // Restablece la otra notificación
			} else if (!isCharging && batteryLevel < threshold) {
				hasNotifiedHighBattery = false; // Restablece la notificación si se desconecta antes de alcanzar el umbral
			} else if (isCharging && batteryLevel < threshold) { 
				hasNotifiedHighBattery = false; // Restablece la notificación si el umbral se ajusta hacia arriba
			}
        } else {
        	if (!isCharging && batteryLevel <= threshold && !hasNotifiedLowBattery) {
        		showNotification("Aviso", "Nivel de batería bajo. Conecta el dispositivo.");
        		hasNotifiedLowBattery = true; // Marca que se ha enviado la notificación 
        		hasNotifiedHighBattery = false; // Restablece la otra notificación
        	} else if (isCharging && batteryLevel > threshold) {
        		hasNotifiedLowBattery = false; // Restablece la notificación si se conecta antes de alcanzar el umbral
        	} else if (!isCharging && batteryLevel > threshold) { 
        		hasNotifiedLowBattery = false; // Restablece la notificación si el umbral se ajusta hacia abajo
        	}
        }
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
    
    // Método para crear cada detalle con icono, texto y valor dentro de un rectángulo
    private HBox createDetailBox(String icon, String label, Label valueLabel) {
    	// Icono
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 14));
        iconLabel.setStyle("-fx-text-fill: white;");
    	
    	// Etiqueta del texto
        Label labelText = new Label(label + " ");
        labelText.setFont(Font.font("Arial", 14));
        labelText.setStyle("-fx-text-fill: white;");

        // Etiqueta del valor
        valueLabel.setFont(Font.font("Arial", 14));
        valueLabel.setStyle("-fx-text-fill: white;");
        
        // Contenedor para el texto y el icono (parte izquierda del rectángulo)
        HBox detailBox = new HBox(10, iconLabel, labelText);
        detailBox.setStyle("-fx-background-color: #4CAF50; -fx-padding: 10; -fx-border-radius: 5;");
        
        // Contenedor para el valor alineado a la derecha (parte derecha del rectángulo)
        HBox rightBox = new HBox(valueLabel);
        rightBox.setStyle("-fx-background-color: #4CAF50;"); // Aseguramos que el valor también tenga fondo verde
        rightBox.setAlignment(Pos.CENTER_RIGHT); // Alineación a la derecha para el valor
        HBox.setHgrow(rightBox, Priority.ALWAYS); // El valor ocupará el espacio disponible en la fila
        
        // Contenedor final con los detalles y el valor a la derecha
        HBox fullDetailBox = new HBox(10, detailBox, rightBox);
        fullDetailBox.setStyle("-fx-background-color: #4CAF50; -fx-padding: 10; -fx-border-radius: 5;");

        return fullDetailBox;
    }

    // Método para mostrar la pantalla de History
    private void showHistory() {
        contentArea.getChildren().clear();
        Label historyLabel = new Label("Battery History");
        historyLabel.setStyle("-fx-font-size: 24; -fx-text-fill: #333333;");
        contentArea.getChildren().add(historyLabel);
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
    
    private void updateBatteryInfo() {
        List<PowerSource> powerSources = hal.getPowerSources();
        if (powerSources.isEmpty()) {
            Platform.runLater(() -> {
                batteryPercentage.setText("Battery Percentage: No battery found");
                percentageCircle.setText("--%");
                batteryPercentageNumeric.set(0);
                isCharging = false;
                batteryStatus.setText("Battery Status: Not available");
                remainingTime.setText("Unknown");
                designCapacity.setText("Unknown");
                fullChargeCapacity.setText("Unknown");
                speedCharge.setText("Unknown");
            });
        } else {
            PowerSource powerSource = powerSources.get(0);
            
            // Obtener capacidad restante y máxima
            double currentCapacity = powerSource.getCurrentCapacity();
            double maxCapacity = powerSource.getMaxCapacity();
            double designCapacityValue = powerSource.getDesignCapacity();
            double power = powerSource.getPowerUsageRate();
            
            // Calcular tiempo restante (si es aplicable)
            double timeRemaining = powerSource.getTimeRemainingInstant();
            String remainingTimeText = (timeRemaining >= 0)
                    ? String.format("%d min", (int) (timeRemaining / 60))
                    : "Unknown";
            
            // Validar que currentCapacity y maxCapacity
            if (currentCapacity > 0 && maxCapacity > 0) {
            	double remainingCapacity = (currentCapacity / maxCapacity) * 100;

                Platform.runLater(() -> {
                	batteryPercentage.setText(String.format("%.0f", remainingCapacity) + "%");
                	percentageCircle.setText(String.format("%.0f", remainingCapacity) + "%");
                	batteryPercentageNumeric.set(remainingCapacity);
                    batteryStatus.setText(powerSource.isCharging() ? "Charging" : "On Battery Power");
                    isCharging = powerSource.isCharging();
                    remainingTime.setText(remainingTimeText);
                    designCapacity.setText(String.format("%.0f Wh", designCapacityValue));
                    fullChargeCapacity.setText(String.format("%.0f Wh", maxCapacity));
                    speedCharge.setText(String.format("%.2f W", power));
                });
                
                Platform.runLater(() -> {
                    if (disconnectSlider != null && connectSlider != null) {
                        checkAndSendNotification(disconnectSlider.getValue(), true);
                        checkAndSendNotification(connectSlider.getValue(), false);
                    }
                });
			} else {
				Platform.runLater(() -> { 
					batteryPercentage.setText("Battery Percentage: Invalid data");
					percentageCircle.setText("--%");
					batteryPercentageNumeric.set(0);
					isCharging = false;
					batteryStatus.setText("Battery Status: Not available");
					remainingTime.setText("Unknown"); 
					designCapacity.setText("Unknown"); 
					fullChargeCapacity.setText("Unknown"); 
					speedCharge.setText("Unknown");
				});
			}
        }
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
	
}
