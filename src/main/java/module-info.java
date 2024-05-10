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

    opens com.cloudcipher.cloudcipher_client_v2 to javafx.fxml;
    exports com.cloudcipher.cloudcipher_client_v2;
}