package com.unloadbrain.assignement.takeaway.scraper.util;

import com.unloadbrain.assignement.takeaway.common.exception.IORuntimeException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;

/**
 * Provides JSoup related utils.
 */
public class JsoupUtil {

    public Document fetch(String url, int timeout) {
        try {
            return Jsoup.parse(new URL(url), timeout);
        } catch (IOException e) {
            throw new IORuntimeException("Could not download document.", e);
        }
    }
}
