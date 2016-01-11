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
    QuoteService quoteService() {
        return new QuoteService();
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
    LocalRegionFactoryBean<Integer, Integer> quotesRegion(final GemFireCache cache) {
        LocalRegionFactoryBean<Integer, Integer> helloRegion = new LocalRegionFactoryBean<>();
        helloRegion.setClose(false);
        helloRegion.setCache(cache);
        helloRegion.setName("Quotes");
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
        Quote quote = requestQuote(12l);
        requestQuote(quote.getId());
        requestQuote(10l);
    }

    private Quote requestQuote(Long id) {
        QuoteService quoteService = quoteService();
        Quote quote = (id != null ? quoteService.requestQuote(id) : quoteService.requestRandomQuote());
        System.out.printf("Quote is \"%1$s\"; Cache Miss is %2$s%n", quote, quoteService.isCacheMiss());
        return quote;
    }

}
