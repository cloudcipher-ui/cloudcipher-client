module com.cloudcipher.cloudcipher_client {
    requires javafx.fxml;

    requires static lombok;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpmime;
    requires atlantafx.base;

    opens com.cloudcipher.cloudcipher_client to javafx.fxml;
    exports com.cloudcipher.cloudcipher_client;

    opens com.cloudcipher.cloudcipher_client.authentication to javafx.fxml;
    exports com.cloudcipher.cloudcipher_client.authentication;

    opens com.cloudcipher.cloudcipher_client.authentication.model to javafx.fxml;
    exports com.cloudcipher.cloudcipher_client.authentication.model;

    opens com.cloudcipher.cloudcipher_client.file to javafx.fxml;
    exports com.cloudcipher.cloudcipher_client.file;

    opens com.cloudcipher.cloudcipher_client.tool to javafx.fxml;
    exports com.cloudcipher.cloudcipher_client.tool;
}