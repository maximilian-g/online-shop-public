package com.online.shop.service.util;

import org.springframework.core.io.InputStreamResource;

public class InputResource {

    private final InputStreamResource inputStreamResource;
    private final long contentLength;
    private final String resourceName;

    public InputResource(InputStreamResource inputStreamResource, long contentLength, String resourceName) {
        this.inputStreamResource = inputStreamResource;
        this.contentLength = contentLength;
        this.resourceName = resourceName;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getResourceName() {
        return resourceName;
    }

    public InputStreamResource getInputStreamResource() {
        return inputStreamResource;
    }

}
