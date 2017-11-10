package hello;

import java.util.Optional;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.cache.config.EnableGemfireCaching;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;

@ClientCacheApplication(name = "CachingGemFireApplication", logLevel = "error")
@EnableGemfireCaching
@SuppressWarnings("unused")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean("Quotes")
    public ClientRegionFactoryBean<Integer, Integer> quotesRegion(GemFireCache gemfireCache) {

        ClientRegionFactoryBean<Integer, Integer> quotesRegion = new ClientRegionFactoryBean<>();

        quotesRegion.setCache(gemfireCache);
        quotesRegion.setClose(false);
        quotesRegion.setShortcut(ClientRegionShortcut.LOCAL);

        return quotesRegion;
    }

    @Bean
    QuoteService quoteService() {
        return new QuoteService();
    }

    @Bean
    ApplicationRunner run() {

        return args -> {
            Quote quote = requestQuote(12L);
            requestQuote(quote.getId());
            requestQuote(10L);
        };
    }

    private Quote requestQuote(Long id) {

        QuoteService quoteService = quoteService();

        long startTime = System.currentTimeMillis();

        Quote quote = Optional.ofNullable(id)
            .map(quoteService::requestQuote)
            .orElseGet(quoteService::requestRandomQuote);

        long elapsedTime = System.currentTimeMillis();

        System.out.printf("\"%1$s\"%nCache Miss [%2$s] - Elapsed Time [%3$s ms]%n", quote,
            quoteService.isCacheMiss(), (elapsedTime - startTime));

        return quote;
    }
}
