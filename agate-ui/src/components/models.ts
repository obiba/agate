export interface StringMap {
  [key: string]: string | string[] | undefined;
}

export interface Message {
  msg: string;
  timestamp: number;
}

export interface FileObject extends Blob {
  readonly size: number;
  readonly name: string;
  readonly path: string;
  readonly type: string;
}

export interface EnumOption {
  key: string;
  title: string;
}

export interface SchemaFormField {
  key: string;
  type: string;
  format?: string;
  title?: string;
  description?: string;
  default?: string;
  minimum?: number;
  maximum?: number;
  fileFormats?: string[];
  enum?: EnumOption[];
  items: SchemaFormField[];
}

export interface SchemaFormObject {
  $schema: string;
  type: string;
  title?: string;
  description?: string;
  items: SchemaFormField[];
  required: string[];
}

export interface FormObject {
  [key: string]: boolean | number | string | FileObject | FormObject | Array<FormObject> | undefined;
}

export const DefaultAlignment: 'left' | 'right' | 'center' = 'left';
