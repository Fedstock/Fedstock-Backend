package com.fedstock.backend.store.application;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fedstock.backend.auth.infrastructure.UserEntity;
import com.fedstock.backend.auth.infrastructure.UserJpaRepository;
import com.fedstock.backend.main.error.BadRequestException;
import com.fedstock.backend.main.error.ConflictException;
import com.fedstock.backend.main.error.ForbiddenException;
import com.fedstock.backend.store.infrastructure.StoreEntity;
import com.fedstock.backend.store.infrastructure.StoreJpaRepository;
import com.fedstock.backend.store.infrastructure.StoreMemberEntity;
import com.fedstock.backend.store.infrastructure.StoreMemberJpaRepository;
import com.fedstock.backend.store.infrastructure.StoreRole;

@Service
public class StoreService {

    private final StoreJpaRepository storeRepository;
    private final StoreMemberJpaRepository storeMemberRepository;
    private final UserJpaRepository userRepository;

    public StoreService(
        StoreJpaRepository storeRepository,
        StoreMemberJpaRepository storeMemberRepository,
        UserJpaRepository userRepository
    ) {
        this.storeRepository = storeRepository;
        this.storeMemberRepository = storeMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<StoreMemberEntity> findMyStores(Long userId) {
        return storeMemberRepository.findStoresByUserId(userId);
    }

    @Transactional
    public StoreMemberEntity create(Long userId, String name, String businessType) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found."));
        StoreEntity store = storeRepository.save(new StoreEntity(requireName(name), blankToNull(businessType)));

        return storeMemberRepository.save(new StoreMemberEntity(user, store, StoreRole.OWNER));
    }

    @Transactional(readOnly = true)
    public StoreMemberEntity findMyStore(Long userId, Long storeId) {
        return requireMember(userId, storeId);
    }

    @Transactional
    public StoreMemberEntity update(Long userId, Long storeId, String name, String businessType) {
        StoreMemberEntity member = requireOwner(userId, storeId);
        if (name == null && businessType == null) {
            throw new BadRequestException("At least one field is required.");
        }

        member.getStore().update(name == null ? null : requireName(name), blankToNull(businessType));
        return member;
    }

    @Transactional(readOnly = true)
    public List<StoreMemberEntity> findMembers(Long userId, Long storeId) {
        requireMember(userId, storeId);
        return storeMemberRepository.findByStore_IdOrderByCreatedAtAsc(storeId);
    }

    @Transactional
    public StoreMemberEntity addMember(Long userId, Long storeId, String email, String role) {
        StoreMemberEntity currentMember = requireOwner(userId, storeId);
        UserEntity targetUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("User not found."));

        if (storeMemberRepository.existsByStore_IdAndUser_Id(storeId, targetUser.getId())) {
            throw new ConflictException("User is already a store member.");
        }

        StoreMemberEntity newMember = new StoreMemberEntity(
            targetUser,
            currentMember.getStore(),
            StoreRole.fromNullable(role, StoreRole.STAFF)
        );
        return storeMemberRepository.save(newMember);
    }

    @Transactional(readOnly = true)
    public StoreMemberEntity requireMember(Long userId, Long storeId) {
        return storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
            .orElseThrow(() -> new ForbiddenException("Store access is required."));
    }

    @Transactional(readOnly = true)
    public StoreMemberEntity requireOwner(Long userId, Long storeId) {
        StoreMemberEntity member = requireMember(userId, storeId);
        if (member.getRole() != StoreRole.OWNER) {
            throw new ForbiddenException("Store OWNER role is required.");
        }
        return member;
    }

    private String requireName(String name) {
        if (name == null || name.isBlank() || name.length() > 100) {
            throw new BadRequestException("Store name must be 1 to 100 characters.");
        }
        return name;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
