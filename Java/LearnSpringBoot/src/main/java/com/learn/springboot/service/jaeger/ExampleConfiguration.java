package com.learn.springboot.service.jaeger;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;
import java.util.concurrent.TimeUnit;

/**
 * All SDK management takes place here, away from the instrumentation code, which should only access
 * the OpenTelemetry APIs.
 */
class ExampleConfiguration {

    private static final String jaegerEndpoint = "http://localhost:4317";

    /**
     * Initialize an OpenTelemetry SDK with a {@link OtlpGrpcSpanExporter} and a {@link
     * BatchSpanProcessor}.
     *
     * @return A ready-to-use {@link OpenTelemetry} instance.
     */
    static OpenTelemetry initOpenTelemetry() {
        // Export traces to Jaeger over OTLP
        OtlpGrpcSpanExporter jaegerOtlpExporter =
                OtlpGrpcSpanExporter.builder()
                        .setEndpoint(jaegerEndpoint)
                        .setTimeout(30, TimeUnit.SECONDS)
                        .build();

        Resource serviceNameResource =
                Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "otel-jaeger-example"));

        // Set to process the spans by the Jaeger Exporter
        SdkTracerProvider tracerProvider =
                SdkTracerProvider.builder()
                        .addSpanProcessor(BatchSpanProcessor.builder(jaegerOtlpExporter).build())
                        .setResource(Resource.getDefault().merge(serviceNameResource))
                        .build();
        OpenTelemetrySdk openTelemetry =
                OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();

        // it's always a good idea to shut down the SDK cleanly at JVM exit.
        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

        return openTelemetry;
    }
}
