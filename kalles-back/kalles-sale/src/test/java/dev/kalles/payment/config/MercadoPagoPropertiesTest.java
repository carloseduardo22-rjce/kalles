package dev.kalles.payment.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MercadoPagoPropertiesTest {

    @Test
    void shouldBindEveryMercadoPagoKeyDeclaredInTheApplicationYaml() throws IOException {
        MercadoPagoProperties properties = bindApplicationYaml();

        assertThat(properties.appId()).isEqualTo("448684586415948");
        assertThat(properties.userId()).isEqualTo("me");
        assertThat(properties.redirectUri()).isEqualTo("https://localhost:3000/admin/pagamentos/mp-callback");
        assertThat(properties.accessToken()).isEmpty();
        assertThat(properties.clientId()).isEmpty();
        assertThat(properties.clientSecret()).isEmpty();
        assertThat(properties.webhookSecret()).isEmpty();
    }

    private MercadoPagoProperties bindApplicationYaml() throws IOException {
        List<PropertySource<?>> loaded = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yml"));

        MutablePropertySources sources = new MutablePropertySources();
        loaded.forEach(sources::addLast);

        return new Binder(
                ConfigurationPropertySources.from(sources),
                new PlaceholderResolvingBinder(sources)
        ).bind("mercadopago", MercadoPagoProperties.class).get();
    }

    private static final class PlaceholderResolvingBinder
            implements org.springframework.boot.context.properties.bind.PlaceholdersResolver {

        private final PropertySourcesPropertyResolver resolver;

        private PlaceholderResolvingBinder(MutablePropertySources sources) {
            this.resolver = new PropertySourcesPropertyResolver(sources);
        }

        @Override
        public Object resolvePlaceholders(Object value) {
            if (value instanceof String text) {
                return resolver.resolvePlaceholders(text);
            }
            return value;
        }
    }
}
