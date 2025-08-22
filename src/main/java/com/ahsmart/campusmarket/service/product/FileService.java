package com.ahsmart.campusmarket.service.product;

import com.ahsmart.campusmarket.model.Product;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    // Upload image and return URL
    String uploadImage(MultipartFile file);

    // delete image
    void deleteImageByUrl(String imageUrl);


}
