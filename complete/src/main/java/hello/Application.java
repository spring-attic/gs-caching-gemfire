package hello;

import java.io.IOException;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.data.gemfire.support.GemfireCacheManager;

import com.gemstone.gemfire.cache.Cache;

@Configuration
@EnableCaching
@EnableGemfireRepositories
public class Application {
	
	@Bean
	FacebookLookupService facebookLookupService() {
		return new FacebookLookupService();
	}
		
	@Bean
	CacheFactoryBean cacheFactoryBean() {
		return new CacheFactoryBean();
	}

	@Bean
	LocalRegionFactoryBean<Integer, Integer> localRegionFactoryBean(final Cache cache) {
		return new LocalRegionFactoryBean<Integer, Integer>() {{
			setCache(cache);
			setName("hello");
		}};
	}

	@Bean
	GemfireCacheManager cacheManager(final Cache gemfireCache) {
		return new GemfireCacheManager() {{
			setCache(gemfireCache);
		}};
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);
		
		FacebookLookupService facebookLookupService = ctx.getBean(FacebookLookupService.class);
		
		lookupPageAndTimeIt(facebookLookupService, "SpringSource");
		lookupPageAndTimeIt(facebookLookupService, "SpringSource");

		lookupPageAndTimeIt(facebookLookupService, "gopivotal");

		ctx.close();	
	}

	private static void lookupPageAndTimeIt(FacebookLookupService bigCalculator ,String page) {
		long start = System.currentTimeMillis();
		Page results = bigCalculator.findPage(page);
		long elapsed = System.currentTimeMillis() - start;
		System.out.println("Found " + results + ", and it only took " + 
				elapsed + " ms to find out!\n");
	}
}
