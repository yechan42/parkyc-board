package idusw.springboot.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import idusw.springboot.domain.Member;
import idusw.springboot.domain.PageRequestDTO;
import idusw.springboot.domain.PageResultDTO;
import idusw.springboot.entity.MemberEntity;
import idusw.springboot.entity.QMemberEntity;
import idusw.springboot.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public int create(Member m) {
        MemberEntity entity = MemberEntity.builder()
                .seq(m.getSeq())
                .email(m.getEmail())
                .name(m.getName())
                .pw(m.getPw())
                .build();
        if (memberRepository.save(entity) != null) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public Member read(Member m) {
        MemberEntity e = memberRepository.getById(m.getSeq());
        Member result = new Member();
        System.out.println(e);
        result.setSeq(e.getSeq());
        result.setEmail(e.getEmail());
        result.setName(e.getName());
        return result;
    }

    @Override
    public List<Member> readList() {
        List<MemberEntity> entities = memberRepository.findAll();
        List<Member> members = new ArrayList<>();
        for (MemberEntity e : entities) {
            Member m = Member.builder()
                    .seq(e.getSeq())
                    .email(e.getEmail())
                    .name(e.getName())
                    .pw(e.getPw())
                    .regDate(e.getRegDate())
                    .modDate(e.getModDate())
                    .build();
            members.add(m);
        }
        return members;
    }

    @Override
    public int update(Member m) {
        MemberEntity entity = MemberEntity.builder()
                .seq(m.getSeq())
                .email(m.getEmail())
                .name(m.getName())
                .pw(m.getPw())
                .build();
        if (memberRepository.save(entity) != null) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int delete(Member m) {
        MemberEntity entity = MemberEntity.builder()
                .seq(m.getSeq())
                .build();
        memberRepository.deleteById(entity.getSeq());
        return 1;
    }
    @Override
    public int checkEmail(Member m) {
        List<MemberEntity> memberEntityList = memberRepository.getMemberEntitiesByEmail(m.getEmail());
        if(memberEntityList.size() > 0)
            return 1; // 중복
        else
            return 0; // 사용가능
    }

    @Override
    public Member login(Member m) {
        MemberEntity e = memberRepository.getByEmailPw(m.getEmail(), m.getPw());
        System.out.println("login: " + e);
        if (e != null) {
            return Member.builder()
                    .seq(e.getSeq())
                    .email(e.getEmail())
                    .name(e.getName())
                    .build();
        }
        return null;
    }

    @Override
    public PageResultDTO<Member, MemberEntity> getList(PageRequestDTO requestDTO) {
        Sort sort = Sort.by("seq").descending();
        Pageable pageable = requestDTO.getPageable(sort);
        BooleanBuilder booleanBuilder = findByCondition(requestDTO);
        Page<MemberEntity> result = memberRepository.findAll(booleanBuilder, pageable);
        Function<MemberEntity, Member> fn = this::entityToDto;
        return new PageResultDTO<>(result, fn, requestDTO.getPerPagination());
    }

    private BooleanBuilder findByCondition(PageRequestDTO pageRequestDTO) {
        String type = pageRequestDTO.getType();
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QMemberEntity qMemberEntity = QMemberEntity.memberEntity;
        booleanBuilder.and(qMemberEntity.seq.gt(0L));
        if (type == null || type.trim().length() == 0) {
            return booleanBuilder;
        }
        String keyword = pageRequestDTO.getKeyword();
        System.out.println("findByCondition " + type + " : " + keyword);
        BooleanBuilder conditionBuilder = new BooleanBuilder();
        if (type.contains("e")) {
            conditionBuilder.or(qMemberEntity.email.contains(keyword));
        }
        if (type.contains("n")) {
            conditionBuilder.or(qMemberEntity.name.contains(keyword));
        }
        booleanBuilder.and(conditionBuilder);
        return booleanBuilder;
    }

    public Member entityToDto(MemberEntity entity) {
        return Member.builder()
                .seq(entity.getSeq())
                .email(entity.getEmail())
                .name(entity.getName())
                .build();
    }
}
