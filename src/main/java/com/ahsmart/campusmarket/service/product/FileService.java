package com.ahsmart.campusmarket.service.product;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    // Upload image and return URL
    String uploadImage(MultipartFile file);

    // Uploads an image from a remote URL — used when seller picks from the image library.
    String uploadImageFromUrl(String url);

    // delete image
    void deleteImageByUrl(String imageUrl);


}
