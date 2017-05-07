package com.moesif.servlet.utils;

import org.apache.commons.lang3.StringUtils;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by derric on 4/10/17.
 */
public class IpAddress {

    public static String getClientIp(HttpServletRequest request) {
        // Standard headers used by Amazon EC2, Heroku, and others.
        String header = getClientIpFromHeader(request.getHeader("x-client-ip"));
        if (StringUtils.isNotEmpty(header)) {
            return header;
        }

        header = getClientIpFromHeader(request.getHeader("client-ip"));
        if (StringUtils.isNotEmpty(header)) {
            return header;
        }

        header = getClientIpFromHeader(request.getHeader("x-forwarded-for"));
        if (StringUtils.isNotEmpty(header)) {
            return header;
        }

        // Cloudflare.
        // @see https://support.cloudflare.com/hc/en-us/articles/200170986-How-does-Cloudflare-handle-HTTP-Request-headers-
        // CF-Connecting-IP - applied to every request to the origin.
        header = getClientIpFromHeader(request.getHeader("cf-connecting-ip"));
        if (StringUtils.isNotEmpty(header)) {
            return header;
        }

        // Akamai and Cloudflare: True-Client-IP.
        header = getClientIpFromHeader(request.getHeader("true-client-ip"));
        if (StringUtils.isNotEmpty(header)) {
            return header;
        }

        // Default nginx proxy/fcgi; alternative to x-forwarded-for, used by some proxies.
        header = getClientIpFromHeader(request.getHeader("x-real-ip"));
        if (StringUtils.isNotEmpty(header)) {
            return header;
        }

        // (Rackspace LB and Riverbed's Stingray)
        // http://www.rackspace.com/knowledge_center/article/controlling-access-to-linux-cloud-sites-based-on-the-client-ip-address
        // https://splash.riverbed.com/docs/DOC-1926
        header = getClientIpFromHeader(request.getHeader("x-cluster-client-ip"));
        if (StringUtils.isNotEmpty(header)) {
            return header;
        }

        header = getClientIpFromHeader(request.getHeader("x-forwarded"));
        if (StringUtils.isNotEmpty(header)) {
            return header;
        }

        header = getClientIpFromHeader(request.getHeader("forwarded-for"));
        if (StringUtils.isNotEmpty(header)) {
            return header;
        }

        header = getClientIpFromHeader(request.getHeader("forwarded"));
        if (StringUtils.isNotEmpty(header)) {
            return header;
        }

        return request.getRemoteAddr();
    }

    private static Pattern PRIVATE_ADDRESS_PATTERN = Pattern.compile(
            "(^127\\.)|(^192\\.168\\.)|(^10\\.)|(^172\\.1[6-9]\\.)|(^172\\.2[0-9]\\.)|(^172\\.3[0-1]\\.)|(^::1$)|(^[fF][cCdD])",
            Pattern.CANON_EQ);

    private static String getClientIpFromHeader(String header) {

        if (StringUtils.isEmpty(header)) {
            return null;
        }

        String firstValue = header.split(",")[0];

        if (StringUtils.isEmpty(firstValue) || isPrivateOrLocalAddress(firstValue)) {
            return null;
        } else {
            return firstValue;
        }
    }

    private static boolean isPrivateOrLocalAddress(String address) {
        Matcher regexMatcher = PRIVATE_ADDRESS_PATTERN.matcher(address);
        return regexMatcher.matches();
    }
}
