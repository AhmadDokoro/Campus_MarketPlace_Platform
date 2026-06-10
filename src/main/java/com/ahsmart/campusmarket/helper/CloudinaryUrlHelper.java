package com.ahsmart.campusmarket.helper;

import org.springframework.stereotype.Component;

/**
 * Spring bean that rewrites plain Cloudinary upload URLs to optimised URLs with responsive sizing.
 * Registered as @Component so Thymeleaf templates can call it as @cloudinaryUrlHelper.
 * (Thymeleaf 3.1 blocks the T() static-class syntax, so bean-reference syntax is used instead.)
 */
@Component
public class CloudinaryUrlHelper {

    private static final String UPLOAD_SEGMENT = "/upload/";

    // Thumbnail optimization: w_400,h_300,c_fill for consistent product grid thumbnails
    private static final String THUMBNAIL_TRANSFORMS = "w_400,h_300,c_fill,q_80,f_auto/";

    // Display optimization: w_600,c_limit for product detail page images
    private static final String DISPLAY_TRANSFORMS = "w_600,c_limit,q_80,f_auto/";

    // Small/thumbnail for search results: w_150,h_150,c_fill for compact display
    private static final String SEARCH_TRANSFORMS = "w_150,h_150,c_fill,q_75,f_auto/";

    /**
     * Returns a thumbnail-optimised Cloudinary URL.
     * Applies: w_400,h_300,c_fill,q_80,f_auto
     * Input:  https://res.cloudinary.com/demo/image/upload/v1234/sample.jpg
     * Output: https://res.cloudinary.com/demo/image/upload/w_400,h_300,c_fill,q_80,f_auto/v1234/sample.jpg
     *
     * If the URL is null, blank, or does not contain "/upload/", it is returned unchanged.
     */
    public String thumbnail(String url) {
        return applyTransform(url, THUMBNAIL_TRANSFORMS);
    }

    /**
     * Returns a display-optimised Cloudinary URL for product detail pages.
     * Applies: w_600,c_limit,q_80,f_auto
     * Optimized for larger display without fixed height ratio.
     */
    public String display(String url) {
        return applyTransform(url, DISPLAY_TRANSFORMS);
    }

    /**
     * Returns a small thumbnail-optimised Cloudinary URL for search results and dropdowns.
     * Applies: w_150,h_150,c_fill,q_75,f_auto
     * More aggressive compression for small display sizes.
     */
    public String search(String url) {
        return applyTransform(url, SEARCH_TRANSFORMS);
    }

    /**
     * Internal helper that applies transformation parameters to a Cloudinary URL.
     * Checks if the transform is already applied to avoid duplicate parameters.
     */
    private String applyTransform(String url, String transformParams) {
        if (url == null || url.isBlank()) {
            return url;
        }
        int idx = url.indexOf(UPLOAD_SEGMENT);
        if (idx == -1) {
            return url;
        }
        int insertAt = idx + UPLOAD_SEGMENT.length();
        if (url.startsWith(transformParams, insertAt) ||
            alreadyHasTransforms(url, insertAt)) {
            return url;
        }
        return url.substring(0, insertAt) + transformParams + url.substring(insertAt);
    }

    /**
     * Check if URL already has transformation parameters (e.g., w_, h_, q_, f_).
     */
    private boolean alreadyHasTransforms(String url, int insertAt) {
        if (insertAt >= url.length()) {
            return false;
        }
        // Look ahead for common transform parameters
        String nextSegment = url.substring(insertAt);
        return nextSegment.startsWith("w_") ||
               nextSegment.startsWith("h_") ||
               nextSegment.startsWith("c_") ||
               nextSegment.startsWith("q_");
    }
}
