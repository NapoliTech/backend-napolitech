package com.pizzaria.backendpizzaria.mock;

import com.pizzaria.backendpizzaria.domain.Endereco;
import com.pizzaria.backendpizzaria.domain.Enum.CategoriaProduto;
import com.pizzaria.backendpizzaria.domain.Produto;
import com.pizzaria.backendpizzaria.domain.Usuario;
import com.pizzaria.backendpizzaria.repository.EnderecoRepository;
import com.pizzaria.backendpizzaria.repository.ProdutoRepository;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DadosMock {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void carregarDados() {
        Usuario usuario = new Usuario();
        usuario.setNome("Marcos Paulo");
        usuario.setEmail("marcos@gmail.com");
        usuario.setSenha(passwordEncoder.encode("A@123456789"));
        usuario.setCpf("123.123.123-11");
        usuario.setTelefone("(11) 99999-9999");
        usuario.setDataNasc("1989-01-10");
        usuario.setTipoUsuario("CLIENTE");
        usuario.setPedidos(0L);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        Endereco endereco = new Endereco();
        endereco.setRua("Rua Haddock Lobo");
        endereco.setBairro("Consolação");
        endereco.setNumero(595);
        endereco.setComplemento("Digital Building");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setCep("01414-001");
        endereco.setUsuarioId(usuarioSalvo.getIdUsuario());

        enderecoRepository.save(endereco);
        // Pizzas Salgadas
        Produto calabresa = new Produto();
        calabresa.setNome("Pizza de Calabresa");
        calabresa.setPreco(39.90);
        calabresa.setQuantidadeEstoque(10);
        calabresa.setIngredientes("Calabresa, Cebola, Orégano, Queijo Mussarela");
        calabresa.setCategoriaProduto(CategoriaProduto.PIZZA);

        Produto portuguesa = new Produto();
        portuguesa.setNome("Pizza Portuguesa");
        portuguesa.setPreco(42.90);
        portuguesa.setQuantidadeEstoque(10);
        portuguesa.setIngredientes("Presunto, Ovo, Cebola, Azeitona, Queijo Mussarela, Orégano");
        portuguesa.setCategoriaProduto(CategoriaProduto.PIZZA);

        Produto frangoCatupiry = new Produto();
        frangoCatupiry.setNome("Pizza de Frango com Catupiry");
        frangoCatupiry.setPreco(44.90);
        frangoCatupiry.setQuantidadeEstoque(10);
        frangoCatupiry.setIngredientes("Frango desfiado, Catupiry, Queijo Mussarela, Orégano");
        frangoCatupiry.setCategoriaProduto(CategoriaProduto.PIZZA);

        Produto quatroQueijos = new Produto();
        quatroQueijos.setNome("Pizza Quatro Queijos");
        quatroQueijos.setPreco(49.90);
        quatroQueijos.setQuantidadeEstoque(10);
        quatroQueijos.setIngredientes("Mussarela, Gorgonzola, Parmesão, Catupiry");
        quatroQueijos.setCategoriaProduto(CategoriaProduto.PIZZA);

        Produto margherita = new Produto();
        margherita.setNome("Pizza Margherita");
        margherita.setPreco(39.90);
        margherita.setQuantidadeEstoque(10);
        margherita.setIngredientes("Molho de tomate, Mussarela, Manjericão fresco");
        margherita.setCategoriaProduto(CategoriaProduto.PIZZA);

        Produto pepperoni = new Produto();
        pepperoni.setNome("Pizza de Pepperoni");
        pepperoni.setPreco(47.90);
        pepperoni.setQuantidadeEstoque(10);
        pepperoni.setIngredientes("Pepperoni, Queijo Mussarela, Molho de tomate");
        pepperoni.setCategoriaProduto(CategoriaProduto.PIZZA);

        Produto carneSeca = new Produto();
        carneSeca.setNome("Pizza de Carne Seca com Catupiry");
        carneSeca.setPreco(52.90);
        carneSeca.setQuantidadeEstoque(10);
        carneSeca.setIngredientes("Carne seca desfiada, Catupiry, Cebola roxa, Mussarela");
        carneSeca.setCategoriaProduto(CategoriaProduto.PIZZA);

        Produto vegetariana = new Produto();
        vegetariana.setNome("Pizza Vegetariana");
        vegetariana.setPreco(45.90);
        vegetariana.setQuantidadeEstoque(10);
        vegetariana.setIngredientes("Tomate, Pimentão, Cebola, Brócolis, Azeitona, Mussarela");
        vegetariana.setCategoriaProduto(CategoriaProduto.PIZZA);

        Produto bacon = new Produto();
        bacon.setNome("Pizza de Bacon");
        bacon.setPreco(46.90);
        bacon.setQuantidadeEstoque(10);
        bacon.setIngredientes("Bacon crocante, Mussarela, Molho de tomate");
        bacon.setCategoriaProduto(CategoriaProduto.PIZZA);

        Produto camarão = new Produto();
        camarão.setNome("Pizza de Camarão");
        camarão.setPreco(59.90);
        camarão.setQuantidadeEstoque(10);
        camarão.setIngredientes("Camarão, Catupiry, Mussarela, Orégano");
        camarão.setCategoriaProduto(CategoriaProduto.PIZZA);

        // Pizzas Doces
        Produto chocolate = new Produto();
        chocolate.setNome("Pizza de Chocolate");
        chocolate.setPreco(39.90);
        chocolate.setQuantidadeEstoque(10);
        chocolate.setIngredientes("Chocolate ao leite, Granulado");
        chocolate.setCategoriaProduto(CategoriaProduto.PIZZA_DOCE);

        Produto brigadeiro = new Produto();
        brigadeiro.setNome("Pizza de Brigadeiro");
        brigadeiro.setPreco(42.90);
        brigadeiro.setQuantidadeEstoque(10);
        brigadeiro.setIngredientes("Chocolate, Granulado, Leite condensado");
        brigadeiro.setCategoriaProduto(CategoriaProduto.PIZZA_DOCE);

        Produto bananaCanela = new Produto();
        bananaCanela.setNome("Pizza de Banana com Canela");
        bananaCanela.setPreco(37.90);
        bananaCanela.setQuantidadeEstoque(10);
        bananaCanela.setIngredientes("Banana, Açúcar, Canela, Chocolate branco");
        bananaCanela.setCategoriaProduto(CategoriaProduto.PIZZA_DOCE);

        Produto romeuJulieta = new Produto();
        romeuJulieta.setNome("Pizza Romeu e Julieta");
        romeuJulieta.setPreco(41.90);
        romeuJulieta.setQuantidadeEstoque(10);
        romeuJulieta.setIngredientes("Goiabada, Queijo Minas");
        romeuJulieta.setCategoriaProduto(CategoriaProduto.PIZZA_DOCE);

        // Salvando todos
        produtoRepository.save(calabresa);
        produtoRepository.save(portuguesa);
        produtoRepository.save(frangoCatupiry);
        produtoRepository.save(quatroQueijos);
        produtoRepository.save(margherita);
        produtoRepository.save(pepperoni);
        produtoRepository.save(carneSeca);
        produtoRepository.save(vegetariana);
        produtoRepository.save(bacon);
        produtoRepository.save(camarão);
        produtoRepository.save(chocolate);
        produtoRepository.save(brigadeiro);
        produtoRepository.save(bananaCanela);
        produtoRepository.save(romeuJulieta);

        // Bebidas (já tinha Coca, pode adicionar mais)
        Produto guarana = new Produto();
        guarana.setNome("Guaraná Lata 350Ml");
        guarana.setPreco(5.50);
        guarana.setQuantidadeEstoque(20);
        guarana.setIngredientes("");
        guarana.setCategoriaProduto(CategoriaProduto.BEBIDAS);

        produtoRepository.save(guarana);
    }
}

