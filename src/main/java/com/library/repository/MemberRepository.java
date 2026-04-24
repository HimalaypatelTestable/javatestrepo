package com.library.repository;

import com.library.model.Member;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MemberRepository {

    private final Map<String, Member> membersById;
    private final Map<String, String> emailIndex;

    public MemberRepository() {
        this.membersById = new ConcurrentHashMap<>();
        this.emailIndex = new ConcurrentHashMap<>();
    }

    public synchronized Member save(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        Member existing = membersById.get(member.getId());
        if (existing != null && !existing.getEmail().equalsIgnoreCase(member.getEmail())) {
            emailIndex.remove(existing.getEmail().toLowerCase());
        }
        String emailKey = member.getEmail().toLowerCase();
        String holder = emailIndex.get(emailKey);
        if (holder != null && !holder.equals(member.getId())) {
            throw new IllegalStateException("Email already registered to another member: " + member.getEmail());
        }
        membersById.put(member.getId(), member);
        emailIndex.put(emailKey, member.getId());
        return member;
    }

    public Optional<Member> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(membersById.get(id));
    }

    public Optional<Member> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String memberId = emailIndex.get(email.toLowerCase());
        return memberId == null ? Optional.empty() : findById(memberId);
    }

    public List<Member> findAll() {
        return new ArrayList<>(membersById.values());
    }

    public List<Member> findByStatus(Member.Status status) {
        if (status == null) {
            return Collections.emptyList();
        }
        return membersById.values().stream()
                .filter(m -> m.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Member> findByMembershipType(Member.MembershipType type) {
        if (type == null) {
            return Collections.emptyList();
        }
        return membersById.values().stream()
                .filter(m -> m.getMembershipType() == type)
                .collect(Collectors.toList());
    }

    public List<Member> findWithOutstandingFines() {
        return membersById.values().stream()
                .filter(m -> m.getOutstandingFine() > 0)
                .sorted(Comparator.comparingDouble(Member::getOutstandingFine).reversed())
                .collect(Collectors.toList());
    }

    public List<Member> findExpiringBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return membersById.values().stream()
                .filter(m -> m.getMembershipExpiry() != null && m.getMembershipExpiry().isBefore(date))
                .collect(Collectors.toList());
    }

    public List<Member> findExpired() {
        LocalDate today = LocalDate.now();
        return membersById.values().stream()
                .filter(m -> m.getMembershipExpiry() != null && m.getMembershipExpiry().isBefore(today))
                .collect(Collectors.toList());
    }

    public List<Member> findTopReaders(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return membersById.values().stream()
                .sorted(Comparator.comparingInt(Member::getTotalBooksRead).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Member> findByNameContains(String fragment) {
        if (fragment == null || fragment.isBlank()) {
            return Collections.emptyList();
        }
        String needle = fragment.toLowerCase();
        return membersById.values().stream()
                .filter(m -> m.getFullName().toLowerCase().contains(needle))
                .collect(Collectors.toList());
    }

    public synchronized boolean deleteById(String id) {
        Member removed = membersById.remove(id);
        if (removed == null) {
            return false;
        }
        emailIndex.remove(removed.getEmail().toLowerCase());
        return true;
    }

    public long count() {
        return membersById.size();
    }

    public long countActive() {
        return membersById.values().stream()
                .filter(m -> m.getStatus() == Member.Status.ACTIVE)
                .count();
    }

    public boolean existsById(String id) {
        return id != null && membersById.containsKey(id);
    }

    public boolean existsByEmail(String email) {
        return email != null && emailIndex.containsKey(email.toLowerCase());
    }

    public double totalOutstandingFines() {
        return membersById.values().stream()
                .mapToDouble(Member::getOutstandingFine)
                .sum();
    }

    public void clear() {
        membersById.clear();
        emailIndex.clear();
    }
}
