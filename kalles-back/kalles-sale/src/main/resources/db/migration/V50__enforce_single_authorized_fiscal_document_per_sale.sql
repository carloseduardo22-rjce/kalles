CREATE UNIQUE INDEX uk_fiscal_document_authorized_per_sale
    ON fiscal_documents (tenant_id, company_id, sale_id, model)
    WHERE status = 'AUTORIZADO';
