package com.pecunia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma categoria de transacao.
 *
 * Exemplos de categorias: Alimentacao, Transporte, Salario, Lazer, etc.
 *
 * --- POR QUE CADA ANOTACAO? ---
 *
 * @Entity     -> Diz ao JPA que esta classe mapeia uma tabela no banco
 * @Table      -> Define o nome da tabela (por padrao seria "category", mas
 *                e bom ser explicito)
 * @Id         -> Marca o campo como chave primaria
 * @GeneratedValue(IDENTITY) -> O banco gera o ID automaticamente (auto-increment)
 * @Column     -> Personaliza como o campo aparece na tabela (nullable, unique, etc.)
 *
 * LOMBOK:
 * @Data            -> Gera getters, setters, toString, equals e hashCode
 * @Builder         -> Permite criar objetos com o padrao Builder: Category.builder().name("x").build()
 * @NoArgsConstructor -> Construtor vazio (JPA exige)
 * @AllArgsConstructor -> Construtor com todos os campos
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    /**
     * Indica se a categoria e padrao do sistema ou criada pelo usuario.
     * Categorias padrao nao podem ser excluidas.
     */
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    /**
     * Indica se a categoria esta ativa.
     * Em vez de excluir do banco, desativamos (soft delete).
     *
     * Pergunta de entrevista: "Voce usa soft delete ou hard delete?"
     * Resposta: "Prefiro soft delete para manter historico. Uso um campo 'active'
     * e filtro nos repositorios para trazer apenas os ativos."
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Callbacks do JPA - executados automaticamente pelo Hibernate.
     * @PrePersist -> antes de salvar pela primeira vez
     * @PreUpdate  -> antes de atualizar
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
