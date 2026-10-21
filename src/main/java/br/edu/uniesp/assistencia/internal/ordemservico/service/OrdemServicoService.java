package br.edu.uniesp.assistencia.internal.ordemservico.service;

import br.edu.uniesp.assistencia.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.assistencia.internal.cliente.repository.ClienteRepository;
import br.edu.uniesp.assistencia.internal.equipamento.entity.EquipamentoEntity;
import br.edu.uniesp.assistencia.internal.equipamento.repository.EquipamentoRepository;
import br.edu.uniesp.assistencia.internal.ordemservico.dto.CriarOrdemServicoRequest;
import br.edu.uniesp.assistencia.internal.ordemservico.dto.CriarOrdemServicoResponse;
import br.edu.uniesp.assistencia.internal.ordemservico.entity.OrdemServicoEntity;
import br.edu.uniesp.assistencia.internal.ordemservico.mapper.OrdemServicoMapper;
import br.edu.uniesp.assistencia.internal.ordemservico.repository.OrdemServicoRepository;
import br.edu.uniesp.assistencia.shared.exception.RecursoNaoEncontradoException;
import br.edu.uniesp.assistencia.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final EquipamentoRepository equipamentoRepository;



    public CriarOrdemServicoResponse abrirOrdemServico(CriarOrdemServicoRequest request) {

        ClienteEntity cliente = clienteRepository
                .findById(request.clienteId()).orElseThrow(()
                        -> new RecursoNaoEncontradoException
                        ("Cliente", request.clienteId()));

        if(!cliente.isAtivo()) {
            throw new RegraNegocioException("Não é permitido abrir Ordem de Serviço para um cliente inativo.");
        }

        EquipamentoEntity equipamento = equipamentoRepository
                .findById(request.equipamentoId()).orElseThrow(()
                        -> new RecursoNaoEncontradoException
                        ("Equipamento", request.equipamentoId()));

        if (!equipamento.getCliente().getId().equals(cliente.getId())) {
            throw new RegraNegocioException("O equipamento informado não pertence ao cliente indicado.");
        }

        String numeroProtocolo =gerarNumeroProtocolo();

        OrdemServicoEntity ordemServicoEntity =
                OrdemServicoMapper.paraEntidade(request,
                        numeroProtocolo,
                        cliente,
                        equipamento);
        ordemServicoRepository.save(ordemServicoEntity);
        return OrdemServicoMapper.paraResposta(ordemServicoEntity);
    }



    private String gerarNumeroProtocolo() {
        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sufixo = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "OS-" + data + "-" + sufixo;
    }

}
