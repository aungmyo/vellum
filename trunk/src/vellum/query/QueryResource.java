/*
 * Apache Software License 2.0, (c) Copyright 2012 Evan Summers, 2010 iPay (Pty) Ltd
 * Apache Software License 2.0
 * Supported by iPay (Pty) Ltd, BizSwitch.net
 */

package vellum.query;

import vellum.query.QueryInfo;
import vellum.query.QueryInfoMap;
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