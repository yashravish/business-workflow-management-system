// Translated from: backend/app/core/config.py (Settings)
package com.example.bwms.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private String jwtSecretKey = "change-me-in-production-use-min-32-chars";
    private String jwtAlgorithm = "HS256";
    private int jwtExpireMinutes = 480;
    private List<String> corsOrigins = List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://127.0.0.1:5173"
    );

    public String getJwtSecretKey()        { return jwtSecretKey; }
    public String getJwtAlgorithm()        { return jwtAlgorithm; }
    public int getJwtExpireMinutes()       { return jwtExpireMinutes; }
    public List<String> getCorsOrigins()   { return corsOrigins; }

    public void setJwtSecretKey(String k)        { this.jwtSecretKey = k; }
    public void setJwtAlgorithm(String a)        { this.jwtAlgorithm = a; }
    public void setJwtExpireMinutes(int m)       { this.jwtExpireMinutes = m; }
    public void setCorsOrigins(List<String> o)   { this.corsOrigins = o; }
}
