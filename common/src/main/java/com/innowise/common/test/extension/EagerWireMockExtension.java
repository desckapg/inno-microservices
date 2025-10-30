package com.innowise.common.test.extension;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import com.github.tomakehurst.wiremock.junit.DslWrapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.Optional;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

public class EagerWireMockExtension extends DslWrapper
    implements ParameterResolver,
    BeforeEachCallback,
    BeforeAllCallback,
    AfterEachCallback,
    AfterAllCallback {

  private final boolean configureStaticDsl;
  private final boolean failOnUnmatchedRequests;
  private final boolean resetOnEachTest;

  private final boolean isDeclarative;

  private Options options;
  private WireMockServer wireMockServer;
  private WireMockRuntimeInfo runtimeInfo;
  private boolean isNonStatic = false;
  private Boolean proxyMode;

  EagerWireMockExtension() {
    configureStaticDsl = true;
    failOnUnmatchedRequests = false;
    isDeclarative = true;
    resetOnEachTest = true;
  }


  protected EagerWireMockExtension(EagerWireMockExtension.Builder builder) {
    this.options = builder.options;
    this.configureStaticDsl = builder.configureStaticDsl;
    this.failOnUnmatchedRequests = builder.failOnUnmatchedRequests;
    this.proxyMode = builder.proxyMode;
    this.isDeclarative = false;
    this.resetOnEachTest = builder.resetOnEachTest;
  }

  private EagerWireMockExtension(
      Options options,
      boolean configureStaticDsl,
      boolean failOnUnmatchedRequests,
      boolean proxyMode) {
    this.options = options;
    this.configureStaticDsl = configureStaticDsl;
    this.failOnUnmatchedRequests = failOnUnmatchedRequests;
    this.proxyMode = proxyMode;
    this.isDeclarative = false;
    this.resetOnEachTest = true;
  }

  private EagerWireMockExtension(
      Options options,
      boolean configureStaticDsl,
      boolean failOnUnmatchedRequests,
      boolean proxyMode,
      boolean resetOnEachTest) {
    this.options = options;
    this.configureStaticDsl = configureStaticDsl;
    this.failOnUnmatchedRequests = failOnUnmatchedRequests;
    this.proxyMode = proxyMode;
    this.isDeclarative = false;
    this.resetOnEachTest = resetOnEachTest;
  }

  public static EagerWireMockExtension.Builder extensionOptions() {
    return newInstance();
  }

  public static EagerWireMockExtension.Builder newInstance() {
    return new EagerWireMockExtension.Builder();
  }

  protected void onBeforeAll(WireMockRuntimeInfo wireMockRuntimeInfo) {}

  protected void onBeforeAll(
      ExtensionContext extensionContext, WireMockRuntimeInfo wireMockRuntimeInfo) {
    this.onBeforeAll(wireMockRuntimeInfo);
  }


  protected void onBeforeEach(WireMockRuntimeInfo wireMockRuntimeInfo) {}


  protected void onBeforeEach(
      ExtensionContext extensionContext, WireMockRuntimeInfo wireMockRuntimeInfo) {
    this.onBeforeEach(wireMockRuntimeInfo);
  }


  protected void onAfterEach(WireMockRuntimeInfo wireMockRuntimeInfo) {}


  protected void onAfterEach(
      ExtensionContext extensionContext, WireMockRuntimeInfo wireMockRuntimeInfo) {
    this.onAfterEach(wireMockRuntimeInfo);
  }

  protected void onAfterAll(WireMockRuntimeInfo wireMockRuntimeInfo) {}

  protected void onAfterAll(
      ExtensionContext extensionContext, WireMockRuntimeInfo wireMockRuntimeInfo) {
    this.onAfterAll(wireMockRuntimeInfo);
  }

  @Override
  public boolean supportsParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterIsWireMockRuntimeInfo(parameterContext);
  }

  @Override
  public Object resolveParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext)
      throws ParameterResolutionException {

    if (parameterIsWireMockRuntimeInfo(parameterContext)) {
      return runtimeInfo;
    }

    return null;
  }

  public void startServerIfRequired() {
    if (wireMockServer == null || !wireMockServer.isRunning()) {
      wireMockServer = new WireMockServer();
      wireMockServer.start();

      runtimeInfo = new WireMockRuntimeInfo(wireMockServer);

      this.admin = wireMockServer;
      this.stubbing = wireMockServer;

      if (configureStaticDsl) {
        WireMock.configureFor(new WireMock(this));
      }
    }
  }


  private void setAdditionalOptions(ExtensionContext extensionContext) {
    if (proxyMode == null) {
      proxyMode =
          extensionContext
              .getElement()
              .flatMap(
                  annotatedElement ->
                      AnnotationSupport.findAnnotation(annotatedElement, WireMockTest.class))
              .map(WireMockTest::proxyMode)
              .orElse(false);
    }
  }

  private Options resolveOptions(ExtensionContext extensionContext) {
    final Options defaultOptions = WireMockConfiguration.options().dynamicPort();
    return extensionContext
        .getElement()
        .flatMap(
            annotatedElement ->
                this.isDeclarative
                    ? AnnotationSupport.findAnnotation(annotatedElement, WireMockTest.class)
                    : Optional.empty())
        .map(this::buildOptionsFromWireMockTestAnnotation)
        .orElseGet(() -> Optional.ofNullable(this.options).orElse(defaultOptions));
  }

  private Options buildOptionsFromWireMockTestAnnotation(WireMockTest annotation) {
    WireMockConfiguration options =
        WireMockConfiguration.options()
            .port(annotation.httpPort())
            .extensionScanningEnabled(annotation.extensionScanningEnabled())
            .enableBrowserProxying(annotation.proxyMode());

    if (annotation.httpsEnabled()) {
      options.httpsPort(annotation.httpsPort());
    }

    return options;
  }

  private void stopServerIfRunning() {
    if (wireMockServer != null && wireMockServer.isRunning()) {
      wireMockServer.stop();
    }
  }

  private boolean parameterIsWireMockRuntimeInfo(ParameterContext parameterContext) {
    return parameterContext.getParameter().getType().equals(WireMockRuntimeInfo.class)
        && this.isDeclarative;
  }

  @Override
  public final void beforeAll(ExtensionContext context) throws Exception {
    startServerIfRequired();
    setAdditionalOptions(context);

    onBeforeAll(context, runtimeInfo);
  }

  @Override
  public final void beforeEach(ExtensionContext context) throws Exception {
    if (wireMockServer == null) {
      isNonStatic = true;
      startServerIfRequired();
    } else {
      if (resetOnEachTest) {
        resetToDefaultMappings();
      }
    }

    setAdditionalOptions(context);

    if (proxyMode) {
      JvmProxyConfigurer.configureFor(wireMockServer);
    }

    onBeforeEach(context, runtimeInfo);
  }

  @Override
  public final void afterAll(ExtensionContext context) throws Exception {
    //stopServerIfRunning();

    onAfterAll(context, runtimeInfo);
  }

  @Override
  public final void afterEach(ExtensionContext context) throws Exception {
    if (failOnUnmatchedRequests) {
      wireMockServer.checkForUnmatchedRequests();
    }

//    if (isNonStatic) {
//      stopServerIfRunning();
//    }

    if (proxyMode) {
      JvmProxyConfigurer.restorePrevious();
    }

    onAfterEach(context, runtimeInfo);
  }

  public WireMockRuntimeInfo getRuntimeInfo() {
    return new WireMockRuntimeInfo(wireMockServer);
  }

  public String baseUrl() {
    return wireMockServer.baseUrl();
  }

  public String url(String path) {
    return wireMockServer.url(path);
  }

  public int getHttpsPort() {
    return wireMockServer.httpsPort();
  }

  public int getPort() {
    return wireMockServer.port();
  }

  public static class Builder {

    private Options options = WireMockConfiguration.wireMockConfig().dynamicPort();
    private boolean configureStaticDsl = false;
    private boolean failOnUnmatchedRequests = false;
    private boolean resetOnEachTest = true;
    private boolean proxyMode = false;

    public EagerWireMockExtension.Builder options(Options options) {
      this.options = options;
      return this;
    }

    public EagerWireMockExtension.Builder configureStaticDsl(boolean configureStaticDsl) {
      this.configureStaticDsl = configureStaticDsl;
      return this;
    }

    public EagerWireMockExtension.Builder failOnUnmatchedRequests(boolean failOnUnmatched) {
      this.failOnUnmatchedRequests = failOnUnmatched;
      return this;
    }

    public EagerWireMockExtension.Builder proxyMode(boolean proxyMode) {
      this.proxyMode = proxyMode;
      return this;
    }

    public EagerWireMockExtension.Builder resetOnEachTest(boolean resetOnEachTest) {
      this.resetOnEachTest = resetOnEachTest;
      return this;
    }

    public EagerWireMockExtension build() {
      if (proxyMode
          && !options.browserProxySettings().enabled()
          && (options instanceof WireMockConfiguration)) {
        ((WireMockConfiguration) options).enableBrowserProxying(true);
      }

      return new EagerWireMockExtension(
          options, configureStaticDsl, failOnUnmatchedRequests, proxyMode, resetOnEachTest);
    }
  }
}