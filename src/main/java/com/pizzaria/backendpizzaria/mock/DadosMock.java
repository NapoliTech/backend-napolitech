package com.pizzaria.backendpizzaria.mock;

import com.pizzaria.backendpizzaria.domain.Enum.CategoriaProduto;
import com.pizzaria.backendpizzaria.domain.Produto;
import com.pizzaria.backendpizzaria.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DadosMock implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosMock.class);

    @Autowired
    private ProdutoRepository produtoRepository;

    @Override
    public void run(ApplicationArguments args) {
        carregarCardapio();
    }

    private void carregarCardapio() {
        log.info("Iniciando carga do cardápio mock...");

        // ── Pizzas Salgadas ──────────────────────────────────────────
        salvar("Pizza de Calabresa",              39.90, 10, "Calabresa, Cebola, Orégano, Queijo Mussarela",                         CategoriaProduto.PIZZA);
        salvar("Pizza Portuguesa",                42.90, 10, "Presunto, Ovo, Cebola, Azeitona, Queijo Mussarela, Orégano",           CategoriaProduto.PIZZA);
        salvar("Pizza de Frango com Catupiry",    44.90, 10, "Frango desfiado, Catupiry, Queijo Mussarela, Orégano",                 CategoriaProduto.PIZZA);
        salvar("Pizza Quatro Queijos",            49.90, 10, "Mussarela, Gorgonzola, Parmesão, Catupiry",                           CategoriaProduto.PIZZA);
        salvar("Pizza Margherita",                39.90, 10, "Molho de tomate, Mussarela, Manjericão fresco",                       CategoriaProduto.PIZZA);
        salvar("Pizza de Pepperoni",              47.90, 10, "Pepperoni, Queijo Mussarela, Molho de tomate",                        CategoriaProduto.PIZZA);
        salvar("Pizza de Carne Seca com Catupiry",52.90, 10, "Carne seca desfiada, Catupiry, Cebola roxa, Mussarela",               CategoriaProduto.PIZZA);
        salvar("Pizza Vegetariana",               45.90, 10, "Tomate, Pimentão, Cebola, Brócolis, Azeitona, Mussarela",             CategoriaProduto.PIZZA);
        salvar("Pizza de Bacon",                  46.90, 10, "Bacon crocante, Mussarela, Molho de tomate",                         CategoriaProduto.PIZZA);
        salvar("Pizza de Camarão",                59.90, 10, "Camarão, Catupiry, Mussarela, Orégano",                               CategoriaProduto.PIZZA);
        salvar("Pizza Napolitana",                43.90, 10, "Tomate, Mussarela, Alho, Azeite, Orégano",                            CategoriaProduto.PIZZA);
        salvar("Pizza de Atum",                   44.90, 10, "Atum, Cebola, Azeitona, Mussarela, Orégano",                         CategoriaProduto.PIZZA);

        // ── Pizzas Doces ─────────────────────────────────────────────
        salvar("Pizza de Chocolate",              39.90, 10, "Chocolate ao leite, Granulado",                                       CategoriaProduto.PIZZA_DOCE);
        salvar("Pizza de Brigadeiro",             42.90, 10, "Chocolate, Granulado, Leite condensado",                              CategoriaProduto.PIZZA_DOCE);
        salvar("Pizza de Banana com Canela",      37.90, 10, "Banana, Açúcar, Canela, Chocolate branco",                           CategoriaProduto.PIZZA_DOCE);
        salvar("Pizza Romeu e Julieta",           41.90, 10, "Goiabada, Queijo Minas",                                             CategoriaProduto.PIZZA_DOCE);
        salvar("Pizza de Nutella",                45.90, 10, "Nutella, Morango, Chantilly",                                         CategoriaProduto.PIZZA_DOCE);
        salvar("Pizza de Prestígio",              43.90, 10, "Chocolate ao leite, Coco ralado, Leite condensado",                   CategoriaProduto.PIZZA_DOCE);

        // ── Bebidas ──────────────────────────────────────────────────
        salvar("Coca-Cola Lata 350ml",             5.90, 30, "",                                                                    CategoriaProduto.BEBIDAS);
        salvar("Coca-Cola Zero Lata 350ml",        5.90, 30, "",                                                                    CategoriaProduto.BEBIDAS);
        salvar("Guaraná Lata 350ml",               5.50, 30, "",                                                                    CategoriaProduto.BEBIDAS);
        salvar("Sprite Lata 350ml",                5.50, 30, "",                                                                    CategoriaProduto.BEBIDAS);
        salvar("Fanta Laranja Lata 350ml",         5.50, 30, "",                                                                    CategoriaProduto.BEBIDAS);
        salvar("Água Mineral 500ml",               3.50, 50, "",                                                                    CategoriaProduto.BEBIDAS);
        salvar("Água com Gás 500ml",               4.00, 50, "",                                                                    CategoriaProduto.BEBIDAS);
        salvar("Suco de Laranja Natural 400ml",    9.90, 20, "",                                                                    CategoriaProduto.BEBIDAS);
        salvar("Suco de Limão Natural 400ml",      9.90, 20, "",                                                                    CategoriaProduto.BEBIDAS);
        salvar("Cerveja Heineken Long Neck 330ml", 9.90, 25, "",                                                                    CategoriaProduto.BEBIDAS);
        salvar("Cerveja Stella Artois 330ml",      9.90, 25, "",                                                                    CategoriaProduto.BEBIDAS);

        log.info("Carga do cardápio mock concluída.");
    }

    private void salvar(String nome, double preco, int estoque, String ingredientes, CategoriaProduto categoria) {
        if (produtoRepository.existsByNome(nome)) {
            log.debug("Produto '{}' já existe, pulando.", nome);
            return;
        }
        Produto p = new Produto();
        p.setNome(nome);
        p.setPreco(preco);
        p.setQuantidadeEstoque(estoque);
        p.setIngredientes(ingredientes);
        p.setCategoriaProduto(categoria);
        produtoRepository.save(p);
        log.info("Produto cadastrado: {}", nome);
    }
}
