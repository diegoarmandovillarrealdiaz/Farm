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
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;


@Component(name="SimpleIcasaComponent")
@Instantiate
public class SimpleIcasaComponent {
	
	@Requires(id="lights")
	private BinaryLight[] lights;
	
	@Requires(id="heaters")
	private Heater[] heaters;
	
	@Requires(id="coolers")
	private Cooler[] coolers;
	
	@Requires(id="thermometers")
	private Thermometer[] thermometers;
	
	private Thread modifyLightsThread;
	
	@Bind(id="lights")
	protected void bindLight(BinaryLight light) {
		System.out.println("A new light has been added to the platform " + light.getSerialNumber());
	}
	
	@Bind(id="heaters")
	protected void bindHeater(Heater heater) {
		System.out.println("A new heater has been added to the platform " + heater.getSerialNumber());
	}
	
	@Bind(id="coolers")
	protected void bindCooler(Cooler cooler) {
		System.out.println("A new cooler has been added to the platform " + cooler.getSerialNumber());
	}
	
	@Bind(id="thermometers")
	protected void bindThermometer(Thermometer thermometer) {
		thermometer.addListener(changeThermometerLocationLisener);
		System.out.println("A new thermometer has been added to the platform " + thermometer.getSerialNumber());
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
			System.out.println("Heaters number: "+currentHeaters.size());
			System.out.println("Location: "+locationName);
			for (Heater heater : currentHeaters) {
				String heaterLocation = (String)heater.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME);
				if(locationName.equals(heaterLocation)){
					result.add(heater);
				}
			}
		}{
			System.out.println("Curioso-location:"+locationName);
		}
		
		
		return result;
	}
	
	private List<Cooler> getCoolerIn(String locationName){
		List<Cooler> result = new ArrayList<Cooler>();
		if(locationName != null && !locationName.equals(GenericDevice.LOCATION_UNKNOWN)){
			List<Cooler> currentCoolers = getCoolers();
			System.out.println("Coolers number: "+currentCoolers.size());
			System.out.println("Location: "+locationName);
			for (Cooler cooler : currentCoolers) {
				String heaterLocation = (String)cooler.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME);
				if(locationName.equals(heaterLocation)){
					result.add(cooler);
				}
			}
		}{
			System.out.println("Curioso-location:"+locationName);
		}
		
		
		return result;
	}
	
	
	private List<BinaryLight> getLightsIn(String locationName){
		List<BinaryLight> result = new ArrayList<BinaryLight>();
		if(locationName != null && !locationName.equals(GenericDevice.LOCATION_UNKNOWN)){
			List<BinaryLight> currentHeaters = getLights();
			for (BinaryLight heater : currentHeaters) {
				String heaterLocation = (String)heater.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME);
				if(locationName.equals(heaterLocation)){
					result.add(heater);
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
	
	protected List<BinaryLight> getLights() {
		return Collections.unmodifiableList(Arrays.asList(lights));
	}
	
	protected List<Cooler> getCoolers() {
		return Collections.unmodifiableList(Arrays.asList(coolers));
	}
	
	protected List<Thermometer> getThermometers() {
		return Collections.unmodifiableList(Arrays.asList(thermometers));
	}
	
	
	@Validate
	public void start() {
		modifyLightsThread = new Thread(new ModifyLigthsRunnable());
		modifyLightsThread.start();
	}
	
	@Invalidate
	public void stop() throws InterruptedException {
		modifyLightsThread.interrupt();
		modifyLightsThread.join();
	}

	
	public void increaseAllHeater(double increase){
		List<Heater> heaters = getHeaters();
		for (Heater het : heaters) {
			het.setPowerLevel(increase);
		}
	}
	
	class ModifyLigthsRunnable implements Runnable {

		public void run() {
						
			boolean running = true;
			double step= 0.1;
			double total = 0;
			double max=1;
			boolean onOff = false;
			while (running) {
				onOff = !onOff;
				try {
					List<BinaryLight> lights = getLights();
					for (BinaryLight binaryLight : lights) {
						binaryLight.setPowerStatus(onOff);
					}
					if(total<(max-step)){
						total+=step;
					}
					else{
						total=0;
					}
					increaseAllHeater(0.8);
					Thread.sleep(1000);					
				} catch (InterruptedException e) {
					running = false;
				}
			}
			
		}
		
	}
	
	class SearchAreasWithThermometersRunnable implements Runnable {

		public void run() {
				
			
			boolean running = true;

			boolean onOff = false;
			while (running) {
				onOff = !onOff;
				try {
					Set<String> areas=getAreasWithThermometer().keySet();
					 for(String tem:areas){
						 double temperatur = ((Thermometer)getAreasWithThermometer().get(tem)).getTemperature();
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
					Thread.sleep(1000);					
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
		
		List<BinaryLight> lights = getLightsIn(location);
		
		String lightsMessage = "("+lights.size()+") Lights in ["+location+"] with the serial Numbers: ";
		
		for(BinaryLight light:lights){
			lightsMessage+=light.getSerialNumber()+".";
		}
		
		System.out.println(lightsMessage);
		
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
