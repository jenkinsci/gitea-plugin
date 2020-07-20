package org.jenkinsci.plugin.gitea.client.http;

import java.net.HttpURLConnection;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class PageLinkHeaderTest {

    @Test
    public void given__pageable_connection__when__fetch__then__parse() {
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("<http://try.gitea.io/api/v1/orgs/test_org/repos?page=3>; rel=\"next\",");
        headerBuilder.append("<http://try.gitea.io/api/v1/orgs/test_org/repos?page=3>; rel=\"last\",");
        headerBuilder.append("<http://try.gitea.io/api/v1/orgs/test_org/repos?page=1>; rel=\"first\",");
        headerBuilder.append("<http://try.gitea.io/api/v1/orgs/test_org/repos?page=1>; rel=\"prev\"");

        HttpURLConnection connect = Mockito.mock(HttpURLConnection.class);
        Mockito.when(connect.getHeaderField(PageLinkHeader.HEADER)).thenReturn(headerBuilder.toString());


        PageLinkHeader linkHeader = PageLinkHeader.from(connect);

        Assert.assertNotNull(linkHeader.getFirst());
        Assert.assertEquals("http://try.gitea.io/api/v1/orgs/test_org/repos?page=1", linkHeader.getFirst());

        Assert.assertNotNull(linkHeader.getLast());
        Assert.assertEquals("http://try.gitea.io/api/v1/orgs/test_org/repos?page=3", linkHeader.getLast());

        Assert.assertNotNull(linkHeader.getPrev());
        Assert.assertEquals("http://try.gitea.io/api/v1/orgs/test_org/repos?page=1", linkHeader.getPrev());

        Assert.assertNotNull(linkHeader.getNext());
        Assert.assertEquals("http://try.gitea.io/api/v1/orgs/test_org/repos?page=3", linkHeader.getNext());
    }

    @Test
    public void given__pageable_rel_not_first__when__fetch__then__parse() {
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("<http://try.gitea.io/api/v1/orgs/test_org/repos?page=2>; attr=value; rel=\"next\",");
        headerBuilder.append("<http://try.gitea.io/api/v1/orgs/test_org/repos?page=2>; rel=\"last\",");

        HttpURLConnection connect = Mockito.mock(HttpURLConnection.class);
        Mockito.when(connect.getHeaderField(PageLinkHeader.HEADER)).thenReturn(headerBuilder.toString());


        PageLinkHeader linkHeader = PageLinkHeader.from(connect);

        Assert.assertNotNull(linkHeader.getNext());
        Assert.assertEquals("http://try.gitea.io/api/v1/orgs/test_org/repos?page=2", linkHeader.getNext());

        Assert.assertNotNull(linkHeader.getLast());
        Assert.assertEquals("http://try.gitea.io/api/v1/orgs/test_org/repos?page=2", linkHeader.getLast());
    }

}
