package hello;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.data.gemfire.support.GemfireCacheManager;

import com.gemstone.gemfire.cache.Cache;

@SpringBootApplication
@EnableCaching
@EnableGemfireRepositories
public class Application implements CommandLineRunner {

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

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        lookupPageAndTimeIt(facebookLookupService(), "SpringSource");
        lookupPageAndTimeIt(facebookLookupService(), "SpringSource");
        lookupPageAndTimeIt(facebookLookupService(), "PivotalSoftware");
    }

    private void lookupPageAndTimeIt(FacebookLookupService bigCalculator ,String page) {
        long start = System.currentTimeMillis();
        Page results = bigCalculator.findPage(page);
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Found " + results + ", and it only took " +
                elapsed + " ms to find out!\n");
    }

}
