package hello;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestTemplate;

public class FacebookLookupService {

	RestTemplate restTemplate = new RestTemplate();

	@Cacheable("hello")
	public Page findPage(String page) {
		return restTemplate.getForObject("http://graph.facebook.com/" + page, Page.class);
	}
	
}
