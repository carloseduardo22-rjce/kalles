package dev.kalles.testsupport;

import dev.kalles.security.context.RequestContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.UUID;

public final class RequestContextExtension implements InvocationInterceptor {

    private final RequestContext context;

    private RequestContextExtension(RequestContext context) {
        this.context = context;
    }

    public static RequestContextExtension tenant(UUID tenantId) {
        return new RequestContextExtension(RequestContext.ofTenant(tenantId));
    }

    public static RequestContextExtension company(UUID companyId) {
        return new RequestContextExtension(RequestContext.empty().withCompany(companyId));
    }

    public static RequestContextExtension tenantAndCompany(UUID tenantId, UUID companyId) {
        return new RequestContextExtension(RequestContext.ofTenant(tenantId).withCompany(companyId));
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation,
                                          ReflectiveInvocationContext<Method> invocationContext,
                                          ExtensionContext extensionContext) throws Throwable {
        proceedWithin(invocation);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        proceedWithin(invocation);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
                                            ReflectiveInvocationContext<Method> invocationContext,
                                            ExtensionContext extensionContext) throws Throwable {
        proceedWithin(invocation);
    }

    private void proceedWithin(Invocation<Void> invocation) throws Throwable {
        RequestContext.callWithin(context, invocation::proceed);
    }
}
