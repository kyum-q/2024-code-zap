package codezap.template.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import codezap.template.domain.Snippet;
import codezap.template.domain.Template;
import codezap.template.domain.ThumbnailSnippet;
import codezap.template.dto.request.CreateSnippetRequest;
import codezap.template.dto.request.CreateTemplateRequest;
import codezap.template.dto.request.UpdateSnippetRequest;
import codezap.template.dto.request.UpdateTemplateRequest;
import codezap.template.dto.response.FindAllTemplatesResponse;
import codezap.template.dto.response.FindTemplateByIdResponse;
import codezap.template.repository.SnippetRepository;
import codezap.template.repository.TemplateRepository;
import codezap.template.repository.ThumbnailSnippetRepository;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.AFTER_TEST_CLASS)
class TemplateServiceTest {

    @Autowired
    private TemplateService templateService;

    @LocalServerPort
    int port;
    @Autowired
    private TemplateRepository templateRepository;
    @Autowired
    private SnippetRepository snippetRepository;
    @Autowired
    private ThumbnailSnippetRepository thumbnailSnippetRepository;

    @BeforeEach
    void setting() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("템플릿 생성 성공")
    void createTemplateSuccess() {
        // given
        CreateTemplateRequest createTemplateRequest = makeTemplateRequest("title");

        // when
        templateService.create(createTemplateRequest);

        // then
        assertThat(templateRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("템플릿 전체 조회 성공")
    void findAllTemplatesSuccess() {
        // given
        saveTemplate(makeTemplateRequest("title1"));
        saveTemplate(makeTemplateRequest("title2"));

        // when
        FindAllTemplatesResponse allTemplates = templateService.findAll();

        // then
        assertThat(allTemplates.templates()).hasSize(2);
    }

    @Test
    @DisplayName("템플릿 단건 조회 성공")
    void findOneTemplateSuccess() {
        // given
        CreateTemplateRequest createdTemplate = makeTemplateRequest("title");
        Template template = saveTemplate(createdTemplate);

        // when
        FindTemplateByIdResponse foundTemplate = templateService.findById(template.getId());

        // then
        assertAll(
                () -> assertThat(foundTemplate.title()).isEqualTo(template.getTitle()),
                () -> assertThat(foundTemplate.snippets()).hasSize(snippetRepository.findAllByTemplate(template).size())
        );
    }

    @Test
    @DisplayName("템플릿 수정 성공")
    void updateTemplateSuccess() {
        // given
        CreateTemplateRequest createdTemplate = makeTemplateRequest("title");
        Template template = saveTemplate(createdTemplate);

        // when
        UpdateTemplateRequest updateTemplateRequest = makeUpdateTemplateRequest("updateTitle");
        templateService.update(template.getId(), updateTemplateRequest);
        List<Snippet> snippets = snippetRepository.findAllByTemplate(template);
        ThumbnailSnippet thumbnailSnippet = thumbnailSnippetRepository.findById(template.getId()).get();

        // then
        assertAll(
                () -> assertThat(updateTemplateRequest.title()).isEqualTo("updateTitle"),
                () -> assertThat(thumbnailSnippet.getSnippet().getId()).isEqualTo(2L),
                () -> assertThat(snippets).hasSize(3)
        );
    }

    @Test
    @DisplayName("템플릿 삭제 성공")
    void deleteTemplateSuccess() {
        // given
        CreateTemplateRequest createdTemplate = makeTemplateRequest("title");
        saveTemplate(createdTemplate);

        // when
        templateService.deleteById(1L);

        // then
        assertAll(
                () -> assertThat(templateRepository.findAll()).isEmpty(),
                () -> assertThat(snippetRepository.findAll()).isEmpty(),
                () -> assertThat(thumbnailSnippetRepository.findAll()).isEmpty()
        );
    }

    private CreateTemplateRequest makeTemplateRequest(String title) {
        return new CreateTemplateRequest(
                title,
                List.of(
                        new CreateSnippetRequest("filename1", "content1", 1),
                        new CreateSnippetRequest("filename2", "content2", 2)
                )
        );
    }

    private UpdateTemplateRequest makeUpdateTemplateRequest(String title) {
        return new UpdateTemplateRequest(
                title,
                List.of(
                        new CreateSnippetRequest("filename3", "content3", 2),
                        new CreateSnippetRequest("filename4", "content4", 3)
                ),
                List.of(
                        new UpdateSnippetRequest(2L, "filename2", "content2", 1)
                ),
                List.of(1L)
        );
    }

    private Template saveTemplate(CreateTemplateRequest createTemplateRequest) {
        Template savedTemplate = templateRepository.save(new Template(createTemplateRequest.title()));
        Snippet savedFirstSnippet = snippetRepository.save(new Snippet(savedTemplate, "filename1", "content1", 1));
        snippetRepository.save(new Snippet(savedTemplate, "filename2", "content2", 2));
        thumbnailSnippetRepository.save(new ThumbnailSnippet(savedTemplate, savedFirstSnippet));

        return savedTemplate;
    }
}
