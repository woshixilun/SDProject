package app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by yubzhu on 19-7-11
 */

public class LogService {

    private Logger logger;

    public LogService(Class clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    private static String getRequestURL(HttpServletRequest httpServletRequest) throws UnsupportedEncodingException {
        if (httpServletRequest.getQueryString() == null) {
            return httpServletRequest.getRequestURL().toString();
        } else {
            return httpServletRequest.getRequestURL() + "?" + URLDecoder.decode(httpServletRequest.getQueryString(), "utf-8");
        }
    }

    public void printExecuteOkInfo(HttpServletRequest httpServletRequest) {
        try {
            String url = getRequestURL(httpServletRequest);
            logger.info("At [{}] --- Execute ok.", url);
        } catch (UnsupportedEncodingException e) {
            logger.error("FATAL ERROR --- Exception \"{}\" occurred.", UnsupportedEncodingException.class.getName());
        }
    }

    public void printExceptionOccurredError(HttpServletRequest httpServletRequest, Exception exception) {
        try {
            String url = getRequestURL(httpServletRequest);
            logger.error("At [{}] --- Exception \"{}\" occurred.", url, exception.getClass().getName());
        } catch (UnsupportedEncodingException e) {
            logger.error("FATAL ERROR --- Exception \"{}\" occurred.", UnsupportedEncodingException.class.getName());
        }
    }
}
