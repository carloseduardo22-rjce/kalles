import type { ClientRequest, PersonType } from "@/features/admin/types";

const NON_DIGITS_REGEX = /\D/g;
const NON_ALPHANUMERIC_REGEX = /[^0-9A-Za-z]/g;
const BRAZILIAN_MOBILE_REGEX = /^[1-9]{2}9\d{8}$/;
const CNPJ_REGEX = /^[0-9A-Z]{12}\d{2}$/;
const CNPJ_FIRST_WEIGHTS = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
const CNPJ_SECOND_WEIGHTS = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];

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

export function alphanumericUpperCase(value?: string | null): string {
  return (value ?? "").replace(NON_ALPHANUMERIC_REGEX, "").toUpperCase();
}

export function formatCnpj(value?: string | null): string {
  const chars = alphanumericUpperCase(value).slice(0, 14);

  if (chars.length <= 2) return chars;
  if (chars.length <= 5) return `${chars.slice(0, 2)}.${chars.slice(2)}`;
  if (chars.length <= 8) {
    return `${chars.slice(0, 2)}.${chars.slice(2, 5)}.${chars.slice(5)}`;
  }
  if (chars.length <= 12) {
    return `${chars.slice(0, 2)}.${chars.slice(2, 5)}.${chars.slice(5, 8)}/${chars.slice(8)}`;
  }

  return `${chars.slice(0, 2)}.${chars.slice(2, 5)}.${chars.slice(5, 8)}/${chars.slice(8, 12)}-${chars.slice(12)}`;
}

export function formatDocument(personType: PersonType, document?: string | null): string {
  return personType === "JURIDICA" ? formatCnpj(document) : formatCpf(document);
}

function cnpjCheckDigit(base: string, weights: number[]): number {
  const sum = weights.reduce(
    (total, weight, index) => total + (base.charCodeAt(index) - 48) * weight,
    0,
  );
  const remainder = sum % 11;
  return remainder < 2 ? 0 : 11 - remainder;
}

export function isValidCnpj(value?: string | null): boolean {
  const cnpj = alphanumericUpperCase(value);

  if (!CNPJ_REGEX.test(cnpj) || /^(\d)\1{13}$/.test(cnpj)) {
    return false;
  }

  const first = cnpjCheckDigit(cnpj.slice(0, 12), CNPJ_FIRST_WEIGHTS);
  const second = cnpjCheckDigit(cnpj.slice(0, 13), CNPJ_SECOND_WEIGHTS);

  return first === Number(cnpj[12]) && second === Number(cnpj[13]);
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
  const shared = {
    personType: data.personType,
    name: data.name.trim(),
    codeCountry: cellphone ? "+55" : null,
    cellphone: cellphone || null,
    observations: trimToNull(data.observations),
  };

  if (data.personType === "JURIDICA") {
    return {
      ...shared,
      document: alphanumericUpperCase(data.document).slice(0, 14),
      tradeName: trimToNull(data.tradeName),
      stateRegistration: alphanumericUpperCase(data.stateRegistration) || null,
      birthDate: null,
      gender: null,
      rg: null,
      nameFather: null,
      nameMother: null,
    };
  }

  return {
    ...shared,
    document: onlyDigits(data.document).slice(0, 11),
    tradeName: null,
    stateRegistration: null,
    birthDate: trimToNull(data.birthDate),
    gender: trimToNull(data.gender),
    rg: trimToNull(data.rg),
    nameFather: trimToNull(data.nameFather),
    nameMother: trimToNull(data.nameMother),
  };
}
