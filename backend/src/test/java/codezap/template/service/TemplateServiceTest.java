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

    @BeforeEach
    void setting() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("템플릿 생성 성공")
    void createTemplateSuccess() {
        //given
        CreateTemplateRequest createTemplateRequest = makeTemplateRequest("title");

        //when
        templateService.create(createTemplateRequest);

        //then
        assertThat(templateRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("템플릿 전체 조회 성공")
    void findAllTemplatesSuccess() {
        //given
        saveTemplate(makeTemplateRequest("title1"));
        saveTemplate(makeTemplateRequest("title2"));

        //when
        FindAllTemplatesResponse allTemplates = templateService.findAll();

        //then
        assertThat(allTemplates.templates().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("템플릿 단건 조회 성공")
    void findOneTemplateSuccess() {
        //given
        CreateTemplateRequest createdTemplate = makeTemplateRequest("title");
        saveTemplate(createdTemplate);

        //when
        FindTemplateByIdResponse foundTemplate = templateService.findById(1L);

        //then
        assertAll(
                () -> assertThat(foundTemplate.title()).isEqualTo(createdTemplate.title()),
                () -> assertThat(foundTemplate.snippets().size()).isEqualTo(createdTemplate.snippets().size())
        );
    }

    @Test
    @DisplayName("템플릿 토픽 검색 성공 : 템플릿 제목에 포함")
    void findAllTemplatesTitleContainTopicSuccess() {
        //given
        saveTemplate(makeTemplateRequest("hello"));
        saveTemplate(makeTemplateRequest("hello topic"));
        saveTemplate(makeTemplateRequest("topic hello"));
        saveTemplate(makeTemplateRequest("hello topic !"));

        //when
        FindAllTemplatesResponse templates = templateService.findContainTopic("topic");

        //then
        assertThat(templates.templates()).hasSize(3);
    }

    @Test
    @DisplayName("템플릿 토픽 검색 성공 : 탬플릿 내에 스니펫 파일명 중 하나라도 포함")
    void findAllSnippetFilenameContainTopicSuccess() {
        //given
        saveTemplateBySnippetFilename("tempate1", "login.js", "signup.js");
        saveTemplateBySnippetFilename("tempate2", "login.java", "signup.java");
        saveTemplateBySnippetFilename("tempate3", "login.js", "signup.java");

        //when
        FindAllTemplatesResponse templates = templateService.findContainTopic("java");

        //then
        assertThat(templates.templates()).hasSize(2);
    }

    @Test
    @DisplayName("템플릿 토픽 검색 성공 : 탬플릿 내에 스니펫 코드 중 하나라도 포함")
    void findAllSnippetContentContainTopicSuccess() {
        //given
        saveTemplateBySnippetContent("tempate1", "public Main {", "new Car();");
        saveTemplateBySnippetContent("tempate2", "private Car", "public Movement");
        saveTemplateBySnippetContent("tempate3", "console.log", "a+b=3");

        //when
        FindAllTemplatesResponse templates = templateService.findContainTopic("Car");

        //then
        assertThat(templates.templates()).hasSize(2);
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

    private void saveTemplate(CreateTemplateRequest createTemplateRequest) {
        Template savedTemplate = templateRepository.save(new Template(createTemplateRequest.title()));
        Snippet savedFirstSnippet = snippetRepository.save(new Snippet(savedTemplate, "filename1", "content1", 1));
        snippetRepository.save(new Snippet(savedTemplate, "filename2", "content2", 2));
        thumbnailSnippetRepository.save(new ThumbnailSnippet(savedTemplate, savedFirstSnippet));
    }

    private void saveTemplateBySnippetFilename(String templateTitle, String firstFilename, String secondFilename) {
        CreateTemplateRequest createTemplateRequest = new CreateTemplateRequest(
                templateTitle,
                List.of(
                        new CreateSnippetRequest(firstFilename, "content1", 1),
                        new CreateSnippetRequest(secondFilename, "content2", 2)
                )
        );
        Template savedTemplate = templateRepository.save(new Template(createTemplateRequest.title()));

        Snippet savedFirstSnippet = snippetRepository.save(new Snippet(savedTemplate, firstFilename, "content1", 1));
        snippetRepository.save(new Snippet(savedTemplate, secondFilename, "content2", 2));
        thumbnailSnippetRepository.save(new ThumbnailSnippet(savedTemplate, savedFirstSnippet));
    }

    private void saveTemplateBySnippetContent(String templateTitle, String firstContent, String secondContent) {
        CreateTemplateRequest createTemplateRequest = new CreateTemplateRequest(
                templateTitle,
                List.of(
                        new CreateSnippetRequest("filename1", firstContent, 1),
                        new CreateSnippetRequest("filename2", secondContent, 2)
                )
        );
        Template savedTemplate = templateRepository.save(new Template(createTemplateRequest.title()));

        Snippet savedFirstSnippet = snippetRepository.save(new Snippet(savedTemplate, "filename1", firstContent, 1));
        snippetRepository.save(new Snippet(savedTemplate, "filename2", secondContent, 2));
        thumbnailSnippetRepository.save(new ThumbnailSnippet(savedTemplate, savedFirstSnippet));
    }
}
