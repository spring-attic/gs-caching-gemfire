package hello;

import java.util.Properties;

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
import com.gemstone.gemfire.cache.GemFireCache;

@SpringBootApplication
@EnableCaching
@EnableGemfireRepositories
@SuppressWarnings("unused")
public class Application implements CommandLineRunner {

    @Bean
    FacebookLookupService facebookLookupService() {
        return new FacebookLookupService();
    }

    @Bean
    Properties gemfireProperties() {
        Properties gemfireProperties = new Properties();
        gemfireProperties.setProperty("name", "DataGemFireCachingApplication");
        gemfireProperties.setProperty("mcast-port", "0");
        gemfireProperties.setProperty("log-level", "config");
        return gemfireProperties;
    }

    @Bean
    CacheFactoryBean gemfireCache() {
        CacheFactoryBean gemfireCache = new CacheFactoryBean();
        gemfireCache.setProperties(gemfireProperties());
        gemfireCache.setUseBeanFactoryLocator(false);
        return gemfireCache;
    }

    @Bean
    LocalRegionFactoryBean<Integer, Integer> localRegionFactory(final GemFireCache cache) {
        LocalRegionFactoryBean<Integer, Integer> helloRegion = new LocalRegionFactoryBean<>();
        helloRegion.setCache(cache);
        helloRegion.setName("hello");
        helloRegion.setPersistent(false);
        return helloRegion;
    }

    @Bean
    GemfireCacheManager cacheManager(final Cache gemfireCache) {
        GemfireCacheManager cacheManager = new GemfireCacheManager();
        cacheManager.setCache(gemfireCache);
        return cacheManager;
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
