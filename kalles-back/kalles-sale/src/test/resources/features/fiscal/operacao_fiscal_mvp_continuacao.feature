# language: pt
@fiscal @nfce @nfe @multi_tenant @wip

Funcionalidade: Operacao fiscal completa do MVP
  Como administrador do ERP Kalles
  Quero configurar a emissao fiscal, classificar produtos, consultar documentos e emitir devolucoes
  Para operar o fiscal do MVP com rastreabilidade por tenant e filial

  Contexto:
    Dado que existe um administrador autenticado no tenant atual
    E existe uma filial ativa no tenant atual

  Cenario: Configurar emissor NFC-e da filial com dados fiscais validos
    Quando o administrador salvar a configuracao fiscal NFC-e da filial com:
      | campo       | valor       |
      | ambiente    | HOMOLOGACAO |
      | uf          | SP          |
      | cscId       | 1           |
      | cscToken    | CSC-TESTE   |
      | serie       | 1           |
      | proximoNumero | 100       |
    Entao a resposta fiscal deve ter status HTTP 201
    E a configuracao fiscal deve pertencer ao tenant atual
    E a configuracao fiscal deve pertencer a filial ativa
    E a configuracao fiscal deve registrar o ambiente "HOMOLOGACAO"

  Cenario: Bloquear configuracao fiscal de filial de outro tenant
    Dado que existe uma filial em outro tenant
    Quando o administrador tentar salvar a configuracao fiscal dessa filial
    Entao a resposta fiscal deve ter status HTTP 404
    E nenhuma configuracao fiscal deve ser criada no tenant atual
    E nenhuma configuracao fiscal deve ser criada no outro tenant

  Cenario: Registrar certificado A1 valido para a filial
    Quando o administrador enviar um certificado A1 valido para a filial ativa
    Entao a resposta fiscal deve ter status HTTP 201
    E o certificado fiscal deve pertencer ao tenant atual
    E o certificado fiscal deve pertencer a filial ativa
    E o conteudo do certificado deve ser armazenado protegido
    E a senha do certificado nao deve ser retornada na resposta
    E o certificado deve registrar a data de validade

  Cenario: Bloquear certificado A1 expirado
    Quando o administrador enviar um certificado A1 expirado para a filial ativa
    Entao a resposta fiscal deve ter status HTTP 400
    E a resposta fiscal deve informar "Certificado digital expirado"
    E nenhum certificado fiscal deve ficar ativo para a filial ativa

  Cenario: Rotacionar certificado A1 mantendo apenas um certificado ativo
    Dado que a filial ativa possui um certificado A1 ativo
    Quando o administrador enviar um novo certificado A1 valido para a filial ativa
    Entao a resposta fiscal deve ter status HTTP 201
    E o certificado anterior deve ficar inativo
    E o novo certificado deve ficar ativo

  Cenario: Classificar produto fiscalmente para a filial ativa
    Dado que existe um produto do tenant atual
    Quando o administrador salvar a classificacao fiscal do produto com:
      | campo | valor    |
      | ncm   | 61091000 |
      | cfop  | 5102     |
      | cest  |          |
    Entao a resposta fiscal deve ter status HTTP 201
    E a classificacao fiscal deve pertencer ao tenant atual
    E a classificacao fiscal deve pertencer a filial ativa
    E a classificacao fiscal deve pertencer ao produto informado

  Cenario: Bloquear classificacao fiscal de produto de outro tenant
    Dado que existe um produto em outro tenant
    Quando o administrador tentar salvar a classificacao fiscal desse produto
    Entao a resposta fiscal deve ter status HTTP 404
    E nenhuma classificacao fiscal deve ser criada no tenant atual
    E nenhuma classificacao fiscal deve ser criada no outro tenant

  Cenario: Emitir NFC-e usando adapter Java_NFe
    Dado que a filial ativa possui configuracao fiscal NFC-e valida
    E a filial ativa possui certificado A1 valido
    E existe uma venda finalizada e paga com itens fiscais validos
    Quando o usuario solicitar a emissao da NFC-e para a venda
    Entao a resposta fiscal deve ter status HTTP 201
    E o adapter Java_NFe deve montar o XML da NFC-e
    E o adapter Java_NFe deve assinar o XML com o certificado da filial ativa
    E o adapter Java_NFe deve enviar o documento para a SEFAZ no ambiente configurado
    E o documento fiscal deve registrar status "AUTORIZADO"
    E o documento fiscal deve armazenar XML autorizado

  Cenario: Converter falha tecnica do Java_NFe em falha de integracao fiscal
    Dado que a filial ativa possui configuracao fiscal NFC-e valida
    E a filial ativa possui certificado A1 valido
    E existe uma venda finalizada e paga com itens fiscais validos
    E o adapter Java_NFe retorna falha de comunicacao com a SEFAZ
    Quando o usuario solicitar a emissao da NFC-e para a venda
    Entao a resposta fiscal deve ter status HTTP 502
    E a resposta fiscal deve informar "Falha de comunicacao com a SEFAZ"
    E nenhum documento fiscal deve ser marcado como autorizado

  Cenario: Consultar status de NFC-e autorizada
    Dado que existe uma NFC-e autorizada no tenant e filial ativos
    Quando o usuario consultar o status da NFC-e
    Entao a resposta fiscal deve ter status HTTP 200
    E a resposta deve informar status "AUTORIZADO"
    E a resposta deve conter a chave de acesso
    E a resposta deve conter o protocolo de autorizacao

  Cenario: Bloquear consulta de NFC-e de outro tenant
    Dado que existe uma NFC-e autorizada em outro tenant
    Quando o usuario do tenant atual consultar essa NFC-e
    Entao a resposta fiscal deve ter status HTTP 404

  Cenario: Baixar DANFE NFC-e de documento autorizado
    Dado que existe uma NFC-e autorizada no tenant e filial ativos
    Quando o usuario solicitar o DANFE da NFC-e
    Entao a resposta fiscal deve ter status HTTP 200
    E a resposta deve retornar um PDF

  Cenario: Bloquear DANFE de documento rejeitado
    Dado que existe uma NFC-e rejeitada no tenant e filial ativos
    Quando o usuario solicitar o DANFE da NFC-e
    Entao a resposta fiscal deve ter status HTTP 409
    E a resposta fiscal deve informar "DANFE disponivel apenas para documento autorizado"

  Cenario: Emitir nota fiscal de devolucao para venda com NFC-e autorizada
    Dado que existe uma venda com NFC-e autorizada no tenant e filial ativos
    E a venda possui reembolso confirmado
    Quando o usuario solicitar a emissao da nota fiscal de devolucao
    Entao a resposta fiscal deve ter status HTTP 201
    E a nota de devolucao deve pertencer ao tenant atual
    E a nota de devolucao deve pertencer a filial ativa
    E a nota de devolucao deve referenciar a chave da NFC-e original
    E a nota de devolucao deve registrar status "AUTORIZADO"

  Cenario: Bloquear devolucao fiscal sem documento original autorizado
    Dado que existe uma venda sem NFC-e autorizada no tenant e filial ativos
    Quando o usuario solicitar a emissao da nota fiscal de devolucao
    Entao a resposta fiscal deve ter status HTTP 409
    E a resposta fiscal deve informar "Documento fiscal original autorizado e obrigatorio para devolucao"

  Cenario: Bloquear devolucao fiscal de venda sem reembolso confirmado
    Dado que existe uma venda com NFC-e autorizada no tenant e filial ativos
    E a venda nao possui reembolso confirmado
    Quando o usuario solicitar a emissao da nota fiscal de devolucao
    Entao a resposta fiscal deve ter status HTTP 409
    E a resposta fiscal deve informar "Reembolso confirmado e obrigatorio para nota de devolucao"
