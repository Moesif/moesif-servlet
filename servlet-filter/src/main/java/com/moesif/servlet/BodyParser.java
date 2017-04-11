package com.moesif.servlet;

import com.moesif.api.APIHelper;
import com.moesif.servlet.utils.Base64;
import java.util.Map;

/**
 * Created by derric on 4/10/17.
 */
public class BodyParser {

    public static BodyWrapper parseBody(Map<String, String> headers, String content) {
        if(content.equals("[\"[UNSUPPORTED ENCODING]\"]")) {

            String errorMsgJson = "{"
                    + "    \"moesif_error\": {"
                    + "       \"code\": \"servlet_content_type_error\","
                    + "       \"msg\": \"The content type of the body is not supported.\","
                    + "       \"src\": \"moesif-servlet\","
                    + "       \"args\": \"\""
                    + "    }";

            try {
                return new BodyWrapper(APIHelper.deserialize(errorMsgJson, Object.class), null);
            } catch(Exception E) {
                return new BodyWrapper(null, null);
            }
        } else {
            if (isJsonHeader(headers) || startWithJson(content)) {
                try {
                    return new BodyWrapper(APIHelper.deserialize(content, Object.class), null);
                } catch (Exception e) {
                    return new BodyWrapper(getBase64String(content), "base64");
                }
            } else {
                return new BodyWrapper(getBase64String(content), "base64");
            }
        }
    }

    private static boolean isJsonHeader(Map<String, String> headers) {
        String val = headers.get("Content-Type");
        if (val != null) {
            if (val.contains("json")) {
                return true;
            }
        }
        String val2 = headers.get("content-type");
        if (val2 != null) {
            if (val2.contains("json")) {
                return true;
            }
        }
        String val3 = headers.get("CONTENT-TYPE");
        if (val3 != null) {
            if (val3.contains("json")) {
                return true;
            }
        }
        return false;
    }

    private static boolean startWithJson(String str) {
        return str.trim().startsWith("[") || str.trim().startsWith("{");
    }

    private static String getBase64String(String str) {
        byte[] encodedBytes = Base64.encode(str.getBytes(), Base64.DEFAULT);
        return new String(encodedBytes);
    }

    public static class BodyWrapper {

        public BodyWrapper(Object body, String transferEncoding) {
            this.body = body;
            this.transferEncoding = transferEncoding;
        }

        public Object body;

        public String transferEncoding;
    }
}
