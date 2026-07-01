# language: pt
@fiscal @nfce @multi_tenant

Funcionalidade: Emissao fiscal NFC-e multi-tenant no MVP
  Como administrador ou operador autorizado do ERP Kalles
  Quero emitir NFC-e a partir de uma venda finalizada do PDV
  Para entregar documento fiscal valido sem expor dados entre tenants e filiais

  Contexto:
    Dado que o tenant atual possui uma empresa fiscal configurada para NFC-e em homologacao
    E a filial ativa possui certificado digital valido
    E a venda do PDV pertence ao tenant e filial ativos

  Cenario: Emitir NFC-e em homologacao para venda finalizada e paga
    Dado que existe uma venda finalizada e paga com itens fiscais validos
    E a venda ainda nao possui documento fiscal autorizado
    Quando o usuario solicitar a emissao da NFC-e para a venda
    Entao a resposta fiscal deve ter status HTTP 201
    E o documento fiscal deve ser criado para o tenant atual
    E o documento fiscal deve ser vinculado a filial ativa
    E o documento fiscal deve registrar o modelo "NFCE"
    E o documento fiscal deve registrar o ambiente "HOMOLOGACAO"
    E o documento fiscal deve registrar status "AUTORIZADO"
    E o documento fiscal deve armazenar a chave de acesso retornada pela SEFAZ
    E o documento fiscal deve armazenar o numero de protocolo retornado pela SEFAZ

  Cenario: Bloquear emissao quando a venda ainda nao esta paga
    Dado que existe uma venda aberta ou pendente de pagamento
    Quando o usuario solicitar a emissao da NFC-e para a venda
    Entao a resposta fiscal deve ter status HTTP 409
    E a resposta fiscal deve informar "A NFC-e so pode ser emitida para venda finalizada e paga"
    E nenhum documento fiscal deve ser criado

  Cenario: Bloquear emissao quando a venda ja possui documento autorizado
    Dado que existe uma venda finalizada e paga com documento fiscal autorizado
    Quando o usuario solicitar a emissao da NFC-e para a venda
    Entao a resposta fiscal deve ter status HTTP 409
    E a resposta fiscal deve informar "A venda ja possui documento fiscal autorizado"
    E nenhum novo documento fiscal deve ser criado

  Cenario: Bloquear emissao sem configuracao fiscal da filial
    Dado que a filial ativa nao possui configuracao fiscal para NFC-e
    E existe uma venda finalizada e paga com itens fiscais validos
    Quando o usuario solicitar a emissao da NFC-e para a venda
    Entao a resposta fiscal deve ter status HTTP 409
    E a resposta fiscal deve informar "Configuracao fiscal da filial nao encontrada"
    E nenhum documento fiscal deve ser criado

  Cenario: Bloquear emissao sem certificado digital valido
    Dado que a filial ativa nao possui certificado digital valido
    E existe uma venda finalizada e paga com itens fiscais validos
    Quando o usuario solicitar a emissao da NFC-e para a venda
    Entao a resposta fiscal deve ter status HTTP 409
    E a resposta fiscal deve informar "Certificado digital valido e obrigatorio para emissao fiscal"
    E nenhum documento fiscal deve ser criado

  Cenario: Bloquear emissao com item sem classificacao fiscal minima
    Dado que existe uma venda finalizada e paga com item sem NCM
    Quando o usuario solicitar a emissao da NFC-e para a venda
    Entao a resposta fiscal deve ter status HTTP 400
    E a resposta fiscal deve informar "Todos os itens da NFC-e devem possuir classificacao fiscal minima"
    E nenhum documento fiscal deve ser criado

  Cenario: Registrar rejeicao da SEFAZ sem autorizar documento fiscal
    Dado que existe uma venda finalizada e paga com itens fiscais validos
    E a SEFAZ retorna rejeicao "Rejeicao: total da NFC-e difere do somatorio dos itens"
    Quando o usuario solicitar a emissao da NFC-e para a venda
    Entao a resposta fiscal deve ter status HTTP 422
    E o documento fiscal deve ser criado para o tenant atual
    E o documento fiscal deve registrar status "REJEITADO"
    E o documento fiscal deve armazenar o motivo da rejeicao da SEFAZ
    E o documento fiscal nao deve armazenar protocolo de autorizacao

  Cenario: Isolar emissao entre tenants
    Dado que existe uma venda finalizada e paga em outro tenant
    Quando o usuario do tenant atual solicitar a emissao da NFC-e para essa venda
    Entao a resposta fiscal deve ter status HTTP 404
    E nenhum documento fiscal deve ser criado no tenant atual
    E nenhum documento fiscal deve ser criado no outro tenant

  Cenario: Isolar emissao entre filiais do mesmo tenant
    Dado que existe uma venda finalizada e paga em outra filial do mesmo tenant
    Quando o usuario solicitar a emissao da NFC-e informando a filial ativa atual
    Entao a resposta fiscal deve ter status HTTP 404
    E nenhum documento fiscal deve ser criado para a filial ativa atual
    E nenhum documento fiscal deve ser criado para a outra filial
