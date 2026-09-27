package com.fintrack.dto.projection;

import java.math.BigDecimal;

/**
 * Interface-based Projection do Spring Data JPA.
 *
 * --- PONTO DE ENTREVISTA ---
 * Em vez de carregar entidades JPA inteiras para fazer relatórios (o que consumiria
 * muita memória e geraria queries lentas), o Spring Data JPA permite definir
 * uma interface simples. O Spring cria dinamicamente um proxy preenchido diretamente
 * com o resultado do SELECT do banco de dados (alta performance para dashboards).
 */
public interface CategoryTotalProjection {

    Long getCategoryId();

    String getCategoryName();

    BigDecimal getTotalAmount();

    Long getTransactionCount();
}
