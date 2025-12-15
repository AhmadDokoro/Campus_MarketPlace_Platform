package com.ahsmart.campusmarket.service.product;

import com.ahsmart.campusmarket.exceptions.APIException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {

    private final Cloudinary cloudinary;


    public FileServiceImpl(Cloudinary cloudinary)
    {
        this.cloudinary = cloudinary;
    }

    // Upload image and return URL
    @Override
    public String uploadImage(MultipartFile file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "transformation", new Transformation()
                                    .quality("auto:best")   // highest quality (AI-based compression)
                                    .fetchFormat("auto")    // auto format for modern browsers allows cloudinary to serve modern image formats if the browser accepts it.
                                    .width(2000)            // resize max width (optional, saves storage)
                                    .height(2500)           // resize max height (optional, saves storage)
                                    .crop("limit")          // don’t upscale small images, only shrink if image is larger than 1600
                    )
            );
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new APIException("Cloudinary upload failed: " + e.getMessage());
        }
    }


    // delete image
    @Override
    public void deleteImageByUrl(String imageUrl) {
        try {
            // Extract public_id from URL
            String[] parts = imageUrl.split("/");
            String fileName = parts[parts.length - 1]; // e.g., "m818wrted4cwzbgsbz8f.jpg"
            String publicId = fileName.substring(0, fileName.lastIndexOf(".")); // remove extension

            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new APIException("Cloudinary deletion failed: " + e.getMessage());
        }
    }


}
