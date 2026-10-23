package dev.kalles.client.dto;

import dev.kalles.client.entity.PersonType;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import java.util.ArrayList;
import java.util.List;

public class ClientRequestGroups implements DefaultGroupSequenceProvider<ClientRequest> {

    public interface NaturalPerson {
    }

    public interface LegalEntity {
    }

    @Override
    public List<Class<?>> getValidationGroups(Class<?> klass, ClientRequest request) {
        List<Class<?>> groups = new ArrayList<>();
        groups.add(ClientRequest.class);
        if (request != null && request.personType() == PersonType.FISICA) {
            groups.add(NaturalPerson.class);
        }
        if (request != null && request.personType() == PersonType.JURIDICA) {
            groups.add(LegalEntity.class);
        }
        return groups;
    }
}
