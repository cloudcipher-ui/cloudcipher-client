package com.cloudcipher.cloudcipher_client.utility;

import com.cloudcipher.cloudcipher_client.Globals;
import com.cloudcipher.cloudcipher_client.authentication.model.AuthenticationResponse;
import com.cloudcipher.cloudcipher_client.file.model.ShareResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public class WebUtility {

    private static CloseableHttpClient createClient() {
        try {
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);

            Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("https", sslsf)
                            .register("http", new PlainConnectionSocketFactory())
                            .build();

            BasicHttpClientConnectionManager connectionManager =
                    new BasicHttpClientConnectionManager(socketFactoryRegistry);

            return HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(connectionManager).build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpEntity postRequest(CloseableHttpClient client, String url, MultipartEntityBuilder builder) {
        try {
            HttpPost post = new HttpPost(url);
            post.setEntity(builder.build());

            HttpResponse response = client.execute(post);
            HttpEntity responseEntity = response.getEntity();

            if (response.getStatusLine().getStatusCode() != 200) {
                String responseString = EntityUtils.toString(responseEntity);
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> error = mapper.readValue(responseString, new TypeReference<>() {
                });

                throw new RuntimeException(error.get("detail"));
            }

            return responseEntity;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpEntity getRequest(CloseableHttpClient client, String url) {
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            HttpEntity responseEntity = response.getEntity();

            if (response.getStatusLine().getStatusCode() != 200) {
                String responseString = EntityUtils.toString(responseEntity);
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> error = mapper.readValue(responseString, new TypeReference<>() {
                });

                throw new RuntimeException(error.get("detail"));
            }

            return responseEntity;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuthenticationResponse authRequest(String type, String username, String password) {
        String url = Globals.getServerUrl() + type;
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addTextBody("username", username)
                .addTextBody("password", password);

        try (CloseableHttpClient client = createClient()) {
            HttpEntity responseEntity = postRequest(client, url, builder);
            if (responseEntity == null) {
                throw new RuntimeException("Internal server error. Please try again later or contact support");
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(EntityUtils.toString(responseEntity), AuthenticationResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, String>> listRequest(String username, String token) {
        String url = Globals.getServerUrl() + "/list";
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addTextBody("username", username)
                .addTextBody("token", token);

        try (CloseableHttpClient client = createClient()) {
            HttpEntity responseEntity = postRequest(client, url, builder);

            if (responseEntity == null) {
                throw new RuntimeException("Internal server error. Please try again later or contact support");
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void deleteRequest(String username, String token, String filename) {
        String url = Globals.getServerUrl() + "/delete";
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addTextBody("username", username)
                .addTextBody("token", token)
                .addTextBody("filename", filename);

        try (CloseableHttpClient client = createClient()) {
            postRequest(client, url, builder);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static byte[] downloadRequest(String username, String token, String filename) {
        String url = Globals.getServerUrl() + "/download";
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addTextBody("username", username)
                .addTextBody("token", token)
                .addTextBody("filename", filename);

        try (CloseableHttpClient client = createClient()) {
            HttpEntity responseEntity = postRequest(client, url, builder);

            if (responseEntity == null) {
                throw new RuntimeException("Internal server error. Please try again later or contact support");
            }

            return EntityUtils.toByteArray(responseEntity);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static ShareResponse shareLocalRequest(String filename, byte[] encryptedFileBytes, byte[] iv, String rg, int[][] key) {
        String url = Globals.getServerUrl() + "/reencrypt/local";
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addBinaryBody("file", encryptedFileBytes, ContentType.APPLICATION_OCTET_STREAM, filename)
                .addBinaryBody("iv", iv, ContentType.APPLICATION_OCTET_STREAM, "iv")
                .addTextBody("rg", rg);

        return getShareResponse(key, url, builder);
    }

    private static ShareResponse getShareResponse(int[][] key, String url, MultipartEntityBuilder builder) {
        try (CloseableHttpClient client = createClient()) {
            HttpEntity responseEntity = postRequest(client, url, builder);

            if (responseEntity == null) {
                throw new RuntimeException("Internal server error. Please try again later or contact support");
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<>() {
            });
            byte[] reencryptedFile = ConversionUtility.byteArrayFromBase64((String) responseMap.get("fileBytes"));
            byte[] reencryptedIv = ConversionUtility.byteArrayFromBase64((String) responseMap.get("ivBytes"));
            String shareId = (String) responseMap.get("shareId");

            return new ShareResponse(shareId, reencryptedFile, reencryptedIv, key);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static ShareResponse shareCloudRequest(String username, String token, String filename, String rg, int[][] key) {
        String url = Globals.getServerUrl() + "/reencrypt/cloud";
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addTextBody("username", username)
                .addTextBody("token", token)
                .addTextBody("filename", filename)
                .addTextBody("rg", rg);

        return getShareResponse(key, url, builder);
    }

    public static String uploadRequest(String username, String token, byte[] encryptedFileBytes, byte[] iv, String filename) {
        String url = Globals.getServerUrl() + "/upload";
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addTextBody("username", username)
                .addTextBody("token", token)
                .addBinaryBody("file", encryptedFileBytes, org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM, filename)
                .addBinaryBody("iv", iv, org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM, "iv");

        try (CloseableHttpClient client = createClient()) {
            HttpEntity responseEntity = postRequest(client, url, builder);

            if (responseEntity == null) {
                throw new RuntimeException("Internal server error. Please try again later or contact support");
            }

            return EntityUtils.toString(responseEntity);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Map<String, Object> receiveRequest(String shareId) {
        String url = Globals.getServerUrl() + "/receive/" + shareId;
        try (CloseableHttpClient client = createClient()) {
            HttpEntity responseEntity = getRequest(client, url);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(EntityUtils.toString(responseEntity), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
