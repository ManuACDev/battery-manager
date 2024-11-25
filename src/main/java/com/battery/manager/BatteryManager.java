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
            double currentCapacity = powerSource.getCurrentCapacity();
            double maxCapacity = powerSource.getMaxCapacity();
            double remainingCapacityPercent = (currentCapacity / maxCapacity) * 100;
            System.out.println("Battery percentage: " + String.format("%.0f", remainingCapacityPercent) + "%");
            
            // Más detalles sobre la batería
            System.out.println("Battery full capacity: " + maxCapacity + " mWh");
            System.out.println("Battery current capacity: " + currentCapacity + " mWh");
            System.out.println("Battery design capacity: " + powerSource.getDesignCapacity() + " mWh");
        }
		
		// Información del sistema operativo
		OperatingSystem os = systemInfo.getOperatingSystem();
		System.out.println("Operating System: " + os);
		System.out.println("" + os.getVersionInfo());
	}

}
