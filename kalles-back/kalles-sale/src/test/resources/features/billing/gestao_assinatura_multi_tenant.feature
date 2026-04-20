# language: pt
@billing @multi_tenant

Funcionalidade: Gestao de assinatura multi-tenant em billing
  Como admin autenticado ou integrador Stripe
  Quero operar checkout, portal e webhook de forma segura por tenant
  Para impedir cruzamento de assinaturas entre contas

  Cenario: Checkout session criada para o tenant autenticado
    Dado um admin autenticado para billing
    Quando ele solicitar uma checkout session de billing
    Entao a resposta de billing deve ter status HTTP 200
    E a checkout session deve ser criada para o tenant atual

  Cenario: Portal session falha quando o tenant nao possui assinatura
    Dado um admin autenticado para billing
    Quando ele solicitar uma portal session de billing
    Entao a resposta de billing deve ter status HTTP 409
    E a resposta deve informar que nao existe assinatura Stripe

  Cenario: Webhook valido persiste assinatura do tenant correto
    Dado um webhook Stripe valido para o tenant atual
    Quando a Stripe enviar o webhook de billing
    Entao a resposta de billing deve ter status HTTP 200
    E a assinatura do tenant deve ser persistida pelo webhook

  Cenario: Webhook sem contexto suficiente falha sem persistir assinatura
    Dado um webhook Stripe sem dados suficientes para resolver o tenant
    Quando a Stripe enviar o webhook de billing
    Entao a resposta de billing deve ter status HTTP 409
    E a resposta deve informar que o tenant do webhook nao pode ser resolvido
