import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import Link from "next/link";

export default function IntegracoesPage() {
  // Em produção, isso viraria de variáveis de ambiente (.env)
  const MP_APP_ID = process.env.NEXT_PUBLIC_MP_APP_ID || "448684586415948";
  const REDIRECT_URI =
    process.env.NEXT_PUBLIC_MP_REDIRECT_URI ||
    "https://7e0f-2804-1494-dbb-aa00-986b-3d27-3b5a-f89c.ngrok-free.app/admin/integracoes/mp-callback";

  // O state é ideal para passarmos o ID do Tenant/Dono do sistema
  // Para que no retorno (callback) saibamos de qual Dono é aquele token
  const STATE = "123e4567-e89b-12d3-a456-426614174000";

  const mpAuthUrl = `https://auth.mercadopago.com/authorization?client_id=${MP_APP_ID}&response_type=code&platform_id=mp&state=${STATE}&redirect_uri=${REDIRECT_URI}`;

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-6">Integrações</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Mercado Pago</CardTitle>
            <CardDescription>
              Vincule sua conta do Mercado Pago para receber pagamentos de suas
              vendas diretamente na sua conta.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button
              asChild
              className="w-full bg-[#009EE3] hover:bg-[#0089C7] text-white"
            >
              <Link href={mpAuthUrl}>Conectar Mercado Pago</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
