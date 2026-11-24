package com.escolasegura.modules.auth.model;

public enum Role {
    COORDENADOR("Coordenador"),
    SUPERVISOR("Supervisor"),
    RESPONSAVEL("Responsável"),
    ALUNO("Aluno");

    private final String descricao;

    Role(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
