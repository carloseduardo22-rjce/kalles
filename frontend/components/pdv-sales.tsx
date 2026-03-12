"use client";

import type React from "react";

import { useState, useEffect } from "react";
import {
  Search,
  Barcode,
  ShoppingCart,
  Trash2,
  Plus,
  Minus,
  XCircle,
  DollarSign,
  Keyboard,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type {
  CashSession,
  Sale,
  SaleItem,
  Product,
  ItemRemovalLog,
  SaleCancellationLog,
} from "@/lib/types";
import {
  findProductByCode,
  searchProductsByDescription,
  MOCK_PRODUCTS,
} from "@/lib/mock-products";
import { ItemRemovalDialog } from "@/components/item-removal-dialog";
import { SaleCancellationDialog } from "@/components/sale-cancellation-dialog";
import { PaymentDialog } from "@/components/payment-dialog";

interface PdvSalesProps {
  session: CashSession;
  onSaleComplete: (sale: Sale) => void;
}

export function PdvSales({ session, onSaleComplete }: PdvSalesProps) {
  const [currentSale, setCurrentSale] = useState<Sale>({
    id: `SALE-${Date.now()}`,
    sessionId: session.id,
    items: [],
    subtotal: 0,
    total: 0,
    status: "in-progress",
    createdAt: new Date().toISOString(),
  });

  const [productCode, setProductCode] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<Product[]>([]);
  const [searchDialogOpen, setSearchDialogOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const [removalDialogOpen, setRemovalDialogOpen] = useState(false);
  const [itemToRemove, setItemToRemove] = useState<SaleItem | null>(null);
  const [removalLogs, setRemovalLogs] = useState<ItemRemovalLog[]>([]);

  const [cancellationDialogOpen, setCancellationDialogOpen] = useState(false);
  const [cancellationLogs, setCancellationLogs] = useState<
    SaleCancellationLog[]
  >([]);

  const [paymentDialogOpen, setPaymentDialogOpen] = useState(false);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (currentSale.status === "cancelled") return;
      if (e.key === "F2") {
        e.preventDefault();
        if (!searchDialogOpen) setSearchDialogOpen(true);
      } else if (e.key === "F8") {
        e.preventDefault();
        if (currentSale.items.length > 0) {
          const lastItem = currentSale.items[currentSale.items.length - 1];
          updateItemQuantity(lastItem.id, 1);
        }
      } else if (e.key === "F9") {
        e.preventDefault();
        if (currentSale.items.length > 0 && !paymentDialogOpen) {
          setPaymentDialogOpen(true);
        }
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [currentSale, searchDialogOpen, paymentDialogOpen]);

  const calculateTotals = (items: SaleItem[]) => {
    const subtotal = items.reduce((sum, item) => sum + item.subtotal, 0);
    return { subtotal, total: subtotal };
  };

  const addOrUpdateItem = (product: Product) => {
    if (currentSale.status === "cancelled") {
      setErrorMessage("Não é possível adicionar itens a uma venda cancelada");
      setTimeout(() => setErrorMessage(""), 3000);
      return;
    }

    setCurrentSale((prevSale) => {
      const existingItemIndex = prevSale.items.findIndex(
        (item) => item.product.id === product.id,
      );

      let updatedItems: SaleItem[];

      if (existingItemIndex >= 0) {
        updatedItems = [...prevSale.items];
        const existingItem = updatedItems[existingItemIndex];
        const newQuantity = existingItem.quantity + 1;
        updatedItems[existingItemIndex] = {
          ...existingItem,
          quantity: newQuantity,
          subtotal: newQuantity * existingItem.unitPrice,
        };
      } else {
        const newItem: SaleItem = {
          id: `ITEM-${Date.now()}-${Math.random()}`,
          product,
          quantity: 1,
          unitPrice: product.price,
          subtotal: product.price,
        };
        updatedItems = [...prevSale.items, newItem];
      }

      const { subtotal, total } = calculateTotals(updatedItems);

      return {
        ...prevSale,
        items: updatedItems,
        subtotal,
        total,
      };
    });

    setErrorMessage("");
  };

  const handleCodeSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!productCode.trim()) return;

    const product = findProductByCode(productCode);

    if (product) {
      addOrUpdateItem(product);
      setProductCode("");
    } else {
      setErrorMessage("Produto não encontrado");
      setTimeout(() => setErrorMessage(""), 3000);
    }
  };

  const handleSearch = () => {
    if (!searchQuery.trim()) {
      setSearchResults([]);
      return;
    }

    const results = searchProductsByDescription(searchQuery);
    setSearchResults(results);
  };

  const handleSelectSearchResult = (product: Product) => {
    addOrUpdateItem(product);
    setSearchDialogOpen(false);
    setSearchQuery("");
    setSearchResults([]);
  };

  const updateItemQuantity = (itemId: string, delta: number) => {
    if (currentSale.status === "cancelled") {
      return;
    }

    setCurrentSale((prevSale) => {
      const updatedItems = prevSale.items
        .map((item) => {
          if (item.id === itemId) {
            const newQuantity = Math.max(0, item.quantity + delta);
            return {
              ...item,
              quantity: newQuantity,
              subtotal: newQuantity * item.unitPrice,
            };
          }
          return item;
        })
        .filter((item) => item.quantity > 0);

      const { subtotal, total } = calculateTotals(updatedItems);

      return {
        ...prevSale,
        items: updatedItems,
        subtotal,
        total,
      };
    });
  };

  const requestItemRemoval = (item: SaleItem) => {
    if (currentSale.status === "cancelled") {
      return;
    }

    setItemToRemove(item);
    setRemovalDialogOpen(true);
  };

  const handleAuthorizedRemoval = (log: ItemRemovalLog) => {
    setRemovalLogs((prev) => [...prev, log]);

    setCurrentSale((prevSale) => {
      const updatedItems = prevSale.items
        .map((item) => {
          if (item.id === log.item.id) {
            const newQuantity = item.quantity - log.removedQuantity;
            if (newQuantity <= 0) {
              return null;
            }
            return {
              ...item,
              quantity: newQuantity,
              subtotal: newQuantity * item.unitPrice,
            };
          }
          return item;
        })
        .filter((item): item is SaleItem => item !== null);

      const { subtotal, total } = calculateTotals(updatedItems);

      return {
        ...prevSale,
        items: updatedItems,
        subtotal,
        total,
      };
    });
  };

  const handleSaleCancellation = (log: SaleCancellationLog) => {
    setCancellationLogs((prev) => [...prev, log]);

    setCurrentSale((prevSale) => ({
      ...prevSale,
      status: "cancelled",
    }));
  };

  const handlePaymentComplete = (payment: any) => {
    const completedSale: Sale = {
      ...currentSale,
      status: "completed",
      payment,
      completedAt: new Date().toISOString(),
    };

    onSaleComplete(completedSale);
    setPaymentDialogOpen(false);

    // Start new sale automatically
    startNewSale();
  };

  const startNewSale = () => {
    setCurrentSale({
      id: `SALE-${Date.now()}`,
      sessionId: session.id,
      items: [],
      subtotal: 0,
      total: 0,
      status: "in-progress",
      createdAt: new Date().toISOString(),
    });
  };

  return (
    <div className="h-screen flex flex-col bg-background">
      {/* Header */}
      <div className="bg-primary text-primary-foreground px-6 py-4 shadow-lg">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <ShoppingCart className="size-8" />
            <div>
              <h1 className="text-2xl font-bold">
                PDV - Venda{" "}
                {currentSale.status === "cancelled"
                  ? "Cancelada"
                  : "em Andamento"}
              </h1>
              <p className="text-sm opacity-90">
                Operador: {session.operatorName} ({session.operatorId})
              </p>
            </div>
          </div>
          <div className="text-right">
            <p className="text-sm opacity-90">Sessão</p>
            <p className="text-lg font-mono font-semibold">{session.id}</p>
          </div>
        </div>
      </div>

      {currentSale.status === "cancelled" && (
        <div className="bg-destructive text-destructive-foreground px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <XCircle className="size-6" />
              <div>
                <p className="text-lg font-bold">Esta venda foi cancelada</p>
                <p className="text-sm opacity-90">Venda #{currentSale.id}</p>
              </div>
            </div>
            <Button onClick={startNewSale} variant="secondary" size="lg">
              Iniciar Nova Venda
            </Button>
          </div>
        </div>
      )}

      <div className="flex-1 flex gap-4 p-4 overflow-hidden">
        {/* Left side - Product input and search */}
        <div className="w-96 space-y-4">
          {/* Code input */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-lg flex items-center gap-2">
                <Barcode className="size-5" />
                Código do Produto
              </CardTitle>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleCodeSubmit} className="space-y-3">
                <Input
                  type="text"
                  placeholder="Código interno ou código de barras"
                  value={productCode}
                  onChange={(e) => setProductCode(e.target.value)}
                  className="h-12 text-lg"
                  autoFocus
                  disabled={currentSale.status === "cancelled"}
                />
                <Button
                  type="submit"
                  className="w-full h-10"
                  disabled={currentSale.status === "cancelled"}
                >
                  Adicionar Produto
                </Button>
              </form>
              {errorMessage && (
                <div className="mt-3 p-3 bg-destructive/10 text-destructive rounded-md text-sm font-medium">
                  {errorMessage}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Keyboard shortcuts */}
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm flex items-center gap-2">
                <Keyboard className="size-4" />
                Atalhos de Teclado
              </CardTitle>
            </CardHeader>
            <CardContent className="pt-0">
              <div className="space-y-1.5 text-xs">
                <div className="flex items-center gap-2">
                  <kbd className="px-1.5 py-0.5 rounded bg-muted font-mono text-[11px] shrink-0">
                    F2
                  </kbd>
                  <span className="text-muted-foreground">
                    Buscar produto por descrição
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <kbd className="px-1.5 py-0.5 rounded bg-muted font-mono text-[11px] shrink-0">
                    F8
                  </kbd>
                  <span className="text-muted-foreground">
                    Incrementar último item
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <kbd className="px-1.5 py-0.5 rounded bg-muted font-mono text-[11px] shrink-0">
                    F9
                  </kbd>
                  <span className="text-muted-foreground">
                    Finalizar pagamento
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Search dialog */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-lg flex items-center gap-2">
                <Search className="size-5" />
                Busca por Descrição
              </CardTitle>
            </CardHeader>
            <CardContent>
              <Dialog
                open={searchDialogOpen}
                onOpenChange={setSearchDialogOpen}
              >
                <DialogTrigger asChild>
                  <Button
                    variant="outline"
                    className="w-full h-10 bg-transparent"
                    disabled={currentSale.status === "cancelled"}
                  >
                    Buscar Produto
                  </Button>
                </DialogTrigger>
                <DialogContent className="max-w-2xl max-h-[80vh]">
                  <DialogHeader>
                    <DialogTitle>Buscar Produto por Descrição</DialogTitle>
                  </DialogHeader>
                  <div className="space-y-4">
                    <div className="flex gap-2">
                      <Input
                        type="text"
                        placeholder="Digite a descrição do produto"
                        value={searchQuery}
                        onChange={(e) => {
                          setSearchQuery(e.target.value);
                          handleSearch();
                        }}
                        className="h-10"
                      />
                    </div>

                    <div className="border rounded-md max-h-96 overflow-y-auto">
                      {searchResults.length > 0 ? (
                        <Table>
                          <TableHeader>
                            <TableRow>
                              <TableHead>Código</TableHead>
                              <TableHead>Descrição</TableHead>
                              <TableHead className="text-right">
                                Preço
                              </TableHead>
                              <TableHead></TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {searchResults.map((product) => (
                              <TableRow key={product.id}>
                                <TableCell className="font-mono text-sm">
                                  {product.internalCode}
                                </TableCell>
                                <TableCell>{product.description}</TableCell>
                                <TableCell className="text-right font-semibold">
                                  R$ {product.price.toFixed(2)}
                                </TableCell>
                                <TableCell>
                                  <Button
                                    size="sm"
                                    onClick={() =>
                                      handleSelectSearchResult(product)
                                    }
                                  >
                                    Adicionar
                                  </Button>
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      ) : searchQuery ? (
                        <div className="p-8 text-center text-muted-foreground">
                          Nenhum produto encontrado
                        </div>
                      ) : (
                        <div className="p-8 text-center text-muted-foreground">
                          Digite para buscar produtos
                        </div>
                      )}
                    </div>
                  </div>
                </DialogContent>
              </Dialog>
            </CardContent>
          </Card>

          {currentSale.status === "in-progress" &&
            currentSale.items.length > 0 && (
              <Card className="border-destructive/20">
                <CardContent className="pt-6">
                  <Button
                    variant="destructive"
                    className="w-full h-12 text-base"
                    onClick={() => setCancellationDialogOpen(true)}
                  >
                    <XCircle className="size-5 mr-2" />
                    Cancelar Venda
                  </Button>
                </CardContent>
              </Card>
            )}

          {/* Quick reference */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm">Produtos Disponíveis</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-xs space-y-1 max-h-64 overflow-y-auto">
                {MOCK_PRODUCTS.map((product) => (
                  <div
                    key={product.id}
                    className="flex justify-between py-1 border-b"
                  >
                    <span className="font-mono">{product.internalCode}</span>
                    <span className="text-muted-foreground truncate ml-2">
                      {product.description}
                    </span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right side - Sale items and total */}
        <div className="flex-1 flex flex-col gap-4">
          {/* Items list */}
          <Card className="flex-1 overflow-hidden flex flex-col">
            <CardHeader className="pb-3">
              <CardTitle className="text-lg">Itens da Venda</CardTitle>
            </CardHeader>
            <CardContent className="flex-1 overflow-y-auto">
              {currentSale.items.length === 0 ? (
                <div className="h-full flex flex-col items-center justify-center text-muted-foreground gap-6">
                  <ShoppingCart className="size-64 opacity-10" />
                  <div className="text-center space-y-3">
                    <p className="text-5xl font-semibold">
                      Nenhum item adicionado
                    </p>
                    <p className="text-3xl text-primary/70">
                      Escaneie ou digite o código de um produto para começar.
                    </p>
                  </div>
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Código</TableHead>
                      <TableHead>Descrição</TableHead>
                      <TableHead className="text-center">Qtd</TableHead>
                      <TableHead className="text-right">Preço Unit.</TableHead>
                      <TableHead className="text-right">Subtotal</TableHead>
                      <TableHead></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {currentSale.items.map((item) => (
                      <TableRow
                        key={item.id}
                        className={
                          currentSale.status === "cancelled" ? "opacity-50" : ""
                        }
                      >
                        <TableCell className="font-mono text-sm">
                          {item.product.internalCode}
                        </TableCell>
                        <TableCell className="font-medium">
                          {item.product.description}
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center justify-center gap-2">
                            <Button
                              size="sm"
                              variant="outline"
                              className="size-8 p-0 bg-transparent"
                              onClick={() => updateItemQuantity(item.id, -1)}
                              disabled={currentSale.status === "cancelled"}
                            >
                              <Minus className="size-3" />
                            </Button>
                            <span className="font-semibold text-lg w-12 text-center">
                              {item.quantity}
                            </span>
                            <Button
                              size="sm"
                              variant="outline"
                              className="size-8 p-0 bg-transparent"
                              onClick={() => updateItemQuantity(item.id, 1)}
                              disabled={currentSale.status === "cancelled"}
                            >
                              <Plus className="size-3" />
                            </Button>
                          </div>
                        </TableCell>
                        <TableCell className="text-right">
                          R$ {item.unitPrice.toFixed(2)}
                        </TableCell>
                        <TableCell className="text-right font-semibold text-lg">
                          R$ {item.subtotal.toFixed(2)}
                        </TableCell>
                        <TableCell>
                          <Button
                            size="sm"
                            variant="ghost"
                            className="size-8 p-0"
                            onClick={() => requestItemRemoval(item)}
                            disabled={currentSale.status === "cancelled"}
                          >
                            <Trash2 className="size-4 text-destructive" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>

          {/* Totals */}
          <Card
            className={
              currentSale.status === "cancelled"
                ? "bg-muted"
                : "bg-primary text-primary-foreground"
            }
          >
            <CardContent className="pt-6">
              <div className="space-y-2">
                <div className="flex justify-between items-center text-lg">
                  <span>Subtotal</span>
                  <span className="font-semibold">
                    R$ {currentSale.subtotal.toFixed(2)}
                  </span>
                </div>
                <div
                  className={`flex justify-between items-center text-3xl font-bold pt-2 border-t ${currentSale.status === "cancelled" ? "border-border" : "border-primary-foreground/20"}`}
                >
                  <span>TOTAL</span>
                  <span>R$ {currentSale.total.toFixed(2)}</span>
                </div>
              </div>
              {currentSale.status === "in-progress" &&
                currentSale.items.length > 0 && (
                  <Button
                    onClick={() => setPaymentDialogOpen(true)}
                    size="lg"
                    className="w-full mt-4 h-14 text-xl bg-emerald-600 hover:bg-emerald-700 text-white"
                  >
                    <DollarSign className="mr-2 size-6" />
                    Finalizar Pagamento
                  </Button>
                )}
            </CardContent>
          </Card>
        </div>
      </div>

      {itemToRemove && (
        <ItemRemovalDialog
          open={removalDialogOpen}
          onOpenChange={setRemovalDialogOpen}
          item={itemToRemove}
          currentOperatorId={session.operatorId}
          currentOperatorName={session.operatorName}
          saleId={currentSale.id}
          onRemove={handleAuthorizedRemoval}
        />
      )}

      <SaleCancellationDialog
        open={cancellationDialogOpen}
        onOpenChange={setCancellationDialogOpen}
        saleId={currentSale.id}
        currentOperatorId={session.operatorId}
        currentOperatorName={session.operatorName}
        onCancel={handleSaleCancellation}
      />

      <PaymentDialog
        open={paymentDialogOpen}
        onOpenChange={setPaymentDialogOpen}
        totalDue={currentSale.total}
        onPaymentComplete={handlePaymentComplete}
      />
    </div>
  );
}
