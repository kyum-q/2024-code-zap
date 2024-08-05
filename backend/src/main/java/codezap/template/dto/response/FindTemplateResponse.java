package codezap.template.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import codezap.category.dto.response.FindCategoryResponse;
import codezap.template.domain.Snippet;
import codezap.template.domain.Tag;
import codezap.template.domain.Template;
import io.swagger.v3.oas.annotations.media.Schema;

public record FindTemplateResponse(
        @Schema(description = "템플릿 식별자", example = "0")
        Long id,

        @Schema(description = "템플릿 이름", example = "스프링 로그인 구현")
        String title,

        @Schema(description = "템플릿 설명", example = "JWT를 사용하여 로그인 기능을 구현함")
        String description,

        @Schema(description = "스니펫 목록")
        List<FindAllSnippetByTemplateResponse> snippets,

        @Schema(description = "카테고리 정보")
        FindCategoryResponse category,

        @Schema(description = "태그 목록")
        List<FindTagResponse> tags,

        @Schema(description = "템플릿 수정 시간", example = "2024-11-11 12:00", type = "string")
        LocalDateTime modifiedAt
) {
    public static FindTemplateResponse of(Template template, List<Snippet> snippets, List<Tag> tags) {
        return new FindTemplateResponse(
                template.getId(),
                template.getTitle(),
                template.getDescription(),
                mapToFindAllSnippetByTemplateResponse(snippets),
                FindCategoryResponse.from(template.getCategory()),
                mapToFindTagByTemplateResponse(tags),
                template.getModifiedAt()
        );
    }

    private static List<FindAllSnippetByTemplateResponse> mapToFindAllSnippetByTemplateResponse(
            List<Snippet> snippets
    ) {
        return snippets.stream()
                .map(FindAllSnippetByTemplateResponse::from)
                .toList();
    }

    private static List<FindTagResponse> mapToFindTagByTemplateResponse(
            List<Tag> tags
    ) {
        return tags.stream()
                .map(FindTagResponse::from)
                .toList();
    }
}