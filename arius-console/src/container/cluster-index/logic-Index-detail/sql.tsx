import * as React from 'react';
import { EditorCom } from 'component/editor';
import * as monaco from 'monaco-editor';
import { getSqlSuggestionsList } from 'constants/sql-suggestions';
import { editorShortCut } from 'lib/utils';
import { btnFunc } from 'typesPath/base-types';

interface ISQLProps {
  className?: string;
  value?: string;
  readOnly?: boolean;
}
export class SQLQuery1 extends React.Component<ISQLProps> {
  private editor: monaco.editor.IStandaloneCodeEditor;

  public getLeftToolBar: btnFunc = () => {
    return [{
      key: 'toDSL',
      icon: 'dsui-icon-yunhang',
      name: 'toDSL',
      title: 'toDSL',
      onClick: () => {
        // fileTab.startRun();
      },
      get disable() {
        // TODO:
        return false;
      },
    }];
  }
  public getEditorValue = () => {
    return this.editor?.getValue();
  }

  public customMount = (editor: monaco.editor.IStandaloneCodeEditor, monaco: any) => {
    this.editor = editor;
    const model = editor.getModel();
    const lineNumber = model.getLineCount();

    editor.setPosition({
      lineNumber,
      column: model.getLineMaxColumn(lineNumber),
    });

    const suggestions = getSqlSuggestionsList();

    monaco.languages.registerCompletionItemProvider('sql', {
      provideCompletionItems(model: monaco.editor.ITextModel, position: monaco.Position) {
        const word = model.getWordUntilPosition(position);
        const range = {
          startLineNumber: position.lineNumber,
          endLineNumber: position.lineNumber,
          startColumn: word.startColumn,
          endColumn: word.endColumn,
        };
        return {
          suggestions: suggestions.map(item => ({ ...item, range })),
        };
      },
      triggerCharacters: [':'],
    });

    editorShortCut(editor, this.getLeftToolBar);
  }

  public render() {
    const { value = '', className = '', readOnly = false } = this.props;
    return (
      <div className={`${className} tab-container`}>
        <EditorCom
          options={{
            language: 'sql',
            value,
            theme: 'vs',
            wordWrap: 'on',
            automaticLayout: true,
            readOnly,
          }}
          customMount={this.customMount}
        />
      </div>
    );
  }
}
