package org.analogweb.netty;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;

import org.analogweb.Headers;

/**
 * @author snowgooseyk
 */
public class FullHttpHeaders implements Headers {

    private final HttpHeaders source;

    public FullHttpHeaders(HttpHeaders headers) {
        this.source = headers;
    }

    @Override
    public boolean contains(String name) {
        return this.source.contains(name);
    }

    @Override
    public List<String> getNames() {
        return new ArrayList<String>(this.source.names());
    }

    @Override
    public List<String> getValues(String name) {
        return this.source.getAll(name);
    }

    @Override
    public void putValue(String name, String value) {
        this.source.add(name, value);
    }
}
