package rest;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fetch.GetActivationCode;

@RestController
public class TheCodeController {
	private GetActivationCode gac = new GetActivationCode();
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/code")
    public TheCode getCode() {
    	// Read Testy McTesterson's gmail account and find the latest activation email
		String code = gac.fetch();
		TheCode theCode = new TheCode(counter.incrementAndGet(),code); 
		return theCode; 
    }
    
    @RequestMapping("/reset")
    public String reset() {
    	gac._reset();
		return "reset"; 		
    }
}