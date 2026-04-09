package com.ahsmart.campusmarket.service.user;

import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.UserAddress;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.payloadDTOs.user.UserProfileFormDTO;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UserAddressRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final SellerRepository sellerRepository;
    private final UserAddressRepository userAddressRepository;

    public UserServiceImpl(UsersRepository usersRepository,
                           SellerRepository sellerRepository,
                           UserAddressRepository userAddressRepository) {
        this.usersRepository = usersRepository;
        this.sellerRepository = sellerRepository;
        this.userAddressRepository = userAddressRepository;
    }

    @Override
    public StartSellingDecision decideStartSelling(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        Optional<Users> userOpt = usersRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        Users user = userOpt.get();
        Optional<Seller> sellerOpt = sellerRepository.findByUser(user);

        if (sellerOpt.isEmpty()) {
            // No seller profile yet
            return new StartSellingDecision(null, null);
        }

        Seller seller = sellerOpt.get();
        SellerStatus status = seller.getStatus();

        String reason = null;
        if (status == SellerStatus.REJECTED) {
            reason = seller.getRejectionReason();
        }

        return new StartSellingDecision(status, reason);
    }

    @Override
    public UserProfileFormDTO getUserProfile(Long userId) {
        Users user = getRequiredUser(userId);
        UserProfileFormDTO profile = new UserProfileFormDTO();
        mapUserToProfile(user, profile);
        userAddressRepository.findFirstByUser_UserIdOrderByAddressIdDesc(userId)
                .ifPresent(address -> mapAddressToProfile(address, profile));
        return profile;
    }

    @Override
    public UserProfileFormDTO updateUserProfile(Long userId, UserProfileFormDTO profileForm) {
        if (profileForm == null) {
            throw new IllegalArgumentException("Profile data is required");
        }

        Users user = getRequiredUser(userId);

        String email = clean(profileForm.getEmail());
        if (email == null) {
            throw new IllegalArgumentException("Email is required");
        }
        if (usersRepository.existsByEmailAndUserIdNot(email, userId)) {
            throw new IllegalArgumentException("This email is already in use");
        }

        String academicId = clean(profileForm.getAcademicId());
        if (academicId == null) {
            throw new IllegalArgumentException("Academic ID is required");
        }
        if (usersRepository.existsByAcademicIdAndUserIdNot(academicId, userId)) {
            throw new IllegalArgumentException("This academic ID is already in use");
        }

        String firstName = clean(profileForm.getFirstName());
        String lastName = clean(profileForm.getLastName());
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("First name and last name are required");
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(clean(profileForm.getPhone()));
        user.setAcademicId(academicId);
        user.setLevel(clean(profileForm.getLevel()));
        usersRepository.save(user);

        UserAddress address = userAddressRepository.findFirstByUser_UserIdOrderByAddressIdDesc(userId)
                .orElseGet(() -> {
                    UserAddress newAddress = new UserAddress();
                    newAddress.setUser(user);
                    return newAddress;
                });

        address.setHostelBlock(clean(profileForm.getHostelBlock()));
        address.setFloor(clean(profileForm.getFloor()));
        address.setRoomNumber(clean(profileForm.getRoomNumber()));
        address.setCity(clean(profileForm.getCity()));
        address.setState(clean(profileForm.getState()));
        userAddressRepository.save(address);

        UserProfileFormDTO updated = new UserProfileFormDTO();
        mapUserToProfile(user, updated);
        mapAddressToProfile(address, updated);
        return updated;
    }

    private Users getRequiredUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        return usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void mapUserToProfile(Users user, UserProfileFormDTO profile) {
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setEmail(user.getEmail());
        profile.setPhone(user.getPhone());
        profile.setAcademicId(user.getAcademicId());
        profile.setLevel(user.getLevel());
    }

    private void mapAddressToProfile(UserAddress address, UserProfileFormDTO profile) {
        profile.setHostelBlock(address.getHostelBlock());
        profile.setFloor(address.getFloor());
        profile.setRoomNumber(address.getRoomNumber());
        profile.setCity(address.getCity());
        profile.setState(address.getState());
    }

    private String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

