package es.codeurjc.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;


/**
 * Service for retrieving Euribor rates from the European Central Bank API.
 */
@Service
public class EuriborService {

    private static final String ECB_API_URL = 
        "https://data-api.ecb.europa.eu/service/data/FM/M.U2.EUR.RT.MM.EURIBOR1YD_.HSTA?format=jsondata&detail=dataonly&lastNObservations=1";

    private final RestTemplate restTemplate;

    public EuriborService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Gets the current Euribor rate from the ECB API.
     * 
     * @return the Euribor rate as a double
     * @throws RuntimeException if the Euribor rate cannot be retrieved from the API
     */
    public double getEuribor() {
        try {
            JsonNode response = restTemplate.getForObject(ECB_API_URL, JsonNode.class);
            
            if (response != null) {
                JsonNode euriborValue = response
                    .path("dataSets").get(0)
                    .path("series")
                    .path("0:0:0:0:0:0:0")
                    .path("observations")
                    .path("0").get(0);
                
                if (euriborValue != null && !euriborValue.isMissingNode()) {
                    return euriborValue.asDouble();
                }
            }
            
            throw new RuntimeException("Euribor data not available in API response");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Euribor rate: " + e.getMessage(), e);
        }
    }
}
