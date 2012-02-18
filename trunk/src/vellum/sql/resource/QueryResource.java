/*
 * (c) Copyright 2011, iPay (Pty) Ltd, Evan Summers
 * Apache Software License 2.0
 * Supported by BizSwitch.net
 */

package vellum.sql.resource;

import vellum.sql.query.QueryInfo;
import vellum.sql.query.QueryInfoMap;
import java.io.InputStream;

/**
 *
 * @author evanx
 */
public enum QueryResource {
    common,
    terminal,
    month,
    incoming;

    QueryInfoMap map;

    public InputStream getStream() {
        return getClass().getResourceAsStream(name() + ".sql");
    }

    public QueryInfoMap getMap() {
        if (map == null) {
            map = new QueryInfoMap(this);
        }
        return map;
    }

    public QueryInfo get(String queryName) {
        return getMap().get(queryName);
    }

}
