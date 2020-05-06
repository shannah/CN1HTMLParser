/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.html;

import com.codename1.ui.BrowserComponent;
import com.codename1.ui.CN;
import com.codename1.util.AsyncResource;
import com.codename1.xml.Element;
import com.codename1.xml.XMLParser;
import java.io.StringReader;

/**
 * A class for parsing HTML.  This uses the platform native webview to parse the
 * HTML, and then serialize it to XML.  It then uses the {@link XMLParser} class to 
 * parse that well-formed XML into an XML document.
 * 
 * @author shannah
 */
public class HTMLParser {
    private BrowserComponent browserCmp;
    private XMLParser parser = new XMLParser();
    
    /**
     * A promise class for the result of parsing HTML.
     */
    public static class Result extends AsyncResource<Element> {
        
    }
    
    /**
     * Parses string containing HTML.  This is an asynchronous call.  Does not block.
     * @param html The HTML to be parsed
     * @return Result object which can be used to obtain the result {@link Element}.
     */
    public Result parse(String html) {
        return parse(new Result(), html);
        
    }
    
    private Result parse(Result out, String html) {
        if (!CN.isEdt()) {
            CN.callSerially(()->parse(out, html));
            return out;
        }
        
        if (browserCmp == null) {
            browserCmp = new BrowserComponent();
            
            
        }
        browserCmp.setPage(html, "/");
        browserCmp.addWebEventListener(BrowserComponent.onLoad, cmp->{
            browserCmp.execute("try{callback.onSuccess(new XMLSerializer().serializeToString(document));}catch (e){callback.onError(e.message,0);}", (ref)->{
                String result = ref.toString();
                try {
                    out.complete(parser.parse(new StringReader(result)));
                } catch (Throwable t) {
                    out.error(new AsyncResource.AsyncExecutionException(t));
                }

            });
        });
        return out;
        
    }
    
    
}
