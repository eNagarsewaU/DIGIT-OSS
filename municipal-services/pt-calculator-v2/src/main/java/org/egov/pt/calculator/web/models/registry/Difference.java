package org.egov.pt.calculator.web.models.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Difference {

    String id;

    List<String> fieldsChanged;

    List<String> classesAdded;

    List<String> classesRemoved;

}
