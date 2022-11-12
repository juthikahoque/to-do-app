module frontend {
    exports frontend;
    exports frontend.services;
    opens frontend.controllers;

    // resources
    opens icons.google_signin_buttons;
    opens icons.sort_icons;

    requires shared;
    requires kotlinx.coroutines.core.jvm;
    requires kotlin.stdlib;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires io.ktor.client.core;
    requires io.ktor.client.content.negotiation;
    requires io.ktor.serialization.kotlinx.json;
    requires kotlinx.serialization.json;
    requires io.ktor.http;
    requires java.desktop;
    requires io.ktor.server.netty;
    requires io.ktor.server.host.common;
    requires io.ktor.server.core;
    requires io.ktor.client.auth;
    requires java.prefs;
}