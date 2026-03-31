export const steps = [
    {
        id: 1,
        title: "Conectar a conta do mercado pago à maquininha",
        content:
            `Antes de tudo para podermos começar a associação do terminal à conta do mercado pago, 
        é imprenscindivel que você tenha o aplicativo do mercado pago instalado no seu celular e à loja e caixas que você deseja integrar a maquininha já configurados.
        
        Comece ligando o Terminal Point (Maquininha). Você verá na tela a mensagem "Inicie sessão neste dispositivo com sua conta do Mercado Pago". Ali você deverá escolher entre as opções abaixo.
        
        • Sou responsável pelo negócio: selecione esta opção se você é o dono da loja física.
        • Sou um colaborador: escolha esta opção se sua conta foi indicada como conta de colaborador pelo proprietário da loja.

        Uma vez selecionada a opção que corresponda, aparecerá um QR code na tela do terminal (Maquininha) que você deverá escanear com a aplicação móvel do Mercado Pago logada na sua conta da respectiva opção escolhida.

        Após alguns segundos, o terminal poderá solicitar algumas configurações adicionais para a loja. Siga as instruções exibidas na tela para concluir todas as etapas.
        `,
    },
    {
        id: 2,
        title: "Selecionando loja e caixa",
        content: `
        Após a conclusão da etapa anterior, o terminal estará associado à sua conta do Mercado Pago. 
        Agora é necessário configurar qual loja e caixa estarão associados ao terminal.
        
        O terminal solicitará que você selecione a loja e o caixa aos quais quer associá-lo, e confirme o endereço da loja previamente criada com sua conta do Mercado Pago. Ao finalizar, pressione o botão Confirmar.

        Se, por alguma razão, você tem mais de uma loja criada, atente-se de selecionar corretamente aquela que quer integrar ao seu Terminal Point (Maquininha).
        
        Por último, o terminal solicitará que você insira uma senha que garantirá seu uso com mais segurança.

        Uma vez finalizado este processo, a tela exibirá a mensagem "Pronto! Já pode cobrar com seu Point", e você terá finalizado a associação do seu terminal à conta do Mercado Pago desejada, e à loja e caixa criados.
        `,
    },
    {
        id: 3,
        title: "Configurando o Kalles para usar a maquininha",
        content: `
        Após a conclusão da etapa anterior, o terminal agora está associado à sua conta do Mercado Pago, loja e caixa que foram também previamente criados no nosso sistema Kalles. 
        
        Isso nos permiti começar a integração de pagamentos via maquininha ao nosso sistema Kalles. 
        
        Para começar a integração de pagamentos via maquininha integrado ao nosso sistema kalles para permitir você receber pagamentos via cartão clique em "Começar configuração" abaixo. 
        
        A configuração poderá demorar alguns segundos a minutos dependendo de como está o sistema do mercado pago.
        `,
    },
];