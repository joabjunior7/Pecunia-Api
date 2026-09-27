package com.pecunia.repository;

import com.pecunia.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository de acesso a dados de Category.
 *
 * --- O QUE E O SPRING DATA JPA? ---
 *
 * Perceba que isso aqui e uma INTERFACE, nao uma classe.
 * Voce nao precisa implementar nada! O Spring Data JPA cria
 * a implementacao automaticamente em tempo de execucao.
 *
 * Ao estender JpaRepository<Category, Long>, voce ja ganha de graca:
 * - save(entity)        -> INSERT ou UPDATE
 * - findById(id)        -> SELECT por ID
 * - findAll()           -> SELECT *
 * - deleteById(id)      -> DELETE por ID
 * - count()             -> COUNT(*)
 * - existsById(id)      -> SELECT EXISTS
 *
 * E ainda pode criar queries customizadas so pelo NOME DO METODO!
 * O Spring interpreta o nome e gera o SQL. Exemplos abaixo.
 *
 * Pergunta de entrevista: "Como funciona o Spring Data JPA?"
 * Resposta: "Voce cria uma interface que estende JpaRepository,
 * e o Spring gera a implementacao em runtime usando proxies.
 * Queries podem ser derivadas do nome do metodo ou escritas com @Query."
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Busca todas as categorias ativas.
     *
     * O Spring interpreta o nome do metodo:
     * findBy  -> SELECT ... WHERE
     * Active  -> active = ?
     * True    -> true
     *
     * SQL gerado: SELECT * FROM categories WHERE active = true
     */
    List<Category> findByActiveTrue();

    /**
     * Busca uma categoria ativa pelo ID.
     *
     * SQL gerado: SELECT * FROM categories WHERE id = ? AND active = true
     */
    Optional<Category> findByIdAndActiveTrue(Long id);

    /**
     * Verifica se ja existe uma categoria com esse nome (case-insensitive).
     * Usado para evitar duplicatas.
     *
     * IgnoreCase -> LOWER(name) = LOWER(?)
     *
     * SQL gerado: SELECT EXISTS(SELECT 1 FROM categories WHERE LOWER(name) = LOWER(?))
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Verifica duplicata excluindo um ID especifico (para o update).
     * Sem isso, ao editar uma categoria, ela "encontraria a si mesma" como duplicata.
     *
     * SQL gerado: SELECT EXISTS(SELECT 1 FROM categories
     *             WHERE LOWER(name) = LOWER(?) AND id != ?)
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
