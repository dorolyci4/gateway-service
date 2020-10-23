package ca.socom.gatewayservice;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.ribbon.proxy.annotation.Hystrix;

@SpringBootApplication
@EnableHystrix
public class GatewayServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}
	
	@Bean
	RouteLocator staticRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				//Countries
				.route(r->r
						.path("/restcountries/**")
						.filters(f->f                                            // filtres
								.addRequestHeader("x-rapidapi-host", "restcountries-v1.p.rapidapi.com")
								.addRequestHeader("x-rapidapi-key", "8e8639e11dmshf4622c39a72adaap18a032jsnab49f0772858")
								.addRequestHeader("useQueryString", "true")
								.rewritePath("/restcountries/(?<segment>.*)", "/${segment}")  // re-ecriture
						        .hystrix(h->h.setName("countries").setFallbackUri("forward:/defaultCountries"))
						 )
						
						.uri("https://restcountries-v1.p.rapidapi.com").id("r1")) 
				//http://localhost:8888/ restcountries/all
				// Muslim
				.route(r->r
				.path("/muslim/**")
				.filters(f->f                                            // filtres
						.addRequestHeader("x-rapidapi-host", "muslimsalat.p.rapidapi.com")
						.addRequestHeader("x-rapidapi-key", "8e8639e11dmshf4622c39a72adaap18a032jsnab49f0772858")
						.addRequestHeader("useQueryString", "true")
						.rewritePath("/muslim/(?<segment>.*)", "/${segment}")  // re-ecriture
						 .hystrix(h->h.setName("muslimsalat").setFallbackUri("forward:/defaultSalat"))
						 )
				
				.uri("https://muslimsalat.p.rapidapi.com").id("r2")) 
				
				//https://muslimsalat.p.rapidapi.com/(location)/(times)/(date)/(daylight)/(method).json
				.build();
	}
	
	@Bean
	DiscoveryClientRouteDefinitionLocator dynamicRoutes(ReactiveDiscoveryClient rdc, DiscoveryLocatorProperties dlp){
		return new DiscoveryClientRouteDefinitionLocator(rdc, dlp);
	}
	
}
// Circuit ouvert et ferme
@RestController 
class CircuitBreakerRestController{
	@GetMapping("/defaultCountries")
	public Map<String, String> countries(){
		Map<String, String>  data=new HashMap<>();
		data.put("message","default Countries");
		//data.put("countries","Maroc, Algerie, Tunisie, Sénégal ,Côte d'ivoire, .......");
		data.put("countries","........");
		data.put("CI","Cote d'ivoire");
		data.put("MA","Maroc");
	
		return data;
	}
	
	@GetMapping("/defaultSalat")
	public Map<String, String> salat(){
		Map<String, String>  data=new HashMap<>();
		data.put("message","Horaire Salawt En Nwakchout ");
		data.put("Majr","7:00");
		data.put("Addohr","14:00, Algerie, Tunisie, .......");
		return data;
	}
}


