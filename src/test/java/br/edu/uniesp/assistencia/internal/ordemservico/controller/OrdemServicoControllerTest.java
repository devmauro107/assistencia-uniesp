package br.edu.uniesp.assistencia.internal.ordemservico.controller;

import br.edu.uniesp.assistencia.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.assistencia.internal.cliente.repository.ClienteRepository;
import br.edu.uniesp.assistencia.internal.equipamento.entity.EquipamentoEntity;
import br.edu.uniesp.assistencia.internal.equipamento.repository.EquipamentoRepository;
import br.edu.uniesp.assistencia.internal.ordemservico.repository.OrdemServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
class OrdemServicoControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    private ClienteEntity clienteAtivo;
    private ClienteEntity clienteInativo;
    private ClienteEntity outroCliente;
    private EquipamentoEntity equipamentoDoClienteAtivo;
    private EquipamentoEntity equipamentoDeOutroCliente;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        ordemServicoRepository.deleteAll();
        equipamentoRepository.deleteAll();
        clienteRepository.deleteAll();

        clienteAtivo = clienteRepository.save(new ClienteEntity("Cliente Ativo", "11122233344", "ativo@email.com"));

        clienteInativo = new ClienteEntity("Cliente Inativo", "22233344455", "inativo@email.com");
        clienteInativo.inativar();
        clienteInativo = clienteRepository.save(clienteInativo);

        outroCliente = clienteRepository.save(new ClienteEntity("Outro Cliente", "33344455566", "outro@email.com"));

        equipamentoDoClienteAtivo = new EquipamentoEntity("NOTEBOOK", "Dell", "Inspiron", "SN123");
        equipamentoDoClienteAtivo.associarCliente(clienteAtivo);
        equipamentoDoClienteAtivo = equipamentoRepository.save(equipamentoDoClienteAtivo);

        equipamentoDeOutroCliente = new EquipamentoEntity("NOTEBOOK", "Lenovo", "ThinkPad", "SN456");
        equipamentoDeOutroCliente.associarCliente(outroCliente);
        equipamentoDeOutroCliente = equipamentoRepository.save(equipamentoDeOutroCliente);
    }

    @Test
    @DisplayName("Cenário 1: Sucesso na Abertura de Ordem de Serviço (HTTP 201)")
    void deveAbrirOrdemServicoComSucesso() throws Exception {
        String json = """
                {
                    "clienteId": %d,
                    "equipamentoId": %d,
                    "descricaoProblema": "Notebook não liga após queda de energia elétrica."
                }
                """.formatted(clienteAtivo.getId(), equipamentoDoClienteAtivo.getId());

        mockMvc.perform(post("/ordens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.numeroProtocolo").value(org.hamcrest.Matchers.startsWith("OS-")))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.dataAbertura").exists());
    }

    @Test
    @DisplayName("Cenário 2: Validação de Campos Obrigatórios (HTTP 400)")
    void deveRetornar400QuandoCamposForemInvalidos() throws Exception {
        String json = """
                {
                    "clienteId": null,
                    "equipamentoId": null,
                    "descricaoProblema": ""
                }
                """;

        mockMvc.perform(post("/ordens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados de requisição inválidos"));
    }

    @Test
    @DisplayName("Cenário 3: Cliente Inexistente (HTTP 404)")
    void deveRetornar404QuandoClienteNaoExistir() throws Exception {
        String json = """
                {
                    "clienteId": 99999,
                    "equipamentoId": %d,
                    "descricaoProblema": "Defeito qualquer"
                }
                """.formatted(equipamentoDoClienteAtivo.getId());

        mockMvc.perform(post("/ordens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"));
    }

    @Test
    @DisplayName("Cenário 4: Violação de Cliente Inativo (HTTP 422)")
    void deveRetornar422QuandoClienteEstiverInativo() throws Exception {
        EquipamentoEntity eqInativo = new EquipamentoEntity("NOTEBOOK", "HP", "Pavilion", "SN789");
        eqInativo.associarCliente(clienteInativo);
        eqInativo = equipamentoRepository.save(eqInativo);

        String json = """
                {
                    "clienteId": %d,
                    "equipamentoId": %d,
                    "descricaoProblema": "Defeito no teclado"
                }
                """.formatted(clienteInativo.getId(), eqInativo.getId());

        mockMvc.perform(post("/ordens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Não é permitido abrir Ordem de Serviço para um cliente inativo."));
    }

    @Test
    @DisplayName("Cenário 5: Violação de Vínculo de Equipamento (HTTP 422)")
    void deveRetornar422QuandoEquipamentoNaoPertencerAoCliente() throws Exception {
        String json = """
                {
                    "clienteId": %d,
                    "equipamentoId": %d,
                    "descricaoProblema": "Defeito na tela"
                }
                """.formatted(clienteAtivo.getId(), equipamentoDeOutroCliente.getId());

        mockMvc.perform(post("/ordens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("O equipamento informado não pertence ao cliente indicado."));
    }
}
