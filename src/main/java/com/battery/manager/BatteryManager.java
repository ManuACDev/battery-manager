package com.battery.manager;

import java.util.List;

import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;
import oshi.software.os.OperatingSystem;

public class BatteryManager {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SystemInfo systemInfo = new SystemInfo();
		HardwareAbstractionLayer hal = systemInfo.getHardware();
		
		// Obtener la informaión del sistema
		ComputerSystem computerSystem = hal.getComputerSystem();
		System.out.println("System manufacturer: " + computerSystem.getManufacturer());
		System.out.println("System model: " + computerSystem.getModel());
		
		// Obtener información de la batería
		List<PowerSource> powerSources = hal.getPowerSources();
		if (powerSources.isEmpty()) {
            System.out.println("No battery information available.");
        } else {
            // Suponemos que la primera fuente de poder es la batería
            PowerSource powerSource = powerSources.get(0);
            System.out.println("Battery status: " + (powerSource.isCharging() ? "Charging" : "Not Charging"));
            
            // Calcular el porcentaje de batería
            double remainingCapacityPercent = calculateBatteryPercentage(powerSource);
            System.out.println("Battery percentage: " + String.format("%.0f", remainingCapacityPercent) + "%");
            
            // Más detalles sobre la batería
            System.out.println("Battery full capacity: " + powerSource.getMaxCapacity() + " mWh");
            System.out.println("Battery current capacity: " + powerSource.getCurrentCapacity() + " mWh");
            System.out.println("Battery design capacity: " + powerSource.getDesignCapacity() + " mWh");
            
            // Control de carga y alertas
            controlBatteryCharge(remainingCapacityPercent);
            checkBatteryAlerts(remainingCapacityPercent);
        }
		
		// Información del sistema operativo
		OperatingSystem os = systemInfo.getOperatingSystem();
		System.out.println("Operating System: " + os);
		System.out.println("" + os.getVersionInfo());
	}
	
	// Calcula el porcentaje de batería basado en la capacidad actual y la máxima
    private static double calculateBatteryPercentage(PowerSource powerSource) {
        double currentCapacity = powerSource.getCurrentCapacity();
        double maxCapacity = powerSource.getMaxCapacity();
        return (currentCapacity / maxCapacity) * 100;
    }
    
    // Controla el estado de la carga basado en el porcentaje de batería
    private static void controlBatteryCharge(double remainingCapacityPercent) {
        if (remainingCapacityPercent < 20) {
            System.out.println("Battery is low. Charging...");
        } else if (remainingCapacityPercent >= 80) {
            System.out.println("Battery is nearly full. Using AC power.");
        } else {
            System.out.println("Battery at " + String.format("%.0f", remainingCapacityPercent) + "%. Manage battery usage accordingly.");
        }
    }
    
    // Verifica y muestra alertas sobre el nivel de batería
    private static void checkBatteryAlerts(double remainingCapacityPercent) {
        if (remainingCapacityPercent <= 20) {
            System.out.println("Warning: Battery is below 20%. Please charge your device.");
        }
        if (remainingCapacityPercent >= 95) {
            System.out.println("Warning: Battery is nearly full. Consider unplugging to preserve battery life.");
        }
    }

}
