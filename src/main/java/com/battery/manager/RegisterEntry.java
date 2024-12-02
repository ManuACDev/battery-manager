package com.battery.manager;

public class RegisterEntry {
	private String icon; 
	private String date; 
	private String percentage; 
	private String time; 
	
	public RegisterEntry(String icon, String date, String percentage, String time) { 
		this.icon = icon; 
		this.date = date; 
		this.percentage = percentage; 
		this.time = time; 
	} 
	
	public String getIcon() { 
		return icon; 
	} 
	
	public String getDate() { 
		return date; 
	} 
	
	public String getPercentage() { 
		return percentage; 
	} 
	
	public String getTime() { 
		return time; 
	}
}
