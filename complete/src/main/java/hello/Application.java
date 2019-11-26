package hello;

import java.util.Optional;

import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.cache.config.EnableGemfireCaching;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions;

@SpringBootApplication
@ClientCacheApplication(name = "CachingGemFireApplication", logLevel = "error")
@EnableCachingDefinedRegions(clientRegionShortcut = ClientRegionShortcut.LOCAL)
@EnableGemfireCaching
@SuppressWarnings("unused")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	ApplicationRunner runner(QuoteService quoteService) {

		return args -> {
			Quote quote = requestQuote(quoteService, 12L);
			requestQuote(quoteService, quote.getId());
			requestQuote(quoteService, 10L);
		};
	}

	private Quote requestQuote(QuoteService quoteService, Long id) {

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
