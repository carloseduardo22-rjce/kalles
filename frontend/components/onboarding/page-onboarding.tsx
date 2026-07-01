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
  const completionCommittedRef = useRef(false);
  const placement =
    config?.helpButtonPlacement ?? (pathname === "/pdv" ? "bottom-left" : "top-right");

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

    const activeConfig = config;
    const steps = activeConfig.steps
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
    completionCommittedRef.current = false;

    function markCompleted(stepCount: number) {
      if (completionCommittedRef.current) {
        return;
      }

      window.localStorage.setItem(
        buildStorageKey(activeConfig.id, activeConfig.version),
        "true",
      );
      setIsCompleted(true);
      completionCommittedRef.current = true;
      capturePosthogEvent("onboarding_completed", {
        screen: activeConfig.id,
        title: activeConfig.title,
        stepCount,
      });
    }

    capturePosthogEvent("onboarding_started", {
      screen: activeConfig.id,
      title: activeConfig.title,
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
      onNextClick: (_element: unknown, _step: unknown, context: any) => {
        const activeIndex = context?.state?.activeIndex ?? 0;
        if (activeIndex >= steps.length - 1) {
          markCompleted(steps.length);
          context?.driver?.destroy();
          return;
        }

        context?.driver?.moveNext();
      },
      onCloseClick: () => {
        closedEarly = true;
      },
      onDestroyed: () => {
        if (!completionCommittedRef.current && visitedLastStep && !closedEarly) {
          markCompleted(steps.length);
          return;
        }

        if (completionCommittedRef.current) {
          return;
        }

        capturePosthogEvent("onboarding_dismissed", {
          screen: activeConfig.id,
          title: activeConfig.title,
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

  const containerClass =
    placement === "bottom-left"
      ? "pointer-events-none fixed bottom-4 left-4 z-[80]"
      : "pointer-events-none fixed right-4 top-20 z-[80]";

  const label = isCompleted ? "Rever guia da tela" : "Iniciar guia da tela";

  return (
    <div className={containerClass}>
      <Button
        type="button"
        variant="secondary"
        className="pointer-events-auto h-10 rounded-full px-3 shadow-lg"
        data-onboarding="help-button"
        onClick={() => openTour("manual")}
        title={label}
        aria-label={label}
      >
        <HelpCircle className="h-4 w-4 sm:mr-2" />
        <span className="hidden sm:inline">{label}</span>
      </Button>
    </div>
  );
}
