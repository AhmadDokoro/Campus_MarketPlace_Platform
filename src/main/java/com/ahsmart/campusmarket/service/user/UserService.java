package com.ahsmart.campusmarket.service.user;

import com.ahsmart.campusmarket.payloadDTOs.user.UserProfileFormDTO;

/**
 * User-facing flows that are not strictly authentication.
 */
public interface UserService {

    /**
     * Decide where the user should go when clicking "Start selling".
     *
     * @param userId authenticated user's id
     * @return decision containing seller status (or null when no seller profile exists)
     */
    StartSellingDecision decideStartSelling(Long userId);

    UserProfileFormDTO getUserProfile(Long userId);

    UserProfileFormDTO updateUserProfile(Long userId, UserProfileFormDTO profileForm);
}

