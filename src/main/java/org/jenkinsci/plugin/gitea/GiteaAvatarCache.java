/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugin.gitea;

import com.damnhandy.uri.template.UriTemplate;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.Util;
import hudson.model.RootAction;
import hudson.model.UnprotectedRootAction;
import hudson.util.DaemonThreadFactory;
import hudson.util.HttpResponses;
import hudson.util.NamingThreadFactory;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import static java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;

/**
 * An avatar cache that will serve URLs that have been recently registered through {@link #buildUrl(String, String)}
 */
@Extension
public class GiteaAvatarCache implements UnprotectedRootAction {
    /**
     * Our logger.
     */
    private static final Logger LOGGER = Logger.getLogger(GiteaSCMNavigator.class.getName());
    /**
     * The cache of entries. Unused entries will be removed over time.
     */
    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    /**
     * A background thread pool to refresh images.
     */
    private final ExecutorService service = new ThreadPoolExecutor(0, 4,
            1L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
            new NamingThreadFactory(new DaemonThreadFactory(), getClass().getName())
    );
    /**
     * The lock to ensure we prevent concurrent requests for the same URL.
     */
    private final Object serviceLock = new Object();
    /**
     * The iterator that searches for unused entries. The search is amortized over every access.
     */
    private Iterator<Map.Entry<String, CacheEntry>> iterator = null;

    /**
     * Constructor.
     */
    public GiteaAvatarCache() {
    }

    /**
     * Builds the URL for the cached avatar image of the required size.
     *
     * @param url  the URL of the source avatar image.
     * @param size the size of the image.
     * @return the URL of the cached image.
     */
    public static String buildUrl(String url, String size) {
        Jenkins j = Jenkins.getActiveInstance();
        GiteaAvatarCache instance = j.getExtensionList(RootAction.class).get(GiteaAvatarCache.class);
        if (instance == null) {
            throw new AssertionError();
        }
        String key = Util.getDigestOf(GiteaAvatarCache.class.getName() + url);
        // seed the cache
        instance.getCacheEntry(key, url);
        return UriTemplate.buildFromTemplate(j.getRootUrlFromRequest())
                .literal(instance.getUrlName())
                .path("key")
                .query("size")
                .build()
                .set("key", key)
                .set("size", size)
                .expand();
    }

    private static BufferedImage scaleImage(BufferedImage src, int size) {
        int newWidth;
        int newHeight;
        if (src.getWidth() > src.getHeight()) {
            newWidth = size;
            newHeight = size * src.getHeight() / src.getWidth();
        } else if (src.getHeight() > src.getWidth()) {
            newWidth = size * src.getWidth() / src.getHeight();
            newHeight = size;
        } else {
            newWidth = newHeight = size;
        }
        boolean flushSrc = false;
        if (newWidth <= src.getWidth() * 6 / 7 && newHeight <= src.getWidth() * 6 / 7) {
            // when scaling down, you get better image quality if you scale down in multiple rounds
            // see https://community.oracle.com/docs/DOC-983611
            // we scale each round by 6/7 = ~85% as this gives nicer looking images
            int curWidth = src.getWidth();
            int curHeight = src.getHeight();
            // we want to break the rounds and do the final round and centre when the src image is this size
            final int penultimateSize = size * 7 / 6;
            while (true) {
                curWidth = curWidth - curWidth / 7;
                curHeight = curHeight - curHeight / 7;
                if (curWidth <= penultimateSize && curHeight <= penultimateSize) {
                    // we are within one round of target size let's go
                    break;
                }
                BufferedImage tmp = new BufferedImage(curWidth, curHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = tmp.createGraphics();
                try {
                    // important, if we don't set these two hints then scaling will not work headless
                    g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                    g.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
                    g.scale(((double) curWidth) / src.getWidth(), ((double) curHeight) / src.getHeight());
                    g.drawImage(src, 0, 0, null);
                } finally {
                    g.dispose();
                }
                if (flushSrc) {
                    src.flush();
                }
                src = tmp;
                flushSrc = true;
            }
        }
        BufferedImage tmp = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        try {
            // important, if we don't set these two hints then scaling will not work headless
            g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
            g.scale(((double) newWidth) / src.getWidth(), ((double) newHeight) / src.getHeight());
            g.drawImage(src, (size - newWidth) / 2, (size - newHeight) / 2, null);
        } finally {
            g.dispose();
        }
        if (flushSrc) {
            src.flush();
        }
        src = tmp;
        return src;
    }

    /**
     * Generates a consistent (for any given seed) 5x5 symmetric pixel avatar that should be unique but recognizable.
     *
     * @param seed the seed.
     * @param size the size.
     * @return the image.
     */
    private static BufferedImage generateAvatar(String seed, int size) {
        byte[] bytes;
        try {
            // we want a consistent image across reboots, so just take a hash of the seed
            // if the seed changes we get a new hash and a new image!
            MessageDigest d = MessageDigest.getInstance("MD5");
            bytes = d.digest(seed.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("JLS specification mandates support for MD5 message digest", e);
        }
        BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            // we want the colour in the range 16-245 to prevent pure white and pure black
            // 0xdf == 1101111 so we throw away the 32 place and add in 16 to give 16 on either side
            g.setColor(new Color(bytes[0] & 0xdf + 16, bytes[1] & 0xdf + 16, bytes[2] & 0xdf + 16));
            int pSize = size / 5;
            // likely there will be some remainder from dividing by 5, so half the remainder will be used
            // as an offset to centre the image
            int pOffset = (size - pSize * 5) / 2;
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) {
                    byte bit = (byte) (1 << Math.min(x, 4 - x));
                    if ((bytes[3 + y] & bit) != 0) {
                        g.fillRect(pOffset + x * pSize, pOffset + y * pSize, pSize, pSize);
                    }
                }
            }
        } finally {
            g.dispose();
        }
        return canvas;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconFileName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrlName() {
        return "gitea-avatar";
    }

