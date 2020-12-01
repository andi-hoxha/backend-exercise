package models.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Datas {

    @NotEmpty
    private String category;
    @NotEmpty
    private Double value;
}
