package com.library.service;

import com.library.exception.LibraryException;
import com.library.model.Member;
import com.library.repository.MemberRepository;
import com.library.util.IdGenerator;
import com.library.util.ValidationUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MemberService {

    private static final double FINE_CAP_FOR_BORROWING = 50.0;

    private final MemberRepository memberRepository;
    private final IdGenerator idGenerator;

    public MemberService(MemberRepository memberRepository, IdGenerator idGenerator) {
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public Member registerMember(String firstName, String lastName, String email) {
        ValidationUtils.requireNonBlank(firstName, "firstName");
        ValidationUtils.requireNonBlank(lastName, "lastName");
        ValidationUtils.requireEmail(email);
        if (memberRepository.existsByEmail(email)) {
            throw new LibraryException("Email already registered: " + email);
        }
        String id = idGenerator.nextMemberId();
        Member member = new Member(id, firstName, lastName, email);
        return memberRepository.save(member);
    }

    public Member updateContactInfo(String memberId, String phone, String address) {
        Member member = requireMember(memberId);
        if (phone != null) {
            member.setPhone(phone);
        }
        if (address != null) {
            member.setAddress(address);
        }
        return memberRepository.save(member);
    }

    public Member changeEmail(String memberId, String newEmail) {
        Member member = requireMember(memberId);
        ValidationUtils.requireEmail(newEmail);
        if (memberRepository.existsByEmail(newEmail)
                && !member.getEmail().equalsIgnoreCase(newEmail)) {
            throw new LibraryException("Email already registered: " + newEmail);
        }
        member.setEmail(newEmail);
        return memberRepository.save(member);
    }

    public Member upgradeMembership(String memberId, Member.MembershipType newType) {
        Member member = requireMember(memberId);
        if (newType == null) {
            throw new IllegalArgumentException("Membership type cannot be null");
        }
        member.setMembershipType(newType);
        return memberRepository.save(member);
    }

    public Member renewMembership(String memberId, int months) {
        Member member = requireMember(memberId);
        if (member.getOutstandingFine() > 0) {
            throw new LibraryException("Cannot renew membership with outstanding fines");
        }
        member.extendMembership(months);
        return memberRepository.save(member);
    }

    public Member suspendMember(String memberId, String reason) {
        Member member = requireMember(memberId);
        member.setStatus(Member.Status.SUSPENDED);
        return memberRepository.save(member);
    }

    public Member reactivateMember(String memberId) {
        Member member = requireMember(memberId);
        if (member.getMembershipExpiry().isBefore(LocalDate.now())) {
            throw new LibraryException("Cannot reactivate expired membership without renewal");
        }
        member.setStatus(Member.Status.ACTIVE);
        return memberRepository.save(member);
    }

    public Member closeMembership(String memberId) {
        Member member = requireMember(memberId);
        if (!member.getActiveLoanIds().isEmpty()) {
            throw new LibraryException("Cannot close membership with active loans");
        }
        if (member.getOutstandingFine() > 0) {
            throw new LibraryException("Cannot close membership with outstanding fines");
        }
        member.setStatus(Member.Status.CLOSED);
        return memberRepository.save(member);
    }

    public Member payFine(String memberId, double amount) {
        Member member = requireMember(memberId);
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        member.payFine(amount);
        return memberRepository.save(member);
    }

    public boolean isEligibleToBorrow(String memberId) {
        Member member = requireMember(memberId);
        if (member.getStatus() != Member.Status.ACTIVE) {
            return false;
        }
        if (member.getMembershipExpiry().isBefore(LocalDate.now())) {
            return false;
        }
        if (member.getOutstandingFine() >= FINE_CAP_FOR_BORROWING) {
            return false;
        }
        return member.getActiveLoanIds().size() < member.getMembershipType().getMaxActiveLoans();
    }

    public void expireMembershipsIfNeeded() {
        LocalDate today = LocalDate.now();
        memberRepository.findExpiringBefore(today).forEach(member -> {
            if (member.getStatus() == Member.Status.ACTIVE) {
                member.setStatus(Member.Status.EXPIRED);
                memberRepository.save(member);
            }
        });
    }

    public Optional<Member> findById(String memberId) {
        return memberRepository.findById(memberId);
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public List<Member> findMembersWithFines() {
        return memberRepository.findWithOutstandingFines();
    }

    public List<Member> findTopReaders(int limit) {
        return memberRepository.findTopReaders(limit);
    }

    private Member requireMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new LibraryException("Member not found: " + memberId));
    }
}