    /**
     * Serves the cached image.
     *
     * @param req  the request.
     * @param size the requested size (defaults to {@code 48x48} if unspecified).
     * @return the response.
     */
    public HttpResponse doDynamic(StaplerRequest req, @QueryParameter String size) {
        if (StringUtils.isBlank(req.getRestOfPath())) {
            return HttpResponses.notFound();
        }
        String key = req.getRestOfPath().substring(1);
        size = StringUtils.defaultIfBlank(size, "48x48");
        int targetSize = 48;
        int index = size.toLowerCase(Locale.ENGLISH).indexOf('x');
        // we will only resize images in the 16x16 - 128x128 range
        if (index < 2) {
            try {
                targetSize = Math.min(128, Math.max(16, Integer.parseInt(StringUtils.trim(size))));
            } catch (NumberFormatException e) {
                // ignore
            }
        } else {
            try {
                targetSize = Math.min(128, Math.max(16, Integer.parseInt(StringUtils.trim(size.substring(0, index)))));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        final CacheEntry avatar = getCacheEntry(key, null);
        if (avatar == null || !(avatar.url.startsWith("http://") || avatar.url.startsWith("https://"))) {
            // we will generate avatars if the URL is not HTTP based
            // since the url string will not magically turn itself into a HTTP url this avatar is immutable
            return new ImageResponse(
                    generateAvatar(avatar == null ? "" : avatar.url, targetSize),
                    true,
                    System.currentTimeMillis(),
                    "max-age=365000000, immutable, public"
            );
        }
        if (avatar.pending() && avatar.image == null) {
            // serve a temporary avatar until we get the remote one, no caching as we could have the real deal
            // real soon now
            return new ImageResponse(
                    generateAvatar(avatar.url, targetSize),
                    true,
                    -1L,
                    "no-cache, public"
            );
        }
        long since = req.getDateHeader("If-Modified-Since");
        if (avatar.lastModified <= since) {
            return new HttpResponse() {
                @Override
                public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node)
                        throws IOException, ServletException {
                    rsp.addDateHeader("Last-Modified", avatar.lastModified);
                    rsp.addHeader("Cache-control", "max-age=3600, public");
                    rsp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                }
            };
        }
        if (avatar.image == null) {
            // we can retry in an hour
            return new ImageResponse(
                    generateAvatar(avatar.url, targetSize),
                    true,
                    -1L,
                    "max-age=3600, public"
            );
        }

        BufferedImage image = avatar.image;
        boolean flushImage = false;
        if (image.getWidth() != targetSize || image.getHeight() != targetSize) {
            image = scaleImage(image, targetSize);
            flushImage = true;
        }
        return new ImageResponse(image, flushImage, avatar.lastModified, "max-age=3600, public");
    }

    /**
     * Retrieves the entry from the cache.
     *
     * @param key the cache key.
     * @param url the URL to fetch if the entry is missing or {@code null} to perform a read-only check.
     * @return the entry or {@code null} if a read-only check found no matching entry.
     */
    @Nullable
    private CacheEntry getCacheEntry(@NonNull final String key, @Nullable final String url) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            synchronized (serviceLock) {
                entry = cache.get(key);
                if (entry == null) {
                    if (url == null) {
                        return null;
                    }
                    entry = new CacheEntry(url, service.submit(new FetchImage(url)));
                    cache.put(key, entry);
                }
            }
        } else {
            if (entry.isStale()) {
                synchronized (serviceLock) {
                    if (!entry.pending()) {
                        entry.setFuture(service.submit(new FetchImage(entry.url)));
                    }
                }
            }
        }
        entry.touch();
        if (iterator == null || !iterator.hasNext()) {
            synchronized (serviceLock) {
                if (iterator == null || !iterator.hasNext()) {
                    iterator = cache.entrySet().iterator();
                }
            }
        } else {
            synchronized (iterator) {
                // process one entry in the cache each access
                if (iterator.hasNext()) {
                    Map.Entry<String, CacheEntry> next = iterator.next();
                    if (next.getValue().isUnused()) {
                        iterator.remove();
                    }
                } else {
                    iterator = null;
                }
            }
        }
        return entry;
    }

    private static class CacheEntry {
        private final String url;
        private BufferedImage image;
        private long lastModified;
        private long lastAccessed = -1L;

        private Future<CacheEntry> future;

        public CacheEntry(String url, BufferedImage image, long lastModified) {
            this.url = url;
            if (image.getHeight() > 128 || image.getWidth() > 128) {
                // limit the amount of storage
                this.image = scaleImage(image, 128);
                image.flush();
            } else {
                this.image = image;
            }
            this.lastModified = lastModified < 0 ? System.currentTimeMillis() : lastModified;
        }

        public CacheEntry(String url, Future<CacheEntry> future) {
            this.url = url;
            this.image = null;
            this.lastModified = System.currentTimeMillis();
            this.future = future;
        }

        public CacheEntry(String url) {
            this.url = url;
            this.lastModified = System.currentTimeMillis();
        }

        public synchronized boolean pending() {
            if (future == null) {
                return false;
            }
            if (future.isDone()) {
                try {
                    CacheEntry pending = future.get();
                    if (pending.image != null && image != null) {
                        image.flush();
                    }
                    if (pending.image != null) {
                        image = pending.image;
                    }
                    lastModified = pending.lastModified;
                    future = null;
                    return false;
                } catch (InterruptedException | ExecutionException e) {
                    // ignore
                }

            }
            return true;
        }

        public synchronized void setFuture(Future<CacheEntry> future) {
            this.future = future;
        }

        public synchronized boolean isStale() {
            return System.currentTimeMillis() - lastModified > TimeUnit.HOURS.toMillis(1);
        }

        public void touch() {
            lastAccessed = System.currentTimeMillis();
        }

        public boolean isUnused() {
            return lastAccessed > 0L && System.currentTimeMillis() - lastAccessed > TimeUnit.HOURS.toMillis(2);
        }

    }

    private static class ImageResponse implements HttpResponse {
        private final BufferedImage image;
        private final boolean flushImage;
        private final String cacheControl;

        private final long lastModified;

        public ImageResponse(BufferedImage image, boolean flushImage, long lastModified, String cacheControl) {
            this.cacheControl = cacheControl;
            this.image = image;
            this.flushImage = flushImage;
            this.lastModified = lastModified;
        }

        @Override
        public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node)
                throws IOException, ServletException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", bos);
            } finally {
                if (flushImage) {
                    image.flush();
                }
            }
            final byte[] bytes = bos.toByteArray();
            if (lastModified > 0) {
                rsp.addDateHeader("Last-Modified", lastModified);
            }
            rsp.addHeader("Cache-control", cacheControl);
            rsp.setContentType("image/png");
            rsp.setContentLength(bytes.length);
            rsp.getOutputStream().write(bytes);
        }

    }

    private static class FetchImage implements Callable<CacheEntry> {
        private final String url;

        public FetchImage(String url) {
            this.url = url;
        }

        @Override
        public CacheEntry call() throws Exception {
            LOGGER.log(Level.FINE, "Attempting to fetch remote avatar: {0}", url);
            long start = System.nanoTime();
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                try {
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(30000);
                    if (!connection.getContentType().startsWith("image/")) {
                        return new CacheEntry(url);
                    }
                    int length = connection.getContentLength();
                    // buffered stream should be no more than 16k if we know the length
                    // if we don't know the length then 8k is what we will use
                    length = length > 0 ? Math.min(16384, length) : 8192;
                    try (InputStream is = connection.getInputStream();
                         BufferedInputStream bis = new BufferedInputStream(is, length)) {
                        BufferedImage image = ImageIO.read(bis);
                        if (image == null) {
                            return new CacheEntry(url);
                        }
                        return new CacheEntry(url, image, connection.getLastModified());
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, e.getMessage(), e);
                return new CacheEntry(url);
            } finally {
                long end = System.nanoTime();
                long duration = TimeUnit.NANOSECONDS.toMillis(end - start);
                LOGGER.log(duration > 250 ? Level.INFO : Level.FINE, "Avatar lookup of {0} took {1}ms",
                        new Object[]{url, duration}
                );
            }
        }
    }
}
