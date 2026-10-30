package uk.gov.hmcts.cp.openapi.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupCaseResponseValidationTest {

    private static GroupMemberCase validLeadCase() {
        return GroupMemberCase.builder()
                .caseId(UUID.randomUUID())
                .caseUrn("URN-001")
                .defendantName("Jane Doe")
                .build();
    }

    private static GroupCaseResponse.GroupCaseResponseBuilder validResponse() {
        return GroupCaseResponse.builder()
                .groupId(UUID.randomUUID())
                .prosecutingAuthority("GAEAA01")
                .courtHearingLocation("B01LY00")
                .dateOfHearing(LocalDate.of(2026, 11, 1))
                .offenceType("Council Tax")
                .leadCase(validLeadCase())
                .cases(List.of(validLeadCase()))
                .createdDate(LocalDate.of(2026, 10, 1))
                .resultingIntent(ResultingIntent.ADJOURN);
    }

    private static Set<ConstraintViolation<GroupCaseResponse>> validate(GroupCaseResponse response) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(response);
        }
    }

    private static Set<ConstraintViolation<GroupCaseResponse>> validateProperty(GroupCaseResponse response, String property) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validateProperty(response, property);
        }
    }

    @Test
    void fully_populated_response_should_pass_validation() {
        assertThat(validate(validResponse().build())).isEmpty();
    }

    @Test
    void optional_fields_may_be_supplied() {
        var response = validResponse()
                .referredCaseId(UUID.randomUUID())
                .caseCount(1)
                .build();

        assertThat(validate(response)).isEmpty();
    }

    @Test
    void missing_group_id_should_fail_validation() {
        assertThat(validateProperty(validResponse().groupId(null).build(), "groupId")).isNotEmpty();
    }

    @Test
    void missing_prosecuting_authority_should_fail_validation() {
        assertThat(validateProperty(validResponse().prosecutingAuthority(null).build(), "prosecutingAuthority")).isNotEmpty();
    }

    @Test
    void missing_court_hearing_location_should_fail_validation() {
        assertThat(validateProperty(validResponse().courtHearingLocation(null).build(), "courtHearingLocation")).isNotEmpty();
    }

    @Test
    void missing_date_of_hearing_should_fail_validation() {
        assertThat(validateProperty(validResponse().dateOfHearing(null).build(), "dateOfHearing")).isNotEmpty();
    }

    @Test
    void missing_offence_type_should_fail_validation() {
        assertThat(validateProperty(validResponse().offenceType(null).build(), "offenceType")).isNotEmpty();
    }

    @Test
    void missing_cases_should_fail_validation() {
        assertThat(validateProperty(validResponse().cases(null).build(), "cases")).isNotEmpty();
    }

    @Test
    void missing_created_date_should_fail_validation() {
        assertThat(validateProperty(validResponse().createdDate(null).build(), "createdDate")).isNotEmpty();
    }

    @Test
    void missing_resulting_intent_should_fail_validation() {
        assertThat(validateProperty(validResponse().resultingIntent(null).build(), "resultingIntent")).isNotEmpty();
    }

    @Test
    void missing_lead_case_should_fail_validation() {
        assertThat(validateProperty(validResponse().leadCase(null).build(), "leadCase")).isNotEmpty();
    }

    @Test
    void lead_case_missing_required_fields_should_fail_cascaded_validation() {
        var response = validResponse().leadCase(new GroupMemberCase()).build();

        var violatedProperties = validate(response).stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        assertThat(violatedProperties).containsExactlyInAnyOrder(
                "leadCase.caseId", "leadCase.caseUrn", "leadCase.defendantName");
    }

    @Test
    void lead_case_optional_fields_may_be_omitted() {
        var response = validResponse().leadCase(validLeadCase()).build();

        assertThat(response.getLeadCase().getDefendantDoB()).isNull();
        assertThat(response.getLeadCase().getAddress()).isNull();
        assertThat(validate(response)).isEmpty();
    }
}
