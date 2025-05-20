package com.luis.ciberloja.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.SpringComponentProvider;

public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(SpringComponentProvider.class);
        packages("com.luis.ciberloja"); // Scan your resource package
    }
}