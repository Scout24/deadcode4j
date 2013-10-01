package de.is24.deadcode4j;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <code>Utils</code> provides some convenience methods on a technical level.
 *
 * @since 1.0.2
 */
final class Utils {

    private Utils() {
        super();
    }

    @Nonnull
    static URL[] toUrls(@Nonnull File[] files) {
        URL[] urls = new URL[files.length];
        for (int i = urls.length; i-- > 0; )
            try {
                urls[i] = files[i].toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Failed to transform [" + files[i] + "] into an URL!", e);
            }
        return urls;
    }

}
