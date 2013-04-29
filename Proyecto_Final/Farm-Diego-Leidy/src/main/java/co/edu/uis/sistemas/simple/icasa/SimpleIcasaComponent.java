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
	
	
	public static final int MAX_TEMPERATUR_ALLOWED = 300;
	
	public static final int MIN_TEMPERATUR_ALLOWED = 290;
	
	public static final int DEFAULT_DEVICE_POWER_LEVEL = 1;
	
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
	
	/**
	 * Lista de los calentadores disponibles.
	 * 
	 * @return
	 */
	protected List<Heater> getHeaters() {
		return Collections.unmodifiableList(Arrays.asList(heaters));
	}
	
	/**
	 * Lista de los ventiladores disponibles.
	 * 
	 * @return
	 */
	protected List<Cooler> getCoolers() {
		return Collections.unmodifiableList(Arrays.asList(coolers));
	}
	
	/**
	 * Lista de los termómetros disponibles.
	 * 
	 * @return
	 */
	protected List<Thermometer> getThermometers() {
		return Collections.unmodifiableList(Arrays.asList(thermometers));
	}
	
	/**
	 * 
	 * Obtiene los calentadores que se encuentran en la zona indicada.
	 * 
	 * @param locationName zona de interés.
	 * 
	 * @return
	 */
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
	
	/**
	 * 
	 * Obtiene los ventiladores que se encuentran en la zona indicada.
	 * 
	 * @param locationName zona de interés.
	 * 
	 * @return
	 */
	private List<Cooler> getCoolerIn(String locationName){
		List<Cooler> result = new ArrayList<Cooler>();
		if(locationName != null && !locationName.equals(GenericDevice.LOCATION_UNKNOWN)){
			List<Cooler> currentCoolers = getCoolers();
			for (Cooler cooler : currentCoolers) {
				String coolLocation = (String)cooler.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME);
				if(locationName.equals(coolLocation)){
					result.add(cooler);
				}
			}
		}		
		return result;
	}
	
	/**
	 * 
	 * Obtiene los temómetros que se encuentran en la zona indicada.
	 * 
	 * @param locationName zona de interés.
	 * 
	 * @return
	 */
	private List<Thermometer> getThermometerIn(String locationName){
		List<Thermometer> result = new ArrayList<Thermometer>();
		if(locationName != null && !locationName.equals(GenericDevice.LOCATION_UNKNOWN)){
			List<Thermometer> currentThermometers = getThermometers();
			for (Thermometer thermometer : currentThermometers) {
				String thermometerLocation = (String)thermometer.getPropertyValue(GenericDevice.LOCATION_PROPERTY_NAME);
				if(locationName.equals(thermometerLocation)){
					result.add(thermometer);
				}
			}
		}		
		return result;
	}
	
	/**
	 * 
	 * Obtiene todas la zonas que contienen por lo menos un termometro.
	 * 
	 * @return
	 */
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
	 
	/**
	 * Al iniciar el componente se inicia el hilo que se encarga de controlar los ventiladores y calentadores.
	 */
	@Validate
	public void start() {
		checkTemperaturThread= new CheckTemperaturThread();
		checkTemperaturThread.start();
	}
	
	/**
	 * Al terminar el componente se debe finalizar el hilo.
	 */
	@Invalidate
	public void stop() throws InterruptedException {
		checkTemperaturThread.interrupt();
		checkTemperaturThread.join();
		checkTemperaturThread.stopSearch();
	}

	/**
	 * 
	 * Clase usada para monitoraer los temometros hubicadados en cada una de las zonas de la casa y de esta 
	 * forma encender o apagar los calentadores y ventiladores.
	 * 
	 * 
	 * @author Leidy Guarin
	 *
	 */
		
	class CheckTemperaturThread extends Thread  {

		public CheckTemperaturThread() {
	
		}
		
		/**
		 * Bandera usada para controlar la finalización del hilo.
		 * 
		 */
		private volatile boolean running = true;
		
		private boolean isRunning() {
			return running;
		}

		private void setRunning(boolean running) {
			this.running = running;
		}

		/**
		 * Este metodo debe ser llamada para terminar el ejecución del hilo.
		 */
		public void stopSearch(){
			setRunning(false);
		}

		public void run() {
				
			System.out.println("Thread started");
			
			while (isRunning()) {
				
				try {
					//Se obtienen todas las zonas que por lo menos  tienen un termómetro.
					Set<String> zones=getZonesthatcontainThermometers().keySet();
					
					 for(String zone:zones){//Se recorre cada zona.
						 
						 //Se obtiene la temperatura de cada una de las zonas.
						 //Se asume que todos los termómetros de una zona perciben al misma temperatura.
						 double temperatur = (getZonesthatcontainThermometers().get(zone).get(0)).getTemperature();
						 
						 if(temperatur>MAX_TEMPERATUR_ALLOWED){//Si la temperatura es mayor a 300 se debe enfriar la zona.							
							 setPowerLevelToAllCoolers(zone,DEFAULT_DEVICE_POWER_LEVEL);
							 setPowerLevelToAllHeaters(zone,0);
						 }else if(temperatur<MIN_TEMPERATUR_ALLOWED){//Si la temperatura es menor a 290 se debe calentar la zona.	
							 setPowerLevelToAllCoolers(zone,0);
							 setPowerLevelToAllHeaters(zone,DEFAULT_DEVICE_POWER_LEVEL);
						 }else{//En cualquier otro caso no se hace nada.
							 setPowerLevelToAllCoolers(zone,0);
							 setPowerLevelToAllHeaters(zone,0);
						 }
						 
						 getCoolerIn(zone);
						 getHeatersIn(zone);
					 }
					
					Thread.sleep(300);// dormir el hilo por 300 iniciar una nueva revisión.					
				} catch (InterruptedException e) {
					setRunning(false);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			System.out.println("Thread finished");
		}
		
	}

	/**
	 * Cambia el nivel de energía a todos los ventiladores ubicados en la zona indicada.
	 * 
	 * @param zone zona de interés.
	 * @param powerLevel nivel de energía deseado.
	 */
	private void setPowerLevelToAllCoolers(String zone,double powerLevel){
		 //System.out.println("Coolers power level was set to ["+powerLevel+"] in ["+zone+"]");
		 List<Cooler>coolers =  getCoolerIn(zone);
		 for(Cooler cooler: coolers){
			 cooler.setPowerLevel(powerLevel);
		 }
	}
	
	/**
	 * Cambia el nivel de energía a todos los calentadores ubicados en la zona indicada.
	 * 
	 * @param zone zona de interés.
	 * @param powerLevel nivel de energía deseado.
	 */
	private void setPowerLevelToAllHeaters(String zone,double powerLevel){
		//System.out.println("Heaters power level was set to ["+powerLevel+"] in ["+zone+"]");
		List<Heater>heaters =  getHeatersIn(zone);
		 for(Heater heater: heaters){
			 heater.setPowerLevel(powerLevel);
		 }
	}
	
	/**
	 * Listener usado para saber cuando un termómetro se cambia de zona.
	 * 
	 */
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
				
				String previousZona=(String)value;
				
				if(getThermometerIn(previousZona).size() == 0){
					System.out.println("There aren't thermometers in the zone ["+previousZona+"]");
					setPowerLevelToAllCoolers(previousZona,0);
					setPowerLevelToAllHeaters(previousZona,0);
				}
				
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
	
	/**
	 * Listener usado para saber cuando un ventilador se cambia de zona.
	 * Se asume que cuando se cambia de zona hay que reducir su nivel de energía a 0.
	 * 
	 */
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
	
	/**
	 * Listener usado para saber cuando un calentador se cambia de zona.
	 * Se asume que cuando se cambia de zona hay que reducir su nivel de energía a 0.
	 * 
	 */
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
