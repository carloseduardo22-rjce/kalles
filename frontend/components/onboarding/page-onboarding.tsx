"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { usePathname } from "next/navigation";
import { HelpCircle } from "lucide-react";
import { driver } from "driver.js";
import "driver.js/dist/driver.css";
import { Button } from "@/components/ui/button";
import { resolveOnboardingConfig } from "@/lib/onboarding-config";
import { capturePosthogEvent } from "@/lib/posthog";

function buildStorageKey(id: string, version: number) {
  return `kalles:onboarding:${id}:v${version}:completed`;
}

export function PageOnboarding() {
  const pathname = usePathname();
  const config = useMemo(
    () => (pathname ? resolveOnboardingConfig(pathname) : null),
    [pathname],
  );
  const [isCompleted, setIsCompleted] = useState(true);
  const autoStartedRef = useRef(false);
  const driverRef = useRef<ReturnType<typeof driver> | null>(null);

  useEffect(() => {
    if (!config || typeof window === "undefined") {
      setIsCompleted(true);
      return;
    }

    const completed = window.localStorage.getItem(
      buildStorageKey(config.id, config.version),
    );
    setIsCompleted(completed === "true");
    autoStartedRef.current = false;
  }, [config]);

  useEffect(() => {
    if (!config || isCompleted || autoStartedRef.current) {
      return;
    }

    const timer = window.setTimeout(() => {
      autoStartedRef.current = true;
      openTour("auto");
    }, 900);

    return () => window.clearTimeout(timer);
  }, [config, isCompleted]);

  useEffect(() => {
    return () => {
      driverRef.current?.destroy();
    };
  }, []);

  function openTour(trigger: "auto" | "manual") {
    if (!config || typeof window === "undefined") {
      return;
    }

    const steps = config.steps
      .filter((step) => document.querySelector(step.selector))
      .map((step) => ({
        element: step.selector,
        popover: {
          title: step.title,
          description: step.description,
          side: step.side ?? "bottom",
          align: step.align ?? "center",
        },
      }));

    if (steps.length === 0) {
      return;
    }

    let visitedLastStep = false;
    let closedEarly = false;

    capturePosthogEvent("onboarding_started", {
      screen: config.id,
      title: config.title,
      trigger,
      stepCount: steps.length,
    });

    driverRef.current?.destroy();
    driverRef.current = driver({
      showProgress: true,
      animate: true,
      allowClose: true,
      overlayOpacity: 0.55,
      stagePadding: 10,
      nextBtnText: "Próximo",
      prevBtnText: "Voltar",
      doneBtnText: "Concluir",
      onHighlighted: (...args: any[]) => {
        const activeIndex = args[2]?.state?.activeIndex ?? 0;
        visitedLastStep = activeIndex === steps.length - 1;
      },
      onCloseClick: () => {
        closedEarly = true;
      },
      onDestroyed: () => {
        if (visitedLastStep && !closedEarly) {
          window.localStorage.setItem(
            buildStorageKey(config.id, config.version),
            "true",
          );
          setIsCompleted(true);
          capturePosthogEvent("onboarding_completed", {
            screen: config.id,
            title: config.title,
            stepCount: steps.length,
          });
          return;
        }

        capturePosthogEvent("onboarding_dismissed", {
          screen: config.id,
          title: config.title,
          stepCount: steps.length,
          trigger,
        });
      },
      steps,
    });

    driverRef.current.drive();
  }

  if (!config) {
    return null;
  }

  return (
    <div className="pointer-events-none fixed bottom-4 right-4 z-[80]">
      <Button
        type="button"
        variant="secondary"
        className="pointer-events-auto shadow-lg"
        data-onboarding="help-button"
        onClick={() => openTour("manual")}
      >
        <HelpCircle className="mr-2 h-4 w-4" />
        {isCompleted ? "Rever guia da tela" : "Iniciar guia da tela"}
      </Button>
    </div>
  );
}
