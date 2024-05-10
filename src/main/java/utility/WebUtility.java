package utility;

import com.cloudcipher.cloudcipher_client_v2.authentication.model.AuthenticationResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.Map;

public class WebUtility {

    public String getServerUrl() {
        return System.getenv("SERVER_URL");
    }
    
    public AuthenticationResponse authRequest(String url, MultipartEntityBuilder builder) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setEntity(builder.build());

            HttpResponse response = client.execute(post);
            HttpEntity responseEntity = response.getEntity();

            ObjectMapper mapper = new ObjectMapper();
            if (response.getStatusLine().getStatusCode() != 200) {
                Map<String, String> error = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<>() {});
                throw new RuntimeException(error.get("message"));
            }

            if (responseEntity == null) {
                throw new RuntimeException("Internal server error. Please try again later or contact support");
            }

            return mapper.readValue(EntityUtils.toString(responseEntity), AuthenticationResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
