package com.didichuxing.datachannel.arius.admin.client.mapping;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-04
 */
@Data
public class SpecialField {

    private String dateField;

    /**
     * 时间字段的格式
     */
    private String dateFieldFormat;

    private String idField;

    private String routingField;

    public static SpecialField analyzeFromFields(Collection<Field> fields) {
        SpecialField specialField = new SpecialField();
        for (Field field : fields) {
            if (field.getDateField() != null && field.getDateField()) {
                specialField.setDateField(field.getName());
                specialField.setDateFieldFormat(field.getDateFieldFormat());
                break;
            }
        }

        List<String> idFields = fields.stream().filter(field -> field.getIdField() != null && field.getIdField())
            .map(Field::getName).collect(Collectors.toList());
        specialField.setIdField(String.join(",", idFields));

        List<String> routingFields = fields.stream()
            .filter(field -> field.getRoutingField() != null && field.getRoutingField()).map(Field::getName)
            .collect(Collectors.toList());
        specialField.setRoutingField(String.join(",", routingFields));

        return specialField;
    }

    public static SpecialField analyzeFromFields(Collection<Field> fields, Set<String> removeFieldNameSet) {

        if (removeFieldNameSet == null) {
            return analyzeFromFields(fields);
        }

        SpecialField specialField = new SpecialField();
        for (Field field : fields) {
            if (field.getDateField() != null && field.getDateField() && !removeFieldNameSet.contains(field.getName())) {
                specialField.setDateField(field.getName());
                specialField.setDateFieldFormat(field.getDateFieldFormat());
                break;
            }
        }

        List<String> idFields = fields.stream().filter(
            field -> field.getIdField() != null && field.getIdField() && !removeFieldNameSet.contains(field.getName()))
            .map(Field::getName).collect(Collectors.toList());
        specialField.setIdField(String.join(",", idFields));

        List<String> routingFields = fields.stream()
            .filter(field -> field.getRoutingField() != null && field.getRoutingField()
                             && !removeFieldNameSet.contains(field.getName()))
            .map(Field::getName).collect(Collectors.toList());
        specialField.setRoutingField(String.join(",", routingFields));

        return specialField;
    }

}
