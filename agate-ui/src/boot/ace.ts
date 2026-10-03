import ace from 'ace-builds';

import modeFtlUrl from 'ace-builds/src-noconflict/mode-ftl?url';
ace.config.setModuleUrl('ace/mode/ftl', modeFtlUrl);

import themeMonokaiUrl from 'ace-builds/src-noconflict/theme-monokai?url';
ace.config.setModuleUrl('ace/theme/monokai', themeMonokaiUrl);
