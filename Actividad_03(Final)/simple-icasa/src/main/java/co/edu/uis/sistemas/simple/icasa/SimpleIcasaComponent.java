package co.edu.uis.sistemas.simple.icasa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;


@Component(name="SimpleIcasaComponent")
@Instantiate
public class SimpleIcasaComponent {
	
	@Requires(id="heaters")
	private Heater[] heaters;
	
	@Requires(id="coolers")
	private Cooler[] coolers;
	
	@Requires(id="thermometers")
	private Thermometer[] thermometers;
	
	private Thread modifyLightsThread;
	
	
	@Bind(id="heaters")
	protected void bindHeater(Heater heater) {
		
	}
	
	@Bind(id="coolers")
	protected void bindCooler(Cooler cooler) {
		
	}
	
	@Bind(id="thermometers")
	protected void bindThermometer(Thermometer thermometer) {
		thermometer.addListener(changeThermometerLocationLisener);
		
	}

	@Unbind(id="thermometers")
	protected void unBindHeater(Thermometer thermometer) {
		thermometer.removeListener(changeThermometerLocationLisener);
		System.out.println("it was removed a thermometer" + thermometer.getSerialNumber());
	}
	
	private List<Heater> getHeatersIn(String locationName){
		List<Heater> result = new ArrayList<Heater>();
		if(locationName != null && !locationName.equals(GenericDevice.LOCATION_UNKNOWN)){
			List<Heater> currentHeaters = getHeaters();
			
			for (Heater heater : currentHeaters) {
				String heaterLocation = (String)heater.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME);
				if(locationName.equals(heaterLocation)){
					result.add(heater);
				}
			}
		}
		
		return result;
	}
	
	private List<Cooler> getCoolerIn(String locationName){
		List<Cooler> result = new ArrayList<Cooler>();
		if(locationName != null && !locationName.equals(GenericDevice.LOCATION_UNKNOWN)){
			List<Cooler> currentCoolers = getCoolers();
			for (Cooler cooler : currentCoolers) {
				String heaterLocation = (String)cooler.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME);
				if(locationName.equals(heaterLocation)){
					result.add(cooler);
				}
			}
		}		
		return result;
	}
	
	
	
	private Map<String,List<Thermometer>> getAreasWithThermometer(){
		Map<String,List<Thermometer>> result = new HashMap<String, List<Thermometer>>();
		List<Thermometer> currentThermometers = getThermometers();
		for (Thermometer thermometer : currentThermometers) {
			String heaterLocation = (String)thermometer.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME);
			if(!result.containsKey(heaterLocation)){
				result.put(heaterLocation, new ArrayList<Thermometer>());
			}
			result.get(heaterLocation).add(thermometer);
		}
		
		return result;
	}
	
	protected List<Heater> getHeaters() {
		return Collections.unmodifiableList(Arrays.asList(heaters));
	}
	
	
	
	protected List<Cooler> getCoolers() {
		return Collections.unmodifiableList(Arrays.asList(coolers));
	}
	
	protected List<Thermometer> getThermometers() {
		return Collections.unmodifiableList(Arrays.asList(thermometers));
	}
	
	Thread searchAreasWithThermometersRunnable;
	
	@Validate
	public void start() {
		searchAreasWithThermometersRunnable= new Thread(new SearchAreasWithThermometersRunnable());
		searchAreasWithThermometersRunnable.start();
	}
	
	@Invalidate
	public void stop() throws InterruptedException {
		modifyLightsThread.interrupt();
		modifyLightsThread.join();
		
		searchAreasWithThermometersRunnable.interrupt();
		searchAreasWithThermometersRunnable.join();
	}

	
	public void increaseAllHeater(double increase){
		List<Heater> heaters = getHeaters();
		for (Heater het : heaters) {
			het.setPowerLevel(increase);
		}
	}
	
		
	class SearchAreasWithThermometersRunnable implements Runnable {

		public void run() {
				
			System.out.println("Start Thread");
			boolean running = true;
			
			boolean onOff = false;
			while (running) {
				
				onOff = !onOff;
				try {
					Set<String> areas=getAreasWithThermometer().keySet();
					
					
					 for(String tem:areas){
						 double temperatur = (getAreasWithThermometer().get(tem).get(0)).getTemperature();
						 if(temperatur>300){
							 List<Cooler>coolers =  getCoolerIn(tem);
							 for(Cooler cooler: coolers){
								 cooler.setPowerLevel(0.5);
							 }
							 List<Heater>heaters =  getHeatersIn(tem);
							 for(Heater heater: heaters){
								 heater.setPowerLevel(0);
							 }
							 
							 
						 }else if(temperatur<290){
							 List<Cooler>coolers =  getCoolerIn(tem);
							 for(Cooler cooler: coolers){
								 cooler.setPowerLevel(0);
							 }
							 List<Heater>heaters =  getHeatersIn(tem);
							 for(Heater heater: heaters){
								 heater.setPowerLevel(0.5);
							 }
						 }else{
							 List<Cooler>coolers =  getCoolerIn(tem);
							 for(Cooler cooler: coolers){
								 cooler.setPowerLevel(0);
							 }
							 List<Heater>heaters =  getHeatersIn(tem);
							 for(Heater heater: heaters){
								 heater.setPowerLevel(0);
							 }
						 }
						 
						 getCoolerIn(tem);
						 getHeatersIn(tem);
					 }
					
					Thread.sleep(200);					
				} catch (InterruptedException e) {
					running = false;
				}
			}
			
			 
			
		}
		
	}

	
	private void showInConsoleHeatersAndLightsIn(String location){
		List<Heater> heaters = getHeatersIn(location);
		
		String heatersMessage = "("+heaters.size()+") Heaters in ["+location+"] with the serial Numbers: ";
		
		for(Heater heater:heaters){
			heatersMessage+=heater.getSerialNumber()+".";
		}
		
		System.out.println(heatersMessage);
		
	}
	
	private DeviceListener changeThermometerLocationLisener=new DeviceListener() {
		
		public void deviceRemoved(GenericDevice arg0) {
			// DO NOTHING
			
		}
		
		public void devicePropertyRemoved(GenericDevice arg0, String arg1) {
			// DO NOTHING
			
		}
		
		public void devicePropertyModified(GenericDevice device, String property,
				Object value) {
			
			if(property != null && property.equals(GenericDevice.LOCATION_PROPERTY_NAME)){
				
				String location=(String)device.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME);
				System.out.println("Thermometer id: "+device.getSerialNumber()+" , location: "+location);
				
				showInConsoleHeatersAndLightsIn(location);
			}else{
				System.out.println("property: ["+property+"] value: ["+value+"]");
			}
			
			
		}
		
		public void devicePropertyAdded(GenericDevice arg0, String arg1) {
			// DO NOTHING
			
		}
		
		public void deviceAdded(GenericDevice arg0) {
			// DO NOTHING
			
		}
	};
	
}
