const LOGO_KEY = "kalles:company-logo";

export const companySettingsService = {
  getLogo: (): string | null => {
    if (typeof window === "undefined") return null;
    return localStorage.getItem(LOGO_KEY);
  },

  setLogo: (url: string): void => {
    localStorage.setItem(LOGO_KEY, url);
  },

  removeLogo: (): void => {
    localStorage.removeItem(LOGO_KEY);
  },
};

const OPERATOR_PHOTO_PREFIX = "kalles:operator-photo:";

export const operatorPhotoService = {
  getPhoto: (operatorId: string): string | null => {
    if (typeof window === "undefined") return null;
    return localStorage.getItem(`${OPERATOR_PHOTO_PREFIX}${operatorId}`);
  },

  setPhoto: (operatorId: string, url: string): void => {
    localStorage.setItem(`${OPERATOR_PHOTO_PREFIX}${operatorId}`, url);
  },

  removePhoto: (operatorId: string): void => {
    localStorage.removeItem(`${OPERATOR_PHOTO_PREFIX}${operatorId}`);
  },
};
