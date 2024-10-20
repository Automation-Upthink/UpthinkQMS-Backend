package com.upthink.qms.service;

import gson.GsonDTO;

import java.util.List;

public class JsonWebTokenResponse extends GsonDTO {

    private final List<JsonWebKey> keys;

    public JsonWebTokenResponse(List<JsonWebKey> keys) {
        this.keys = keys;
    }

    public static class JsonWebKey extends GsonDTO {
        private final String kty, alg, kid, e, n, use;

        public JsonWebKey(String kty, String alg, String kid, String e, String n, String use) {
            this.kty = kty;
            this.alg = alg;
            this.kid = kid;
            this.e = e;
            this.n = n;
            this.use = use;
        }
    }
}
