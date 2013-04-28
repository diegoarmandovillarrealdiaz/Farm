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



/**
 * 
 * Este componente se encarga de controlar al temperatura en los cuartos en los que se encuentre un termómetro. Haciendo
 * uso de los ventiladores y calentadores disponibles.
 * 
 * @author Leidy Guarin
 *
 */
@Component(name="SimpleIcasaComponent")
@Instantiate
public class SimpleIcasaComponent {
	
	/**
	 * Instancia del hilo encargado de revisar la temperatura y de esta forma encender o apagar ventiladores y calentadores.
	 * 
	 */
	private CheckTemperaturThread checkTemperaturThread;
	
	/**
	 * Arreglo con los calentadores disponibles.
	 */
	@Requires(id="heaters")
	private Heater[] heaters;
	
	/**
	 * Arreglo con los ventiladores disponibles.
	 * 
	 */
	@Requires(id="coolers")
	private Cooler[] coolers;
	
	/**
	 * Arreglo con los termómetros disponibles.
	 * 
	 */
	@Requires(id="thermometers")
	private Thermometer[] thermometers;
	
	/**
	 * Al añadirse un calentador, se le asigna un nivel de energía de 0 y 
	 * se añade un listener para detectar los cambios de zona.
	 *  
	 */
	@Bind(id="heaters")
	protected void bindHeater(Heater heater) {
		heater.setPowerLevel(0);
		heater.addListener(changeHeaterLocationLisener);
	}
	
	/**
	 * Al remover  un calendaros, hay que remover el listener añadido.
	 * 
	 * @param heater
	 */
	@Unbind(id="heaters")
	protected void unBindHeater(Heater heater) {
		heater.removeListener(changeHeaterLocationLisener); 
	}
	
	/**
	 * Al añadirse un ventilador, se le asigna un nivel de energía de 0 y 
	 * se añade un listener para detectar los cambios de zona. 
	 */
	@Bind(id="coolers")
	protected void bindCooler(Cooler cooler) {
		cooler.setPowerLevel(0);
		cooler.addListener(changeCoolerLocationLisener);
	}
	

	/**
	 * Al remover un ventilador, hay que remover el listener añadido.
	 * 
	 * @param heater
	 */
	@Unbind(id="coolers")
	protected void unBindCooler(Cooler cooler) {
		cooler.removeListener(changeCoolerLocationLisener); 
	}
	
	/**
	 * Al añadirse un termómetro, se añade un listener para detectar los cambios de zona. 
	 * 
	 */
	@Bind(id="thermometers")
	protected void bindThermometer(Thermometer thermometer) {
		thermometer.addListener(changeThermometerLocationLisener);	
	}

	/**
	 * Al remover un termómetro, hay que remover el listener añadido y asignar un nivel de energio
	 * de 0 a todos los ventiladores y calentadores.
	 *  
	 * @param heater
	 */
	@Unbind(id="thermometers")
	protected void unBindThermometers(Thermometer thermometer) {
		thermometer.removeListener(changeThermometerLocationLisener);
		String thermometerLocation = (String)thermometer.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME);
		setPowerLevelToAllCoolers(thermometerLocation, 0);
		setPowerLevelToAllHeaters(thermometerLocation, 0);
		
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
	
	private Map<String,List<Thermometer>> getZonesthatcontainThermometers(){
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
	 
	
	@Validate
	public void start() {
		checkTemperaturThread= new CheckTemperaturThread();
		checkTemperaturThread.start();
	}
	
	@Invalidate
	public void stop() throws InterruptedException {
		checkTemperaturThread.interrupt();
		checkTemperaturThread.join();
		checkTemperaturThread.stopSearch();
	}

		
	class CheckTemperaturThread extends Thread  {

		public CheckTemperaturThread() {
	
		}
		
		private volatile boolean running = true;
		
		private boolean isRunning() {
			return running;
		}


		private void setRunning(boolean running) {
			this.running = running;
		}

		public void stopSearch(){
			setRunning(false);
		}

		public void run() {
				
			System.out.println("Thread started");
			
			while (isRunning()) {
				
				try {
					Set<String> zones=getZonesthatcontainThermometers().keySet();
					
					 for(String zone:zones){
						 double temperatur = (getZonesthatcontainThermometers().get(zone).get(0)).getTemperature();
						 
						 if(temperatur>300){
							 setPowerLevelToAllCoolers(zone,1);
							 setPowerLevelToAllHeaters(zone,0);
						 }else if(temperatur<290){
							 setPowerLevelToAllCoolers(zone,0);
							 setPowerLevelToAllHeaters(zone,1);
						 }else{
							 setPowerLevelToAllCoolers(zone,0);
							 setPowerLevelToAllHeaters(zone,0);
						 }
						 
						 getCoolerIn(zone);
						 getHeatersIn(zone);
					 }
					
					Thread.sleep(300);					
				} catch (InterruptedException e) {
					setRunning(false);
				}
			}
			
			System.out.println("Thread finished");
		}
		
	}

	private void setPowerLevelToAllCoolers(String zone,double powerLevel){
		 List<Cooler>coolers =  getCoolerIn(zone);
		 for(Cooler cooler: coolers){
			 cooler.setPowerLevel(powerLevel);
		 }
	}
	
	private void setPowerLevelToAllHeaters(String zone,double powerLevel){
		List<Heater>heaters =  getHeatersIn(zone);
		 for(Heater heater: heaters){
			 heater.setPowerLevel(powerLevel);
		 }
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
				
			}else{
				System.out.println("property: ["+property+"] old value: ["+value+"]");
			}
			
		}
		
		public void devicePropertyAdded(GenericDevice arg0, String arg1) {
			// DO NOTHING
			
		}
		
		public void deviceAdded(GenericDevice arg0) {
			// DO NOTHING
			
		}
	};
	
	private DeviceListener changeCoolerLocationLisener=new DeviceListener() {
		
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
				((Cooler)device).setPowerLevel(0);
				System.out.println("Cooler id: "+device.getSerialNumber()+" , location: "+location+", power level set to 0 ");
				
			}else{
				System.out.println("property: ["+property+"] old value: ["+value+"]");
			}
			
			
		}
		
		public void devicePropertyAdded(GenericDevice arg0, String arg1) {
			// DO NOTHING
			
		}
		
		public void deviceAdded(GenericDevice arg0) {
			// DO NOTHING
			
		}
	};
	
	private DeviceListener changeHeaterLocationLisener=new DeviceListener() {
		
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
				((Heater)device).setPowerLevel(0);
				System.out.println("Heater id: "+device.getSerialNumber()+" , location: "+location+", power level set to 0 ");
				
			}else{
				System.out.println("property: ["+property+"] old value: ["+value+"]");
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
