package hello;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestTemplate;

/**
 * TODO replace service with Greg Turnquist's CF Quotes (of the Day) Service...
 *
 * From Greg...
 *
 * Regarding Facebook, it might be better to pick an alternative service to cache against.
 * Checkout http://gturnquist-quoters.cfapps.io/api, a service a created in PWS.
 * There is:
 *
 * /api to get all quotes
 * /api/random for a random quote
 * /api/{id} for specific quote
 * @deprecated see new {@link QuoteService} instead.
 */
@Deprecated
public class FacebookLookupService {

    private RestTemplate restTemplate = new RestTemplate();

    @Cacheable("hello")
    public Page findPage(String page) {
        return restTemplate.getForObject("http://graph.facebook.com/" + page, Page.class);
    }

}
