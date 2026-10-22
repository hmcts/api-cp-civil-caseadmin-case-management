package uk.gov.hmcts.cp.openapi.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExtractCasesRequestValidationTest {

    private static ExtractCasesRequest.ExtractCasesRequestBuilder validRequest() {
        return ExtractCasesRequest.builder()
                .caseIds(List.of(UUID.randomUUID(), UUID.randomUUID()))
                .newGroup(true)
                .resultingIntent(ResultingIntent.ADJOURN);
    }

    private static boolean hasViolation(ExtractCasesRequest request, String property) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return !factory.getValidator().validateProperty(request, property).isEmpty();
        }
    }

    @Test
    void fully_populated_request_should_pass_validation() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(validRequest().build())).isEmpty();
        }
    }

    @Test
    void lead_case_id_is_optional() {
        var request = validRequest().leadCaseId(null).build();

        assertThat(hasViolation(request, "leadCaseId")).isFalse();
    }

    @Test
    void missing_case_ids_should_fail_validation() {
        assertThat(hasViolation(validRequest().caseIds(null).build(), "caseIds")).isTrue();
    }

    @Test
    void empty_case_ids_should_fail_validation() {
        assertThat(hasViolation(validRequest().caseIds(List.of()).build(), "caseIds")).isTrue();
    }

    @Test
    void single_case_id_should_pass_validation() {
        var request = validRequest().caseIds(List.of(UUID.randomUUID())).build();

        assertThat(hasViolation(request, "caseIds")).isFalse();
    }

    @Test
    void missing_new_group_flag_should_fail_validation() {
        assertThat(hasViolation(validRequest().newGroup(null).build(), "newGroup")).isTrue();
    }

    @Test
    void missing_resulting_intent_should_fail_validation() {
        assertThat(hasViolation(validRequest().resultingIntent(null).build(), "resultingIntent")).isTrue();
    }

    @Test
    void extracting_to_individual_cases_with_lead_case_selected_should_pass_validation() {
        UUID leadCaseId = UUID.randomUUID();
        var request = ExtractCasesRequest.builder()
                .caseIds(List.of(leadCaseId))
                .leadCaseId(leadCaseId)
                .newGroup(false)
                .resultingIntent(ResultingIntent.WITHDRAW)
                .build();

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(request)).isEmpty();
        }
    }
}
