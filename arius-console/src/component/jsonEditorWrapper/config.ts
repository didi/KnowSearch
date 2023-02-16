export const mappingExample = `{
  "properties": {
      "key0-string类型不分词": {
          "type": "keyword"
      },
      "key1-string类型分词": {
          "type": "text",
          "analyzer": "standard"
      },
      "key2-int类型": {
          "type": "integer"
      },
      "key3-long类型": {
          "type": "long"
      },
      "key4-double类型": {
          "type": "double"
      },
      "key5-毫秒时间戳": {
          "format": "epoch_millis",
          "type": "date"
      },
      "key6-boolean类型不分词": {
          "type": "boolean"
      },
      "key7-object类型": {
          "type": "object",
          "properties": {
              "key5.1": {
                  "type": "integer"
              },
              "key5.2": {
                  "type": "keyword"
              }
          }
      },
      "key8-nest内嵌类型": {
          "type": "nested",
          "properties": {
              "key6.1": {
                  "type": "integer"
              },
              "key6.2": {
                  "type": "keyword"
              }
          }
      }
  }
}`;

export const esDocLink = {
  mapping: "https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html",
  setting: "https://www.elastic.co/guide/en/elasticsearch/reference/current/index-modules.html",
};

export function formatRequestBodyDoc(data: string, indent: boolean = true) {
  let changed = false;
  let newDoc = data;

  try {
    newDoc = JSON.parse(newDoc);
    newDoc = jsonToString(newDoc, indent);
    changed = changed || newDoc !== data;
  } catch (e) {
    // eslint-disable-next-line no-console
    console.log(e);
  }

  return {
    data: newDoc,
    changed,
  };
}

export function jsonToString(data: any, indent: boolean) {
  return JSON.stringify(data, null, indent ? 4 : 0);
}
