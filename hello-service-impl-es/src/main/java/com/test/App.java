package com.test;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import co.edu.uis.sistemas.hello.api.HelloService;

@Component(name="HelloServiceComponentEnglish")
@Provides(specifications=HelloService.class)
@Instantiate
public class App implements HelloService {

	public String sayBye(String name) {
		return "Adios " + name;
	}

	public String sayHello(String name) {
		return "Hola " + name;
	}

}
