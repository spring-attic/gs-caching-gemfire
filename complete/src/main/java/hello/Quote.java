package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.util.ObjectUtils;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("unused")
public class Quote {

	private Long id;

	private String quote;

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Quote)) {
			return false;
		}

		Quote that = (Quote) obj;

		return ObjectUtils.nullSafeEquals(this.getId(), that.getId());
	}

	@Override
	public int hashCode() {

		int hashValue = 17;

		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getId());

		return hashValue;
	}

	@Override
	public String toString() {
		return getQuote();
	}
}
