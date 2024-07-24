package codezap.template.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codezap.template.domain.ThumbnailSnippet;

public interface ThumbnailSnippetRepository extends JpaRepository<ThumbnailSnippet, Long> {
    @Query("""
            SELECT t
            FROM ThumbnailSnippet t
            WHERE t.template.title LIKE %:topic%
            """)
    List<ThumbnailSnippet> searchByTopic(@Param("topic") String topic);

}
