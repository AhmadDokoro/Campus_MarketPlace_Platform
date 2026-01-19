CREATE DATABASE campus_marketplace;
USE campus_marketplace;


CREATE TABLE mentors (
    mentor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mentor_name VARCHAR(150) NOT NULL,
    mentor_email VARCHAR(255) NOT NULL UNIQUE
);


CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    user_password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    academic_id VARCHAR(50) NOT NULL UNIQUE,
    `level` VARCHAR(50),
    mentor_id BIGINT,
    role ENUM('BUYER', 'SELLER', 'ADMIN') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_mentor
        FOREIGN KEY (mentor_id) REFERENCES mentors(mentor_id)
);


-- seller
CREATE TABLE sellers (
    seller_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    id_card_image_url VARCHAR(500) NOT NULL,
    mynemo_profile_url VARCHAR(500),
    `status` ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    reviewer_id BIGINT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_seller_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),

    CONSTRAINT fk_seller_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES users(user_id)
);

CREATE TABLE user_addresses (
    address_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    Hostel_block VARCHAR(50),
    floor VARCHAR(20),
    room_number VARCHAR(20),
    city VARCHAR(100),
    state VARCHAR(100),

    CONSTRAINT fk_address_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(255)
);


CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    `description` TEXT,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    `condition` ENUM('NEW', 'USED'),
    flagged_status ENUM('UNKNOWN','SUSPICIOUS', 'VERIFIED') DEFAULT 'UNKNOWN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_seller
        FOREIGN KEY (seller_id) REFERENCES sellers(seller_id),

    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id)
);


CREATE TABLE product_images (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    public_id VARCHAR(255),
    is_primary BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_image_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
);



CREATE TABLE cart (
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);


CREATE TABLE cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cart_item_cart
        FOREIGN KEY (cart_id) REFERENCES cart(cart_id),

    CONSTRAINT fk_cart_item_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
);


CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    `status` ENUM('PENDING_PAYMENT', 'PAID', 'CANCELLED', 'REFUNDED') NOT NULL,
    delivery_status ENUM('PENDING', 'IN_CAMPUS', 'DELIVERED') NOT NULL,
    delivery_address_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_buyer
        FOREIGN KEY (buyer_id) REFERENCES users(user_id),

    CONSTRAINT fk_order_address
        FOREIGN KEY (delivery_address_id) REFERENCES user_addresses(address_id)
);


CREATE TABLE order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id),

    CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id) REFERENCES products(product_id),

    CONSTRAINT fk_order_item_seller
        FOREIGN KEY (seller_id) REFERENCES sellers(seller_id)
);




CREATE TABLE payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    provider_reference VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    `status` ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') NOT NULL,
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
);


CREATE TABLE chats (
    chat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chat_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
);





CREATE TABLE messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_chat
        FOREIGN KEY (chat_id) REFERENCES chats(chat_id),

    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id) REFERENCES users(user_id)
);


CREATE TABLE reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reviewer_id BIGINT NOT NULL,
    target_seller_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_review_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES users(user_id),

    CONSTRAINT fk_review_target
        FOREIGN KEY (target_seller_id) REFERENCES users(user_id),

    CONSTRAINT fk_review_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
);


