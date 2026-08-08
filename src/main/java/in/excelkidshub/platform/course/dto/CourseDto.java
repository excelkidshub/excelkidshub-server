package in.excelkidshub.platform.course.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Course summary returned to the frontend.
 * Includes hasAccess so the dashboard can render Open/Subscribe buttons correctly.
 */
@Data
@Builder
public class CourseDto {

    private Long    id;
    private String  title;
    private String  levelName;
    private String  slug;
    private String  description;
    private String  ageGroup;
    private String  thumbnailUrl;
    private Integer totalLessons;
    private Boolean isFree;
    private Boolean hasAccess;   // true if user is subscribed or course is free
}
