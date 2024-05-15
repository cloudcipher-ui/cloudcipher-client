module com.cloudcipher.cloudcipher_client_v2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires static lombok;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpmime;

    opens com.cloudcipher.cloudcipher_client_v2 to javafx.fxml;
    exports com.cloudcipher.cloudcipher_client_v2;

    opens com.cloudcipher.cloudcipher_client_v2.authentication to javafx.fxml;
    exports com.cloudcipher.cloudcipher_client_v2.authentication;

    opens com.cloudcipher.cloudcipher_client_v2.authentication.model to javafx.fxml;
    exports com.cloudcipher.cloudcipher_client_v2.authentication.model;

    opens com.cloudcipher.cloudcipher_client_v2.file to javafx.fxml;
    exports com.cloudcipher.cloudcipher_client_v2.file;

    opens com.cloudcipher.cloudcipher_client_v2.tool to javafx.fxml;
    exports com.cloudcipher.cloudcipher_client_v2.tool;
}