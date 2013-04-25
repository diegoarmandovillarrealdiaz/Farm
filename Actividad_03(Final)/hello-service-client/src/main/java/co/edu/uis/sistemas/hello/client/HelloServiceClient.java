package co.edu.uis.sistemas.hello.client;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import co.edu.uis.sistemas.hello.api.HelloService;


@Component(name="HelloServiceClientComp")
@Instantiate
public class HelloServiceClient {

	@Requires
	private HelloService helloService;
	
	@Validate
	public void start() {
		String greeting = helloService.sayHello("Gabo");
		System.out.println("----------->  " + greeting);
	}
	
	@Invalidate
	public void stop() {
		String greeting = helloService.sayBye("Gabo");
		System.out.println("----------->  " + greeting);		
	}
	
	
}
