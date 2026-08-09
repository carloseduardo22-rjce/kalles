package dev.kalles.support.dto;

import dev.kalles.support.domain.Ticket;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TicketResponse(
    String id,
    String title,
    String description,

    @Schema(description = "Current status", allowableValues = {"OPEN", "IN_PROGRESS", "WAITING_FOR_CUSTOMER", "RESOLVED", "CLOSED"})
    String status,

    @Schema(description = "Priority derived from the category", allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    String priority,

    UserSummary user,
    AgentSummary agent,
    CategorySummary category,
    SlaInfo sla,
    List<InteractionResponse> interactions
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus().name(),
                ticket.getPriority().name(),
                UserSummary.from(ticket.getUser()),
                ticket.getAgent() != null ? AgentSummary.from(ticket.getAgent()) : null,
                CategorySummary.from(ticket.getCategory()),
                SlaInfo.from(ticket.getSla()),
                ticket.getInteractions().stream().map(InteractionResponse::from).toList()
        );
    }

    public record UserSummary(String email, String name) {
        public static UserSummary from(dev.kalles.support.domain.User user) {
            return user != null ? new UserSummary(user.getEmail(), user.getName()) : null;
        }
    }

    public record AgentSummary(String employeeId, String name) {
        public static AgentSummary from(dev.kalles.support.domain.Agent agent) {
            return agent != null ? new AgentSummary(agent.getEmployeeId(), agent.getName()) : null;
        }
    }

    public record CategorySummary(String name, String subcategory, String defaultPriority) {
        public static CategorySummary from(dev.kalles.support.domain.Category category) {
            return category != null
                    ? new CategorySummary(category.getName(), category.getSubcategory(), category.getDefaultPriority().name())
                    : null;
        }
    }

    public record SlaInfo(boolean active, java.time.Instant startedAt) {
        public static SlaInfo from(dev.kalles.support.domain.Sla sla) {
            return sla != null ? new SlaInfo(sla.isActive(), sla.getStartedAt()) : null;
        }
    }
}
