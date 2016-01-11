package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The QuoteResponse class models the response from the QuoteService when a quote is requested by an application.
 *
 * @author John Blum
 * @see hello.Quote
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("unused")
public class QuoteResponse {

	@JsonProperty("value")
	private Quote quote;

	@JsonProperty("type")
	private String status;

	public Quote getQuote() {
		return quote;
	}

	public void setQuote(final Quote quote) {
		this.quote = quote;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return String.format("{ @type = %1$s, quote = '%2$s', status = %3$s }",
			getClass().getName(), getQuote(), getStatus());
	}

}
