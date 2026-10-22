package uk.gov.hmcts.cp.openapi.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {

    @Test
    void no_arg_constructor_leaves_fields_null_and_details_empty() {
        ErrorResponse subject = new ErrorResponse();

        assertThat(subject.getError()).isNull();
        assertThat(subject.getMessage()).isNull();
        assertThat(subject.getTimestamp()).isNull();
        assertThat(subject.getTraceId()).isNull();
        assertThat(subject.getDetails()).isNotNull().isEmpty();
    }

    @Test
    void builder_produces_object_with_expected_values() {
        Instant now = Instant.parse("2026-10-22T13:00:00Z");

        ErrorResponse subject = ErrorResponse.builder()
                .error("GROUP_CASE_NOT_FOUND")
                .message("Group case not found")
                .details(Map.of("groupId", "abc"))
                .timestamp(now)
                .traceId("trace-1")
                .build();

        assertThat(subject.getError()).isEqualTo("GROUP_CASE_NOT_FOUND");
        assertThat(subject.getMessage()).isEqualTo("Group case not found");
        assertThat(subject.getDetails()).containsEntry("groupId", "abc");
        assertThat(subject.getTimestamp()).isEqualTo(now);
        assertThat(subject.getTraceId()).isEqualTo("trace-1");
    }

    @Test
    void put_details_item_adds_entry() {
        ErrorResponse subject = new ErrorResponse();

        subject.putDetailsItem("remainingCases", 1);

        assertThat(subject.getDetails()).containsEntry("remainingCases", 1);
    }

    @Test
    void equals_returns_true_for_objects_with_same_content() {
        ErrorResponse a = ErrorResponse.builder().error("E").message("m").build();
        ErrorResponse b = ErrorResponse.builder().error("E").message("m").build();

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    void equals_returns_false_for_different_error_codes() {
        ErrorResponse a = ErrorResponse.builder().error("E1").build();
        ErrorResponse b = ErrorResponse.builder().error("E2").build();

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void to_string_contains_error_and_message() {
        ErrorResponse subject = ErrorResponse.builder().error("BAD_REQUEST").message("fewer than 2 cases remain").build();

        assertThat(subject.toString()).contains("BAD_REQUEST").contains("fewer than 2 cases remain");
    }
}
