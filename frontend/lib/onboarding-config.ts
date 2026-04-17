export type OnboardingStepConfig = {
  selector: string;
  title: string;
  description: string;
  side?: "top" | "right" | "bottom" | "left";
  align?: "start" | "center" | "end";
};

export type OnboardingConfig = {
  id: string;
  version: number;
  title: string;
  description: string;
  steps: OnboardingStepConfig[];
};

const configs: Array<{
  matcher: (pathname: string) => boolean;
  config: OnboardingConfig;
}> = [
  {
    matcher: (pathname) => pathname === "/caixas",
    config: {
      id: "cash-registers",
      version: 1,
      title: "Gerenciamento de Caixas",
      description:
        "Esta tela mostra quais caixas podem operar, quais já estão em sessão e onde iniciar um novo turno com segurança.",
      steps: [
        {
          selector: "[data-onboarding='caixas-page']",
          title: "O que esta tela resolve",
          description:
            "Use esta página para decidir em qual caixa abrir operação. Aqui você valida disponibilidade, status de sessão e preparo do ambiente antes de ir para o PDV.",
          side: "bottom",
          align: "start",
        },
        {
          selector: "[data-onboarding='sidebar']",
          title: "Navegação operacional",
          description:
            "A barra lateral organiza a jornada: primeiro caixas, depois terminal PDV e por fim relatórios. O cliente aprende a sequência natural de uso por aqui.",
          side: "right",
        },
        {
          selector: "[data-onboarding='caixas-summary']",
          title: "Leitura rápida da operação",
          description:
            "Os cards do topo resumem o parque de caixas. Eles respondem rapidamente quantos existem, quantos estão abertos e se há capacidade para iniciar novas sessões.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='caixas-create-button']",
          title: "Cadastrar um novo caixa",
          description:
            "Use este botão quando a empresa ainda precisa criar um caixa físico novo no sistema. Ele é diferente de abrir sessão: primeiro você cadastra o caixa, depois começa a operação nele.",
          side: "left",
        },
        {
          selector: "[data-onboarding='caixas-content']",
          title: "Abrir sessão do jeito certo",
          description:
            "Cada card representa um caixa físico. Quando o pagamento estiver configurado e não houver sessão ativa, o botão permite iniciar o turno com operador e valor inicial.",
          side: "top",
        },
      ],
    },
  },
  {
    matcher: (pathname) => pathname === "/pdv",
    config: {
      id: "pdv-terminal",
      version: 1,
      title: "Terminal PDV",
      description:
        "O PDV concentra atendimento, montagem da venda, fidelidade e fechamento financeiro da operação em uma única tela.",
      steps: [
        {
          selector: "[data-onboarding='pdv-page']",
          title: "Visão geral do terminal",
          description:
            "Esta é a tela de atendimento do caixa. A lógica do operador aqui é simples: identificar o caixa certo, adicionar itens, confirmar total e concluir pagamento.",
          side: "bottom",
          align: "start",
        },
        {
          selector: "[data-onboarding='pdv-header']",
          title: "Contexto da sessão",
          description:
            "No cabeçalho você confere em qual caixa está, qual operador está responsável e o estado atual da venda. Esse bloco evita erros de operação.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='pdv-products']",
          title: "Montagem da venda",
          description:
            "A coluna central é onde os produtos entram na compra. A busca adiciona itens, a lista mostra quantidades e dali saem remoção, desconto e ajustes.",
          side: "right",
        },
        {
          selector: "[data-onboarding='pdv-totals']",
          title: "Leitura financeira imediata",
          description:
            "Aqui o operador enxerga subtotal, descontos e total final. Isso transforma a lista de itens em uma decisão de cobrança objetiva antes do pagamento.",
          side: "right",
        },
        {
          selector: "[data-onboarding='pdv-payment']",
          title: "Fidelidade e pagamento",
          description:
            "Na coluna da direita o cliente identifica o consumidor, aplica benefícios de fidelidade e registra os meios de pagamento até concluir a venda.",
          side: "left",
        },
        {
          selector: "[data-onboarding='pdv-shortcuts']",
          title: "Atalhos para ganhar velocidade",
          description:
            "Os atalhos aceleram operações repetitivas: consulta de produto, decremento e incremento do último item. Eles reduzem cliques e mantêm o caixa fluido.",
          side: "top",
        },
      ],
    },
  },
  {
    matcher: (pathname) => pathname === "/relatorios",
    config: {
      id: "reports",
      version: 1,
      title: "Relatórios",
      description:
        "Esta área transforma sessões e pagamentos em leitura gerencial: desempenho, histórico, comparativos e saúde financeira da operação.",
      steps: [
        {
          selector: "[data-onboarding='reports-page']",
          title: "Para que servem os relatórios",
          description:
            "A página responde o que aconteceu no caixa. Em vez de agir em uma venda, aqui o usuário interpreta dados e identifica tendências ou problemas.",
          side: "bottom",
          align: "start",
        },
        {
          selector: "[data-onboarding='sidebar']",
          title: "Leitura por contexto",
          description:
            "Na lateral, os atalhos já separam resumo, pagamentos e histórico. Isso ensina que cada visão responde uma pergunta operacional diferente.",
          side: "right",
        },
        {
          selector: "[data-onboarding='reports-filters']",
          title: "Escolha do período",
          description:
            "Os filtros mudam completamente a leitura: sessões ativas mostram o agora, data específica fecha um dia e intervalo ajuda em auditoria ou análise semanal.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='reports-content']",
          title: "Interpretação dos resultados",
          description:
            "Os cards, gráficos e listas mostram total vendido, mix de pagamento e desempenho por sessão. O foco aqui é entender comportamento, não registrar operações.",
          side: "top",
        },
      ],
    },
  },
  {
    matcher: (pathname) => pathname === "/produtos",
    config: {
      id: "catalog",
      version: 1,
      title: "Catálogo de Produtos",
      description:
        "O catálogo é a consulta rápida do mix da empresa para atendimento, conferências e validação de códigos antes da venda.",
      steps: [
        {
          selector: "[data-onboarding='catalog-page']",
          title: "O papel do catálogo",
          description:
            "Esta tela é uma consulta operacional. Ela ajuda a localizar produtos, confirmar códigos e revisar estoque ou preço sem entrar no cadastro administrativo.",
          side: "bottom",
          align: "start",
        },
        {
          selector: "[data-onboarding='catalog-header']",
          title: "Visão de disponibilidade",
          description:
            "O cabeçalho resume quantos produtos existem e oferece atualização manual. Serve para validar se o catálogo refletiu as últimas mudanças do sistema.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='catalog-filters']",
          title: "Busca de atendimento",
          description:
            "Pesquise por nome, código interno ou código de barras. O cliente aprende que esta é a forma mais rápida de localizar um item durante a operação.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='catalog-content']",
          title: "Como ler a tabela",
          description:
            "A grade combina identificação, descrição, estoque, localização e preço. Isso permite decidir rapidamente se o item existe, onde está e quanto custa.",
          side: "top",
        },
      ],
    },
  },
  {
    matcher: (pathname) => pathname === "/suporte",
    config: {
      id: "support",
      version: 1,
      title: "Suporte",
      description:
        "O módulo de suporte organiza a comunicação com o cliente por chamado, status e histórico de interações.",
      steps: [
        {
          selector: "[data-onboarding='support-page']",
          title: "Para que serve o suporte",
          description:
            "Aqui o usuário acompanha problemas e conversas em andamento. A unidade de trabalho não é a venda, e sim o chamado com contexto, prioridade e SLA.",
          side: "bottom",
          align: "start",
        },
        {
          selector: "[data-onboarding='support-header']",
          title: "Ações principais",
          description:
            "No topo você atualiza a fila e, quando permitido, abre um novo chamado. O cabeçalho deixa claro se a tela está em modo cliente ou administrativo.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='support-filters']",
          title: "Organização da fila",
          description:
            "Busca e filtro de status ajudam a separar o urgente do resolvido. Essa etapa ensina o usuário a reduzir ruído antes de abrir um ticket.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='support-content']",
          title: "Leitura dos chamados",
          description:
            "A lista mostra título, categoria, status, prioridade e responsável. O objetivo é bater o olho e entender qual ticket precisa de ação agora.",
          side: "top",
        },
      ],
    },
  },
  {
    matcher: (pathname) => pathname === "/admin/lojas",
    config: {
      id: "admin-stores",
      version: 1,
      title: "Lojas",
      description:
        "Esta tela define a estrutura de filiais do negócio. Sem ela, o restante da operação multiempresa fica incompleto.",
      steps: [
        {
          selector: "[data-onboarding='stores-page']",
          title: "Base da estrutura operacional",
          description:
            "Aqui se cadastram as filiais que o sistema vai administrar. Loja é a unidade que contextualiza configurações, integrações e seleção da filial ativa.",
          side: "bottom",
          align: "start",
        },
        {
          selector: "[data-onboarding='stores-header']",
          title: "Leitura da filial ativa",
          description:
            "O topo informa em qual filial administrativa você está trabalhando. Isso é importante porque várias telas abaixo passam a depender dessa seleção.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='stores-form']",
          title: "Cadastro de uma nova filial",
          description:
            "O formulário cria uma nova loja com nome e endereço. O cliente deve entender que este passo prepara o ambiente para caixas, estoque e integrações.",
          side: "right",
        },
        {
          selector: "[data-onboarding='stores-content']",
          title: "Conferência da estrutura criada",
          description:
            "A coluna ao lado lista as lojas já cadastradas. Ela funciona como validação visual de que a filial entrou corretamente no ambiente.",
          side: "left",
        },
      ],
    },
  },
  {
    matcher: (pathname) => pathname === "/admin/produtos",
    config: {
      id: "admin-products",
      version: 1,
      title: "Cadastro de Produtos",
      description:
        "Nesta tela o usuário administra o cadastro mestre dos produtos vendidos no PDV e controlados no estoque.",
      steps: [
        {
          selector: "[data-onboarding='admin-products-page']",
          title: "Objetivo da tela",
          description:
            "O cadastro de produtos é o ponto de verdade do item. É aqui que se definem nome, códigos, preço, custo e status de disponibilidade.",
          side: "bottom",
          align: "start",
        },
        {
          selector: "[data-onboarding='admin-products-header']",
          title: "Ações de cadastro",
          description:
            "O cabeçalho mostra volume de produtos, atualização e criação de novos itens. É o lugar para iniciar manutenção do catálogo administrativo.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='admin-products-filters']",
          title: "Busca inteligente",
          description:
            "A pesquisa permite encontrar um produto pelo nome, código interno ou código de barras. Isso ensina o usuário a localizar rapidamente qualquer item.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='admin-products-content']",
          title: "Como interpretar a grade",
          description:
            "A tabela junta identificação, estoque, local principal, preço de venda, custo e status. Ela serve tanto para consulta quanto para decisão de manutenção.",
          side: "top",
        },
      ],
    },
  },
  {
    matcher: (pathname) => pathname === "/admin/clientes",
    config: {
      id: "admin-clients",
      version: 1,
      title: "Clientes",
      description:
        "Esta tela centraliza o cadastro de clientes para atendimento, fidelidade e histórico comercial.",
      steps: [
        {
          selector: "[data-onboarding='clients-page']",
          title: "Papel do cadastro de clientes",
          description:
            "Aqui ficam os dados de identificação do cliente, usados no PDV, na fidelidade e no relacionamento da operação com a base de consumidores.",
          side: "bottom",
          align: "start",
        },
        {
          selector: "[data-onboarding='clients-header']",
          title: "Criação e atualização",
          description:
            "No topo o usuário atualiza a lista e inicia um novo cadastro. Essa é a entrada principal para ampliar ou manter a base de clientes.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='clients-filters']",
          title: "Busca por identificação",
          description:
            "A busca localiza rapidamente pessoas por nome, CPF ou celular. Isso reduz duplicidade e acelera o atendimento no momento do cadastro ou consulta.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='clients-content']",
          title: "Leitura do relacionamento",
          description:
            "A tabela mostra quem é o cliente e como ele se conecta ao programa de fidelidade. Essa visão ajuda a decidir contato, manutenção e elegibilidade.",
          side: "top",
        },
      ],
    },
  },
  {
    matcher: (pathname) => pathname === "/admin/estoque",
    config: {
      id: "admin-stock",
      version: 1,
      title: "Gestão de Estoque",
      description:
        "A tela conecta produto, depósito e localização física para manter saldo e custo confiáveis.",
      steps: [
        {
          selector: "[data-onboarding='stock-page']",
          title: "Para que serve esta área",
          description:
            "Esta é a tela de controle físico do estoque. Ela responde onde o produto está, quanto existe e qual custo alimenta os relatórios financeiros.",
          side: "bottom",
          align: "start",
        },
        {
          selector: "[data-onboarding='stock-products']",
          title: "Escolha do produto",
          description:
            "A coluna da esquerda serve para encontrar o item que você quer auditar ou abastecer. O fluxo correto sempre começa por selecionar o produto.",
          side: "right",
        },
        {
          selector: "[data-onboarding='stock-header']",
          title: "Contexto e entrada de mercadoria",
          description:
            "O cabeçalho mostra qual produto está em foco e oferece o atalho para registrar entrada. Essa ação impacta saldo e custo médio do item.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='stock-content']",
          title: "Leitura por depósito e localização",
          description:
            "A tabela detalha onde o estoque está distribuído. Ela ajuda em contagem, reposição e investigação de divergências por local físico.",
          side: "top",
        },
      ],
    },
  },
  {
    matcher: (pathname) => pathname === "/admin/pagamentos",
    config: {
      id: "admin-payments",
      version: 1,
      title: "Integração de Pagamentos",
      description:
        "Esta área prepara a empresa para receber pagamentos e conecta loja, caixa e adquirente ao fluxo do PDV.",
      steps: [
        {
          selector: "[data-onboarding='payments-page']",
          title: "Visão geral da integração",
          description:
            "Aqui o usuário aprende que pagamento configurado não é só conta conectada: envolve provedor, loja vinculada e terminal associado a um caixa real.",
          side: "bottom",
          align: "start",
        },
        {
          selector: "[data-onboarding='payments-provider-selector']",
          title: "Escolha do provedor",
          description:
            "Os cards de provedor explicam qual integração está ativa e qual modelo operacional cada opção oferece. Isso orienta a decisão técnica e de negócio.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='payments-tabs']",
          title: "Dois modos de trabalho",
          description:
            "A aba de listagem serve para auditar o que já está conectado. A aba de criação conduz o onboarding de conta, estabelecimento e POS.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='payments-content']",
          title: "Como concluir o setup",
          description:
            "O restante da tela mostra se já existem lojas e terminais vinculados ou, quando necessário, conduz a configuração passo a passo até o caixa ficar apto para cobrar.",
          side: "top",
        },
      ],
    },
  },
  {
    matcher: (pathname) => pathname === "/admin/configuracoes",
    config: {
      id: "admin-settings",
      version: 1,
      title: "Configurações",
      description:
        "A tela de configurações personaliza a identidade visual da empresa dentro do ERP e do terminal.",
      steps: [
        {
          selector: "[data-onboarding='settings-page']",
          title: "O que o usuário ajusta aqui",
          description:
            "Esta página não altera operação ou cadastro. Ela cuida da identidade visual que será percebida pelos usuários do sistema e do PDV.",
          side: "bottom",
          align: "start",
        },
        {
          selector: "[data-onboarding='settings-header']",
          title: "Contexto da tela",
          description:
            "O cabeçalho deixa claro que esta área é de personalização. Isso ajuda o cliente a distinguir configuração visual de configurações operacionais.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='settings-theme']",
          title: "Tema e paleta",
          description:
            "Aqui o cliente escolhe modo claro ou escuro e a paleta predominante do ERP. É o lugar certo para alinhar aparência com a marca da empresa.",
          side: "bottom",
        },
        {
          selector: "[data-onboarding='settings-logo']",
          title: "Logo da empresa",
          description:
            "A logo aparece no terminal e em contextos visuais do sistema. O usuário aprende que esta configuração reforça reconhecimento de marca no uso diário.",
          side: "top",
        },
      ],
    },
  },
];

export function resolveOnboardingConfig(pathname: string) {
  return configs.find((entry) => entry.matcher(pathname))?.config ?? null;
}
