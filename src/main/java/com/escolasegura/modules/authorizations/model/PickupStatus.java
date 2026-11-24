package com.escolasegura.modules.authorizations.model;

public enum PickupStatus {
    VALIDA("Válida"),
    VENCIDA("Vencida"),
    REVOGADA("Revogada");

    private final String descricao;

    PickupStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
