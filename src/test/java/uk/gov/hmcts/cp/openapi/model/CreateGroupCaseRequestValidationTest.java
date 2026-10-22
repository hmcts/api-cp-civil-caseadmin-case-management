package uk.gov.hmcts.cp.openapi.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CreateGroupCaseRequestValidationTest {

    private static LeadCase validLeadCase() {
        return LeadCase.builder()
                .leadCaseId(UUID.randomUUID())
                .caseUrn("URN-001")
                .defendantName("Jane Doe")
                .build();
    }

    private static CreateGroupCaseRequest.CreateGroupCaseRequestBuilder validRequest() {
        return CreateGroupCaseRequest.builder()
                .groupId(UUID.randomUUID())
                .leadCase(validLeadCase())
                .dateOfHearing(LocalDate.of(2026, 11, 1))
                .caseCount(10)
                .offenceType("Council Tax")
                .courtHearingLocation("B01LY00")
                .prosecutingAuthority("GAEAA01");
    }

    private static Set<ConstraintViolation<CreateGroupCaseRequest>> validate(CreateGroupCaseRequest request) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(request);
        }
    }

    private static Set<ConstraintViolation<CreateGroupCaseRequest>> validateProperty(CreateGroupCaseRequest request, String property) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validateProperty(request, property);
        }
    }

    @Test
    void fully_populated_request_should_pass_validation() {
        assertThat(validate(validRequest().build())).isEmpty();
    }

    @Test
    void optional_fields_may_be_supplied() {
        var request = validRequest()
                .parentGroupId(UUID.randomUUID())
                .resultingIntent(ResultingIntent.WITHDRAW)
                .build();

        assertThat(validate(request)).isEmpty();
    }

    @Test
    void empty_request_should_report_every_required_field() {
        var violatedProperties = validate(new CreateGroupCaseRequest()).stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        assertThat(violatedProperties).containsExactlyInAnyOrder(
                "groupId", "leadCase", "dateOfHearing", "caseCount", "offenceType",
                "courtHearingLocation", "prosecutingAuthority");
    }

    @Test
    void missing_group_id_should_fail_validation() {
        assertThat(validateProperty(validRequest().groupId(null).build(), "groupId")).isNotEmpty();
    }

    @Test
    void missing_lead_case_should_fail_validation() {
        assertThat(validateProperty(validRequest().leadCase(null).build(), "leadCase")).isNotEmpty();
    }

    @Test
    void missing_date_of_hearing_should_fail_validation() {
        assertThat(validateProperty(validRequest().dateOfHearing(null).build(), "dateOfHearing")).isNotEmpty();
    }

    @Test
    void missing_offence_type_should_fail_validation() {
        assertThat(validateProperty(validRequest().offenceType(null).build(), "offenceType")).isNotEmpty();
    }

    @Test
    void missing_court_hearing_location_should_fail_validation() {
        assertThat(validateProperty(validRequest().courtHearingLocation(null).build(), "courtHearingLocation")).isNotEmpty();
    }

    @Test
    void missing_prosecuting_authority_should_fail_validation() {
        assertThat(validateProperty(validRequest().prosecutingAuthority(null).build(), "prosecutingAuthority")).isNotEmpty();
    }

    @Test
    void missing_case_count_should_fail_validation() {
        assertThat(validateProperty(validRequest().caseCount(null).build(), "caseCount")).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void case_count_below_one_should_fail_validation(int caseCount) {
        assertThat(validateProperty(validRequest().caseCount(caseCount).build(), "caseCount")).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5000})
    void case_count_of_one_or_more_should_pass_validation(int caseCount) {
        assertThat(validateProperty(validRequest().caseCount(caseCount).build(), "caseCount")).isEmpty();
    }

    @Test
    void lead_case_missing_required_fields_should_fail_cascaded_validation() {
        var request = validRequest().leadCase(new LeadCase()).build();

        var violatedProperties = validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        assertThat(violatedProperties).containsExactlyInAnyOrder(
                "leadCase.leadCaseId", "leadCase.caseUrn", "leadCase.defendantName");
    }

    @Test
    void lead_case_date_of_birth_is_optional() {
        var request = validRequest().leadCase(validLeadCase()).build();

        assertThat(request.getLeadCase().getDefendantDoB()).isNull();
        assertThat(validate(request)).isEmpty();
    }
}
