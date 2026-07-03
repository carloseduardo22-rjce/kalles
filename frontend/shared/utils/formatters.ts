export function formatCurrency(value: number): string {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
}

export function formatDate(dateString: string): string {
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(dateString));
}

export function formatPaymentMethod(method: string): string {
  const labels: Record<string, string> = {
    CASH: "Dinheiro",
    PIX: "Pix",
    CREDIT_CARD: "Cartão de Crédito",
    DEBIT_CARD: "Cartão de Débito",
    OTHER: "Outros",
  };
  return labels[method] ?? method;
}

export function formatSaleState(state: string): string {
  const labels: Record<string, string> = {
    OPEN: "Em aberto",
    ON_HOLD: "Em espera",
    PAYMENT_IN_PROGRESS: "Pagamento em andamento",
    PAID: "Pago",
    COMPLETED: "Concluído",
    CANCELED: "Cancelado",
  };
  return labels[state] ?? state;
}
