package org.jenkinsci.plugin.gitea.client.http;

import java.net.HttpURLConnection;

/**
 * @see <a href="https://tools.ietf.org/html/rfc8288">RFC8288</a>
 */
public class PageLinkHeader {

    public static final String HEADER = "Link";
    private static final String PARAMETER_SEPARATOR = ",";
    private static final String ATTRIBUTE_SEPARATOR = ";";

    private String first;
    private String last;
    private String next;
    private String prev;

    private PageLinkHeader(HttpURLConnection connection) {
        String headerField = connection.getHeaderField(HEADER);
        if (headerField != null) {
            for (String rawLink : headerField.split(PARAMETER_SEPARATOR)) {
                String[] attributes = rawLink.split(ATTRIBUTE_SEPARATOR);
                if (attributes.length < 2) {
                    continue;
                }

                String link = attributes[0].trim();
                if (!link.startsWith("<") || !link.endsWith(">")) {
                    continue;
                }

                String parsedLink = link.substring(1, link.length() - 1);
                for (int i = 1; i < attributes.length; i++) {
                    String[] parameterAttributes = attributes[i].split("=");
                    if (parameterAttributes.length < 2 || !parameterAttributes[0].trim().equals("rel")) {
                        continue;
                    }

                    String parameterAttributeValue = parameterAttributes[1].replace("\"", "").toLowerCase();
                    if ("first".equals(parameterAttributeValue)) {
                        first = parsedLink;
                    } else if ("last".equals(parameterAttributeValue)) {
                        last = parsedLink;
                    } else if ("next".equals(parameterAttributeValue)) {
                        next = parsedLink;
                    } else if ("prev".equals(parameterAttributeValue)) {
                        prev = parsedLink;
                    }
                }
            }
        }
    }

    public static PageLinkHeader from(HttpURLConnection connection) {
        return new PageLinkHeader(connection);
    }

    public String getFirst() {
        return first;
    }

    public String getLast() {
        return last;
    }

    public String getNext() {
        return next;
    }

    public String getPrev() {
        return prev;
    }
}
