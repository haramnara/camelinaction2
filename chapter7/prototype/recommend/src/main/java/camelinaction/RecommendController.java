package camelinaction;

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
@ConfigurationProperties(prefix = "recommend")
public class RecommendController {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendController.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private String cartUrl;
    private String rulesUrl;
    private String ratingUrl;

    @RequestMapping(value = "recommend", method = RequestMethod.GET, produces = "application/json")
    @SuppressWarnings("unchecked")
    public List<ItemDto> recommend(HttpSession session) {
        String id = session.getId();
        LOG.info("HTTP session id {}", id);

        // get the current item in the shopping cart
        List<CartDto> carts = restTemplate.getForObject(cartUrl, List.class, id);
        // provide details what we have in the shopping cart for the rules
        String cartIds = null;
        if (carts != null && !carts.isEmpty()) {
            cartIds = carts.stream().map(CartDto::getItemId).collect(Collectors.joining(","));
            LOG.info("Shopping cart items {}", cartIds);
        }

        LOG.info("Calling rules backend {}", rulesUrl);
        List<ItemDto> items = restTemplate.getForObject(rulesUrl, List.class, id, cartIds);

        // gather item ids for rating
        String itemIds = null;
        if (items != null && !items.isEmpty()) {
            itemIds = items.stream().map(item -> "" + item.getItemNo()).collect(Collectors.joining(","));
            LOG.info("Inventory items {}", itemIds);
        }

        LOG.info("Calling rating backend {}", ratingUrl);
        List<RatingDto> ratings = restTemplate.getForObject(ratingUrl, List.class, id, itemIds);

        // append ratings to items to recommend
        for (RatingDto rating : ratings) {
            System.out.println("Rating " + rating);
        }

        return items;
    }

    public String getCartUrl() {
        return cartUrl;
    }

    public void setCartUrl(String cartUrl) {
        this.cartUrl = cartUrl;
    }

    public String getRulesUrl() {
        return rulesUrl;
    }

    public void setRulesUrl(String rulesUrl) {
        this.rulesUrl = rulesUrl;
    }

    public String getRatingUrl() {
        return ratingUrl;
    }

    public void setRatingUrl(String ratingUrl) {
        this.ratingUrl = ratingUrl;
    }
}
