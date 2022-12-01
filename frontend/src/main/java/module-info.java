module frontend {
    exports frontend;
    exports frontend.services;
    opens frontend.controllers;

    // resources
    opens icons.google_signin_buttons;
    opens icons.sort_icons;
    opens icons.status;
    opens icons.ui_icons;

    requires shared;
    requires kotlinx.coroutines.core.jvm;
    requires kotlin.stdlib;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires io.ktor.client.core;
    requires io.ktor.client.auth;
    requires io.ktor.client.content.negotiation;
    requires io.ktor.serialization.kotlinx.json;
    requires io.ktor.server.netty;
    requires io.ktor.server.host.common;
    requires io.ktor.server.core;
    requires kotlinx.serialization.json;
    requires io.ktor.http;
    requires java.desktop;
    requires java.prefs;
    requires kotlinx.coroutines.javafx;
    requires org.jfxtras.styles.jmetro;
}
