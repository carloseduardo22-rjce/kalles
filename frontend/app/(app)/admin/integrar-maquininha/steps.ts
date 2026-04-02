export const steps = [
  {
    id: 1,
    title: "Conectar a conta do Mercado Pago à maquininha",
    content: `Para iniciarmos a configuração da sua maquininha (Terminal Point), é fundamental ter o aplicativo do Mercado Pago instalado em seu celular. Certifique-se também de já ter criado sua loja e caixa diretamente no sistema Kalles.
        
        1. Ligue o seu Terminal Point (Maquininha).
        2. Na tela, você verá a mensagem: "Inicie sessão neste dispositivo com sua conta do Mercado Pago". Selecione uma das opções:
           • Sou responsável pelo negócio: se você for o dono do estabelecimento.
           • Sou um colaborador: se sua conta for vinculada como colaborador pelo proprietário.
        
        3. Um QR Code aparecerá na tela da maquininha. Abra o aplicativo Mercado Pago no seu celular (logado na conta escolhida) e escaneie o código.
        
        Após alguns instantes, o terminal poderá solicitar configurações adicionais. Basta seguir as instruções exibidas na tela da maquininha para concluir esta etapa.`,
  },
  {
    id: 2,
    title: "Selecionar a loja e o caixa na maquininha",
    content: `Com a maquininha vinculada à sua conta, o próximo passo é indicar em qual loja e caixa físicos ela irá operar.
        
        1. A maquininha pedirá que você selecione a loja e o caixa.
        2. Confirme o endereço e as informações do seu negócio.
        
        Atenção: Caso você possua mais de uma loja ou caixa, certifique-se de selecionar exatamente a mesma loja e o mesmo caixa que você configurou no Kalles.
        
        3. O terminal solicitará a criação de uma senha de segurança para uso diário.
        4. Ao finalizar, a maquininha exibirá a mensagem: "Pronto! Já pode cobrar com seu Point".`,
  },
  {
    id: 3,
    title: "Ativar integração com o sistema Kalles",
    content: `Tudo pronto do lado do Mercado Pago! Sua maquininha já está associada à sua conta, loja e caixa físicos.
        
        Agora, precisamos configurar o Kalles para se comunicar automaticamente com a sua maquininha, permitindo que suas vendas em cartão sejam enviadas direto do sistema para ela (modo PDV integrado).
        
        Para identificarmos a sua maquininha, por favor, insira o Número de Série (serial) que se encontra na etiqueta traseira física do próprio terminal e clique em "Começar configuração".
        
        Nós faremos o restante do trabalho, incluindo a ativação do modo PDV no terminal e a liberação das funções de pagamento, cancelamento e reembolso pelas transações no Kalles.`,
  },
];
