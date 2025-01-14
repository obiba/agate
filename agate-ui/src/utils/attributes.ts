import type { AttributeConfigurationDto } from 'src/models/Agate';
import type { SchemaFormField, SchemaFormObject } from 'src/components/models';

export function attributesToSchema(attributes: AttributeConfigurationDto[], title: string, description: string) {
  const schema = {
    $schema: 'http://json-schema.org/schema#',
    title: title || '',
    description: description || '',
    type: 'array',
    items: [] as SchemaFormField[],
    required: [],
  } as SchemaFormObject;

  (attributes || []).forEach((attribute: AttributeConfigurationDto) => {
    const type = attribute.type.toLowerCase();
    const field = {
      key: attribute.name,
      type: type,
      title: attribute.name,
      description: attribute.description,
    } as SchemaFormField;

    switch (attribute.type.toLowerCase()) {
      // case 'number': // Not supported yet TODO
      case 'integer':
      case 'boolean':
      case 'string':
        if (type === 'string' && attribute.values) {
          field.enum = attribute.values.map((value: string) => ({ key: value, title: value }));
        }

        schema.items.push(field);
        if (attribute.required) {
          schema.required.push(attribute.name);
        }
        break;
    }
  });

  return schema;
}
