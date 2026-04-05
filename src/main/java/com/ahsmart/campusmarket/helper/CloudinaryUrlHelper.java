package com.ahsmart.campusmarket.helper;

import org.springframework.stereotype.Component;

/**
 * Spring bean that rewrites plain Cloudinary upload URLs to thumbnail-optimised URLs.
 * Registered as @Component so Thymeleaf templates can call it as @cloudinaryUrlHelper.thumbnail(...).
 * (Thymeleaf 3.1 blocks the T() static-class syntax, so bean-reference syntax is used instead.)
 */
@Component
public class CloudinaryUrlHelper {

    private static final String UPLOAD_SEGMENT = "/upload/";
    private static final String THUMBNAIL_TRANSFORMS = "w_400,h_300,c_fill,q_auto,f_auto/";

    /**
     * Returns a thumbnail-optimised Cloudinary URL.
     * Input:  https://res.cloudinary.com/demo/image/upload/v1234/sample.jpg
     * Output: https://res.cloudinary.com/demo/image/upload/w_400,h_300,c_fill,q_auto,f_auto/v1234/sample.jpg
     *
     * If the URL is null, blank, or does not contain "/upload/", it is returned unchanged.
     */
    public String thumbnail(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        int idx = url.indexOf(UPLOAD_SEGMENT);
        if (idx == -1) {
            return url;
        }
        int insertAt = idx + UPLOAD_SEGMENT.length();
        if (url.startsWith(THUMBNAIL_TRANSFORMS, insertAt)) {
            return url;
        }
        return url.substring(0, insertAt) + THUMBNAIL_TRANSFORMS + url.substring(insertAt);
    }
}
