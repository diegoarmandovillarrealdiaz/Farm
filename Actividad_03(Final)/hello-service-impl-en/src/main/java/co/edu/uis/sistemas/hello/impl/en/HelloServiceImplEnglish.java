package co.edu.uis.sistemas.hello.impl.en;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import co.edu.uis.sistemas.hello.api.HelloService;


@Component(name="HelloServiceComponentEnglish")
@Provides(specifications=HelloService.class)
@Instantiate
public class HelloServiceImplEnglish implements HelloService {

	public String sayBye(String name) {
		return "Bye " + name;
	}

	public String sayHello(String name) {
		return "Hello " + name;
	}

}
