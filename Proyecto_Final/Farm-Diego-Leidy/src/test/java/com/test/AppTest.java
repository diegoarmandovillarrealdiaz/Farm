package com.test;

import co.edu.uis.sistemas.simple.icasa.SimpleIcasaComponent;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }
    
    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    
    /**
     * Check if the basic classes are available.
     *
     * @param testName name of the test case
     */
    public void testBasicClasses()
    {
    	Class<?> h = Heater.class;
    	Class<?> c = Cooler.class;
    	Class<?> t = Thermometer.class;
    	
    	assertTrue(true);//So far, so good.
    	
    }
    
    /**
     * GenericDevice.LOCATION_UNKNOWN should be different to a regular name. 
     *
     */
    public void testLocationUnknown()
    {
    	String unknown = GenericDevice.LOCATION_UNKNOWN;
    	String testName = "Device A";
    	
    	assertFalse(unknown.equals(testName));
    		
    }
    
    /**
     * Check the maximum and minimum temperature values 
     *
     */
    public void testCheckMaxAndMin()
    {
    	assertEquals(SimpleIcasaComponent.MAX_TEMPERATUR_ALLOWED, 300);
    	assertEquals(SimpleIcasaComponent.MIN_TEMPERATUR_ALLOWED, 290);
    		
    }
    
    
    /**
     * The default device power level should be different of 0. 
     *
     */
    public void testCheckMaxDevicePowerlevel()
    {
    	assertTrue(SimpleIcasaComponent.DEFAULT_DEVICE_POWER_LEVEL != 0);
    		
    }
}
