module com.teagang.projecttea {
    requires javafx.controls;
    requires javafx.fxml;

    requires atlantafx.base;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.material2;

    requires commons.math3;

    opens com.teagang.projecttea to javafx.fxml;
    exports com.teagang.projecttea;
}