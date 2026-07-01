# Investigacao SDD: emissor fiscal Mercado Pago

Data: 2026-04-30

## Fontes oficiais consultadas

- Mercado Pago Ajuda, "Quais notas fiscais posso emitir no Mercado Pago?"
- Mercado Pago Ajuda, "Onde emitir as notas fiscais das minhas vendas?"
- Mercado Pago Ajuda, "Por que o botao Emitir nota fiscal nao aparece na minha Point Smart?"
- Mercado Pago Ajuda, "Como habilito meu CNPJ para emitir notas fiscais?"
- Mercado Pago Ajuda, "Tenho o emissor de NF-e configurado, mas so posso emitir nota de devolucao. Por que?"
- Mercado Pago Ajuda, "O que e e como emitir uma nota de devolucao?"
- Mercado Pago Developers, "Mercado Pago Point - processamento de pagamentos"
- Mercado Pago Developers, "Configurar impressoes"

## Descobertas

1. O Mercado Pago oferece dois emissores na configuracao da conta: NFC-e e NF-e.
2. A NFC-e e indicada para vendas presenciais ao consumidor, inclusive emissao pela Point Smart ou pelo computador.
3. A NF-e no Mercado Pago aparece associada a vendas online e notas de devolucao, mas uma pagina oficial informa que o emissor NF-e esta disponivel atualmente apenas para nota de devolucao.
4. A emissao de NFC-e pela Point Smart exige emissor configurado, produtos cadastrados e dados fiscais dos produtos informados.
5. Vendas feitas fora da secao "Seus produtos" da Point Smart podem nao exibir o botao de emissao fiscal.
6. O Mercado Pago informa que o emissor fiscal opera somente em producao, nao como ambiente de testes fiscais.
7. Na documentacao Developers consultada, a API publica de Point cobre processamento de pagamento, notificacoes, reembolso/cancelamento e impressoes personalizadas. Nao foi identificada uma API publica documentada para emitir NFC-e/NF-e via endpoint.
8. A API de impressao da Point Smart permite impressoes complementares, mas isso nao equivale a emissao fiscal. No Kalles, imprimir DANFE/NFC-e no terminal deve continuar separado de autorizar documento fiscal.

## Impacto na arquitetura Kalles

O Mercado Pago nao deve substituir o emissor fiscal proprio do Kalles no dominio fiscal neste momento. O desenho correto e tratar Mercado Pago como:

- origem de venda/pagamento conciliavel;
- possivel emissor fiscal externo operado pela conta Mercado Pago;
- possivel canal de impressao/comprovante, quando suportado;
- fonte de eventos de pagamento/devolucao que podem disparar intencoes fiscais no Kalles.

O core fiscal Kalles deve continuar sendo o sistema de registro de documentos fiscais do ERP, com uma porta para provedores externos quando o documento for emitido fora do Kalles.

## Decisoes propostas

1. Manter o fluxo principal do MVP como emissao propria via dominio fiscal Kalles e Java_NFe.
2. Criar um conceito de `FiscalProvider` no dominio fiscal em uma proxima fatia: `KALLES_SEFAZ`, `MERCADO_PAGO`.
3. Registrar documentos emitidos fora do Kalles como documento fiscal externo, com chave de acesso, protocolo, venda e provider.
4. Nao chamar impressao Point Smart de emissao fiscal. Impressao e adaptador de saida; autorizacao fiscal e porta SEFAZ/provider fiscal.
5. Para Mercado Pago, iniciar com rastreio e conciliacao: quando pagamento MP for aprovado, marcar venda como elegivel para NFC-e ou registrar pendencia de emissao externa.
6. Adicionar UI para mostrar se a venda foi emitida pelo Kalles ou pelo Mercado Pago.
7. Investigar com conta/autenticacao real se ha endpoint privado/partner para emissao fiscal. Sem documentacao publica, nao implementar chamada HTTP especulativa.

## Perguntas em aberto

1. A conta Kalles/Mercado Pago tera emissor fiscal habilitado em producao?
2. O cliente quer que o Kalles emita sempre pelo proprio fiscal ou aceite emissao feita manualmente pelo Mercado Pago?
3. Devemos bloquear emissao propria no Kalles se a venda ja teve NFC-e emitida no Mercado Pago?
4. Como obter a chave de acesso/protocolo de uma nota emitida pelo Mercado Pago: exportacao manual, tela, webhook, relatorio ou API partner?
5. Para devolucao, a origem da devolucao sera reembolso Mercado Pago, cancelamento de venda no Kalles ou ambos?
