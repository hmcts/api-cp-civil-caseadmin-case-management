package uk.gov.hmcts.cp.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.openapi.api.GroupCasesApi;
import uk.gov.hmcts.cp.openapi.api.RootApi;
import uk.gov.hmcts.cp.openapi.model.CreateGroupCaseRequest;
import uk.gov.hmcts.cp.openapi.model.DefendantAddress;
import uk.gov.hmcts.cp.openapi.model.ErrorResponse;
import uk.gov.hmcts.cp.openapi.model.ExtractCasesRequest;
import uk.gov.hmcts.cp.openapi.model.GroupCaseResponse;
import uk.gov.hmcts.cp.openapi.model.GroupMemberCase;
import java.lang.reflect.Field;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class OpenApiObjectsTest {
    @Test
    void generated_error_response_should_have_expected_fields() {
        assertThat(ErrorResponse.class).hasDeclaredMethods("error", "message", "details", "traceId");
    }

    @Test
    void generated_create_group_case_request_should_have_expected_fields() {
        assertThat(CreateGroupCaseRequest.class).hasDeclaredFields(
                "groupId", "leadCase", "dateOfHearing", "caseCount", "offenceType",
                "courtHearingLocation", "prosecutingAuthority", "parentGroupId", "resultingIntent");
    }

    @Test
    void generated_extract_cases_request_should_have_expected_fields() {
        assertThat(ExtractCasesRequest.class).hasDeclaredFields("caseIds", "leadCaseId", "newGroup", "resultingIntent");
    }

    @Test
    void generated_group_case_response_should_have_expected_fields() {
        assertThat(GroupCaseResponse.class).hasDeclaredFields("groupId", "leadCase", "referredCaseId", "cases", "createdDate", "resultingIntent");
    }

    @Test
    void generated_group_member_case_should_have_expected_fields() {
        assertThat(GroupMemberCase.class).hasDeclaredFields("caseId", "caseUrn", "defendantName", "defendantDoB", "address");
    }

    @Test
    void generated_defendant_address_should_have_expected_fields() {
        assertThat(DefendantAddress.class).hasDeclaredFields(
                "address1", "address2", "address3", "address4", "address5", "postcode");
    }

    @Test
    void generated_root_api_should_have_expected_methods() {
        assertThat(RootApi.class).hasDeclaredMethods("getRoot");
    }

    @Test
    void generated_group_cases_api_should_have_expected_methods() {
        assertThat(GroupCasesApi.class).hasDeclaredMethods(
                "createGroupCase", "searchGroupCases", "getGroupCase",
                "getGroupCaseResultingIntents", "extractGroupCases");
    }

    @Test
    void generated_error_response_timestamp_should_be_instant() throws Exception {
        Field timestampField = ErrorResponse.class.getDeclaredField("timestamp");

        assertThat(timestampField.getType())
                .as("timestamp field type")
                .isEqualTo(Instant.class);
    }
}
