module backend {
    requires kotlin.stdlib;
    requires io.ktor.server.netty;
    requires io.ktor.server.core;
    requires io.ktor.server.content.negotiation;
    requires io.ktor.serialization.kotlinx.json;
    requires io.ktor.server.status.pages;
    requires io.ktor.http;
    requires java.sql;

    requires shared;
    requires io.ktor.server.auth;
    requires com.google.auth.oauth2;
    requires firebase.admin;
}