package org.analogweb.netty;

import io.netty.handler.codec.http2.*;
import org.analogweb.Application;
import org.analogweb.ApplicationProperties;

import static io.netty.handler.logging.LogLevel.INFO;

/**
 * @author y2k2mt
 */
public class AnalogwebHttp2ConnectionHandlerBuilder
        extends AbstractHttp2ConnectionHandlerBuilder<AnalogwebHttp2ConnectionHandler, AnalogwebHttp2ConnectionHandlerBuilder> {

    private final Application app;
    private final ApplicationProperties properties;

    private static final Http2FrameLogger logger = new Http2FrameLogger(INFO, AnalogwebHttp2ConnectionHandlerBuilder.class);

    public AnalogwebHttp2ConnectionHandlerBuilder(Application app,ApplicationProperties props) {
        frameLogger(logger);
        this.app = app;
        this.properties =props;
    }

    @Override
    public AnalogwebHttp2ConnectionHandler build() {
        return super.build();
    }

    @Override
    protected AnalogwebHttp2ConnectionHandler build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                                    Http2Settings initialSettings) {
        AnalogwebHttp2ConnectionHandler handler = new AnalogwebHttp2ConnectionHandler(decoder, encoder, initialSettings,getApplication(),getApplicationProperties());
        frameListener(handler);
        return handler;
    }
    protected Application getApplication() {
        return this.app;
    }

    protected ApplicationProperties getApplicationProperties() {
        return this.properties;
    }
}
