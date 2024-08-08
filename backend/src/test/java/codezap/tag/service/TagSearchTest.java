package codezap.tag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import codezap.category.domain.Category;
import codezap.category.repository.CategoryRepository;
import codezap.fixture.MemberDtoFixture;
import codezap.member.domain.Member;
import codezap.member.repository.MemberRepository;
import codezap.template.domain.Snippet;
import codezap.template.domain.Tag;
import codezap.template.domain.Template;
import codezap.template.domain.TemplateTag;
import codezap.template.domain.ThumbnailSnippet;
import codezap.template.dto.request.CreateSnippetRequest;
import codezap.template.dto.request.CreateTemplateRequest;
import codezap.template.repository.SnippetRepository;
import codezap.template.repository.TagRepository;
import codezap.template.repository.TemplateRepository;
import codezap.template.repository.TemplateTagRepository;
import codezap.template.repository.ThumbnailSnippetRepository;
import codezap.template.service.TemplateService;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.AFTER_TEST_CLASS)
public class TagSearchTest {

    @LocalServerPort
    int port;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private ThumbnailSnippetRepository thumbnailSnippetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TemplateTagRepository templateTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setting() {
        RestAssured.port = port;
    }

    private void saveDefault15Templates(Member member, Category category) {
        saveTemplate(makeTemplateRequest("hello keyword 1"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 2"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 3"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 4"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 5"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 6"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 7"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 8"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 9"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 10"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 11"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 12"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 13"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 14"), member, category);
        saveTemplate(makeTemplateRequest("hello keyword 15"), member, category);
    }

    private CreateTemplateRequest makeTemplateRequest(String title) {
        return new CreateTemplateRequest(title, "description",
                List.of(new CreateSnippetRequest("filename1", "content1", 1),
                        new CreateSnippetRequest("filename2", "content2", 2)),
                1L,
                List.of());
    }

    private Template saveTemplate(CreateTemplateRequest createTemplateRequest, Member member, Category category) {
        Template savedTemplate = templateRepository.save(
                new Template(member, createTemplateRequest.title(), createTemplateRequest.description(), category));
        Snippet savedFirstSnippet = snippetRepository.save(new Snippet(savedTemplate, "filename1", "content1", 1));
        snippetRepository.save(new Snippet(savedTemplate, "filename2", "content2", 2));
        thumbnailSnippetRepository.save(new ThumbnailSnippet(savedTemplate, savedFirstSnippet));

        return savedTemplate;
    }

    @Nested
    @DisplayName("조건에 따른 페이지 조회 메서드 동작 확인")
    class FilteringPageTest {

        @Test
        @DisplayName("사용자별 태그 목록 조회 성공")
        void findBySingleTagPageSuccess() {
            Member member1 = memberRepository.fetchById(MemberDtoFixture.getFirstMemberDto().id());
            Member member2 = memberRepository.fetchById(MemberDtoFixture.getSecondMemberDto().id());
            Category category1 = categoryRepository.save(new Category("category1", member1));
            tagRepository.save(new Tag("tag1"));
            tagRepository.save(new Tag("tag2"));
            saveDefault15Templates(member1, category1);
            saveDefault15Templates(member2, category1);

            templateTagRepository.save(
                    new TemplateTag(templateRepository.fetchById(1L), tagRepository.fetchById(1L)));
            templateTagRepository.save(
                    new TemplateTag(templateRepository.fetchById(2L), tagRepository.fetchById(2L)));
            templateTagRepository.save(
                    new TemplateTag(templateRepository.fetchById(30L), tagRepository.fetchById(1L)));

            assertAll(
                    () -> assertThat(templateService.findAllTagsByMemberId(member1.getId()).tags()).hasSize(2),
                    () -> assertThat(templateService.findAllTagsByMemberId(member2.getId()).tags()).hasSize(1)
            );
        }
    }
}
