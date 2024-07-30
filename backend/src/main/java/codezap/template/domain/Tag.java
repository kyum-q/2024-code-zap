package codezap.template.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import codezap.global.auditing.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Tag extends BaseTimeEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String value;
}