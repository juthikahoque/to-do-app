module frontend {
    requires utilities;
    requires kotlinx.coroutines.core.jvm;
    requires kotlin.stdlib;
    requires javafx.graphics;
    requires io.ktor.client.core;
    requires io.ktor.client.content.negotiation;
    requires io.ktor.serialization.kotlinx.json;
    requires kotlinx.serialization.json;
    requires javafx.controls;
    requires io.ktor.http;
}