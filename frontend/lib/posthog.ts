declare global {
  interface Window {
    posthog?: {
      capture: (eventName: string, properties?: Record<string, unknown>) => void;
    };
  }
}

export function capturePosthogEvent(
  eventName: string,
  properties: Record<string, unknown>,
) {
  if (typeof window === "undefined") {
    return;
  }

  window.posthog?.capture(eventName, properties);
  window.dispatchEvent(
    new CustomEvent("kalles:onboarding-event", {
      detail: {
        eventName,
        properties,
      },
    }),
  );
}
