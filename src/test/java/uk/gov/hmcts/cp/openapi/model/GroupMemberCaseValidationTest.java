package uk.gov.hmcts.cp.openapi.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupMemberCaseValidationTest {

    private static DefendantAddress.DefendantAddressBuilder validAddress() {
        return DefendantAddress.builder()
                .address1("102 Petty France")
                .postcode("SW1H 9AJ");
    }

    private static GroupMemberCase.GroupMemberCaseBuilder validCase() {
        return GroupMemberCase.builder()
                .caseId(UUID.randomUUID())
                .caseUrn("URN-001")
                .defendantName("Jane Doe");
    }

    private static Set<ConstraintViolation<GroupMemberCase>> validate(GroupMemberCase groupMemberCase) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(groupMemberCase);
        }
    }

    private static Set<ConstraintViolation<GroupMemberCase>> validateProperty(GroupMemberCase groupMemberCase, String property) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validateProperty(groupMemberCase, property);
        }
    }

    @Test
    void fully_populated_case_should_pass_validation() {
        var groupMemberCase = validCase()
                .defendantDoB(LocalDate.of(1990, 1, 1))
                .address(validAddress().build())
                .build();

        assertThat(validate(groupMemberCase)).isEmpty();
    }

    @Test
    void case_without_address_should_pass_validation() {
        var groupMemberCase = validCase().build();

        assertThat(groupMemberCase.getAddress()).isNull();
        assertThat(validate(groupMemberCase)).isEmpty();
    }

    @Test
    void empty_case_should_report_every_required_field() {
        var violatedProperties = validate(new GroupMemberCase()).stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        assertThat(violatedProperties).containsExactlyInAnyOrder("caseId", "caseUrn", "defendantName");
    }

    @Test
    void missing_case_id_should_fail_validation() {
        assertThat(validateProperty(validCase().caseId(null).build(), "caseId")).isNotEmpty();
    }

    @Test
    void missing_case_urn_should_fail_validation() {
        assertThat(validateProperty(validCase().caseUrn(null).build(), "caseUrn")).isNotEmpty();
    }

    @Test
    void missing_defendant_name_should_fail_validation() {
        assertThat(validateProperty(validCase().defendantName(null).build(), "defendantName")).isNotEmpty();
    }

    @Test
    void defendant_date_of_birth_is_optional() {
        var groupMemberCase = validCase().build();

        assertThat(groupMemberCase.getDefendantDoB()).isNull();
        assertThat(validate(groupMemberCase)).isEmpty();
    }

    @Test
    void address_missing_address_line_one_should_fail_cascaded_validation() {
        var groupMemberCase = validCase().address(DefendantAddress.builder().build()).build();

        var violatedProperties = validate(groupMemberCase).stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        assertThat(violatedProperties).containsExactlyInAnyOrder("address.address1");
    }

    @Test
    void address_line_exceeding_max_length_should_fail_cascaded_validation() {
        String tooLong = "A".repeat(36);
        var groupMemberCase = validCase()
                .address(validAddress().address2(tooLong).build())
                .build();

        var violatedProperties = validate(groupMemberCase).stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        assertThat(violatedProperties).containsExactlyInAnyOrder("address.address2");
    }

    @Test
    void address_optional_lines_two_to_five_may_be_omitted() {
        var groupMemberCase = validCase().address(validAddress().build()).build();

        assertThat(groupMemberCase.getAddress().getAddress2()).isNull();
        assertThat(groupMemberCase.getAddress().getAddress3()).isNull();
        assertThat(groupMemberCase.getAddress().getAddress4()).isNull();
        assertThat(groupMemberCase.getAddress().getAddress5()).isNull();
        assertThat(validate(groupMemberCase)).isEmpty();
    }

    @Test
    void address_with_valid_uk_postcode_should_pass_validation() {
        var groupMemberCase = validCase()
                .address(validAddress().postcode("EC4M 7LS").build())
                .build();

        assertThat(validate(groupMemberCase)).isEmpty();
    }

    @Test
    void address_with_malformed_postcode_should_fail_cascaded_validation() {
        var groupMemberCase = validCase()
                .address(validAddress().postcode("12345").build())
                .build();

        var violatedProperties = validate(groupMemberCase).stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();

        assertThat(violatedProperties).containsExactlyInAnyOrder("address.postcode");
    }

    @Test
    void address_postcode_is_optional() {
        var groupMemberCase = validCase().address(DefendantAddress.builder().address1("1 Court Street").build()).build();

        assertThat(groupMemberCase.getAddress().getPostcode()).isNull();
        assertThat(validate(groupMemberCase)).isEmpty();
    }
}
