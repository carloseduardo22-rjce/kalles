import type { ClientRequest } from "@/features/admin/types";

const NON_DIGITS_REGEX = /\D/g;
const BRAZILIAN_MOBILE_REGEX = /^[1-9]{2}9\d{8}$/;

function trimToNull(value?: string | null): string | null {
  const normalized = value?.trim();
  return normalized ? normalized : null;
}

export function onlyDigits(value?: string | null): string {
  return (value ?? "").replace(NON_DIGITS_REGEX, "");
}

export function formatCpf(value?: string | null): string {
  const digits = onlyDigits(value).slice(0, 11);

  if (digits.length <= 3) return digits;
  if (digits.length <= 6) return `${digits.slice(0, 3)}.${digits.slice(3)}`;
  if (digits.length <= 9) {
    return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6)}`;
  }

  return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`;
}

export function formatBrazilianCellphone(value?: string | null): string {
  const digits = onlyDigits(value).slice(0, 11);

  if (digits.length <= 2) return digits;
  if (digits.length <= 7) return `(${digits.slice(0, 2)}) ${digits.slice(2)}`;

  return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
}

export function isValidCpf(value?: string | null): boolean {
  const cpf = onlyDigits(value);

  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
    return false;
  }

  let sum = 0;
  for (let index = 0; index < 9; index += 1) {
    sum += Number(cpf[index]) * (10 - index);
  }

  let remainder = (sum * 10) % 11;
  if (remainder === 10) remainder = 0;
  if (remainder !== Number(cpf[9])) return false;

  sum = 0;
  for (let index = 0; index < 10; index += 1) {
    sum += Number(cpf[index]) * (11 - index);
  }

  remainder = (sum * 10) % 11;
  if (remainder === 10) remainder = 0;

  return remainder === Number(cpf[10]);
}

export function isValidBrazilianCellphone(value?: string | null): boolean {
  const digits = onlyDigits(value);
  return BRAZILIAN_MOBILE_REGEX.test(digits);
}

export function normalizeClientRequest(data: ClientRequest): ClientRequest {
  const cellphone = onlyDigits(data.cellphone).slice(0, 11);

  return {
    ...data,
    name: data.name.trim(),
    birthDate: trimToNull(data.birthDate),
    gender: trimToNull(data.gender),
    cpf: onlyDigits(data.cpf).slice(0, 11),
    codeCountry: cellphone ? "+55" : null,
    cellphone: cellphone || null,
    rg: trimToNull(data.rg),
    nameFather: trimToNull(data.nameFather),
    nameMother: trimToNull(data.nameMother),
    observations: trimToNull(data.observations),
  };
}
