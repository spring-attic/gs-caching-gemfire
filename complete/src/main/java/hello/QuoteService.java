package hello;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("unused")
@Service
public class QuoteService {

	protected static final String ID_BASED_QUOTE_SERVICE_URL = "http://gturnquist-quoters.cfapps.io/api/{id}";
	protected static final String RANDOM_QUOTE_SERVICE_URL = "http://gturnquist-quoters.cfapps.io/api/random";

	private volatile boolean cacheMiss = false;

	private final RestTemplate quoteServiceTemplate = new RestTemplate();

	/**
	 * Determines whether the previous service method invocation resulted in a cache miss.
	 *
	 * @return a boolean value indicating whether the previous service method invocation resulted in a cache miss.
	 */
	public boolean isCacheMiss() {
		boolean cacheMiss = this.cacheMiss;
		this.cacheMiss = false;
		return cacheMiss;
	}

	protected void setCacheMiss() {
		this.cacheMiss = true;
	}

	/**
	 * Requests a quote with the given identifier.
	 *
	 * @param id the identifier of the {@link Quote} to request.
	 * @return a {@link Quote} with the given ID.
	 */
	@Cacheable("Quotes")
	public Quote requestQuote(Long id) {
		setCacheMiss();
		return requestQuote(ID_BASED_QUOTE_SERVICE_URL, Collections.singletonMap("id", id));
	}

	/**
	 * Requests a random quote.
	 *
	 * @return a random {@link Quote}.
	 */
	@CachePut(cacheNames = "Quotes", key = "#result.id")
	public Quote requestRandomQuote() {
		setCacheMiss();
		return requestQuote(RANDOM_QUOTE_SERVICE_URL);
	}

	protected Quote requestQuote(String URL) {
		return requestQuote(URL, Collections.emptyMap());
	}

	protected Quote requestQuote(String URL, Map<String, Object> urlVariables) {

		return Optional.ofNullable(this.quoteServiceTemplate.getForObject(URL, QuoteResponse.class, urlVariables))
			.map(QuoteResponse::getQuote)
			.orElse(null);
	}
}
