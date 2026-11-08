package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Category {
    @Setter private String name;
    private OperationType type;
    @Setter private Double budget;
}
