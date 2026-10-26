"use client";

import { useState } from "react";
import { LogOut } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { authService } from "@/shared/services/auth.service";

interface LogoutButtonProps {
  warning?: string;
  className?: string;
}

export function LogoutButton({ warning, className }: LogoutButtonProps) {
  const [confirming, setConfirming] = useState(false);
  const [leaving, setLeaving] = useState(false);

  async function logout() {
    setLeaving(true);
    await authService.logout();
  }

  return (
    <>
      <Button
        variant="ghost"
        size="sm"
        className={className}
        disabled={leaving}
        onClick={() => (warning ? setConfirming(true) : logout())}
      >
        <LogOut className="h-4 w-4" />
        Sair
      </Button>

      {warning && (
        <AlertDialog open={confirming} onOpenChange={setConfirming}>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Sair da conta?</AlertDialogTitle>
              <AlertDialogDescription>{warning}</AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel disabled={leaving}>Voltar</AlertDialogCancel>
              <AlertDialogAction disabled={leaving} onClick={logout}>
                Sair mesmo assim
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      )}
    </>
  );
}
