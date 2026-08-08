package in.excelkidshub.platform.progress.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for POST /progress/save.
 * Called by the reading studio on every page load.
 */
@Data
public class SaveProgressRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Page number is required")
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNumber;
}
