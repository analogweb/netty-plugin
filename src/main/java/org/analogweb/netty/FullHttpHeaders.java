package org.analogweb.netty;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.*;

import org.analogweb.Headers;

/**
 * @author y2k2mt
 */
public class FullHttpHeaders implements Headers {

    private final HttpHeaders source;
    private Map<String, List<String>> mapEntry;

    public FullHttpHeaders(HttpHeaders headers) {
        this.source = headers;
    }

    @Override
    public boolean contains(String name) {
        return this.source.contains(name);
    }

    @Override
    public Map<String, List<String>> toMap() {
        if (this.mapEntry == null) {
            Set<String> names = this.source.names();
            HashMap<String, List<String>> entries = new HashMap<String, List<String>>();
            for (String name : this.source.names()) {
                entries.put(name, this.source.getAll(name));
            }
            this.mapEntry = Collections.unmodifiableMap(entries);
        }
        return this.mapEntry;
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