package com.escolasegura.modules.reports.service;

import com.escolasegura.shared.domain.Notificavel;
import com.escolasegura.shared.domain.RegistroAuditavel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditoriaPolimorficaService {

    public List<String> gerarTrilhaAuditoriaUnificada(List<? extends RegistroAuditavel> registros) {
        return registros.stream()
                .map(RegistroAuditavel::resumirEvento)
                .toList();
    }

    public List<String> formatarFilaNotificacoes(List<? extends Notificavel> eventosNotificaveis) {
        return eventosNotificaveis.stream()
                .map(evento -> "[" + evento.getNivelSeveridade() + "] " + evento.gerarMensagemNotificacao())
                .toList();
    }
}